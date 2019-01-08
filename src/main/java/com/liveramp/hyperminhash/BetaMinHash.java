package com.liveramp.hyperminhash;

import util.hash.MetroHash128;

import java.nio.ByteBuffer;

/**
 * Implementation of HyperMinHash described in Yu and Weber: https://arxiv.org/pdf/1710.08436.pdf.
 * This class implements LogLog-Beta described in Qin, Kim, et al. here: https://arxiv.org/pdf/1612.02284.pdf.
 * Loglog-Beta is almost identical in accuracy to HyperLogLog and HyperLogLog++ except it performs better on cardinality
 * estimations for small datasets (n <= 200_000). It's also much simpler to implement.
 *
 * The log log implementation uses the values of p and beta coefficients tested in the Loglog-beta paper. It's possible
 * to use different values of P but we'd need to recompute the beta coefficients which is a computationally intensive
 * process. So for now, this impl doesn't support using different values of P. This being said the current value of P
 * works with high accuracy for very large cardinalities and small jaccard  indices. See the paper for more details.
 *
 * Similarly, we use values of Q and R suggested in the HyperMinHash paper. Those are theoretically changeable, but the
 * current values should provide sufficient accuracy for set cardinalities up to 2^89 (see Hyperminhash paper for
 * reference).
 *
 * If you'd like this class to support custom Q or R or P values, please open a github issue.
 *
 */
public class BetaMinHash {

  // HLL Precision parameter
  public final static int P = 14;
  public final static int NUM_REGISTERS = (int) Math.pow(2, P);


  // TODO add actual validation if necessary
  // Q + R must always be <= 16 since we're packing values into 16 bit registers
  public final static int Q = 6;
  public final static int R = 10;

  final short[] registers;

  public BetaMinHash() {
    registers = new short[NUM_REGISTERS];
  }

  BetaMinHash(short[] registers) {
    this();
    System.arraycopy(registers, 0, this.registers, 0, registers.length);
  }

  public BetaMinHash(BetaMinHash other) {
    this(other.registers);
  }

  public void add(byte[] val) {
    MetroHash128 hash = new MetroHash128(1337).apply(ByteBuffer.wrap(val));
    ByteBuffer buf = ByteBuffer.allocate(16);
    hash.writeBigEndian(buf);
    addHash(buf);
  }

  /**
   * @param _128BitHash
   */
  private void addHash(ByteBuffer _128BitHash) {
    if (_128BitHash.array().length != 16) {
      throw new IllegalArgumentException("input hash should be 16 bytes");
    }

    long hashLeftHalf = _128BitHash.getLong(0);
    long hashRightHalf = _128BitHash.getLong(8);

    int registerIndex = getLeftmostPBits(hashLeftHalf);
    short rBits = getRightmostRBits(hashLeftHalf);

    byte leftmostOneBitPosition = getLeftmostOneBitPosition(hashRightHalf);

    short packedRegister = packIntoRegister(leftmostOneBitPosition, rBits);
    if (registers[registerIndex] < packedRegister) {
      registers[registerIndex] = packedRegister;
    }
  }

  private int getLeftmostPBits(long hash) {
    return (int) (hash >>> (Long.SIZE - P));
  }

  /**
   * Finds the position of the leftmost one-bit in the first (2^Q)-1 bits.
   *
   * @param hash
   * @return
   */
  private byte getLeftmostOneBitPosition(long hash) {
    // To find the position of the leftmost 1-bit in the first (2^Q)-1 bits
    // We zero out all bits to the right of the first (2^Q)-1 bits then add a
    // 1-bit in the 2^Qth position of the bits to search. This way if the bits we're
    // searching are all 0, we take the position of the leftmost 1-bit to be 2^Q
    int _2q = (1 << Q) - 1;
    int shiftAmount = (Long.SIZE - _2q);

    // zero all bits to the right of the first (2^Q)-1 bits
    long _2qSearchBits = ((hash >>> shiftAmount) << shiftAmount);

    // add a 1-bit in the 2^Qth position
    _2qSearchBits += (1 << (shiftAmount - 1));

    return (byte) (Long.numberOfLeadingZeros(_2qSearchBits) + 1);
  }

  private short getRightmostRBits(long hash) {
    return (short) (hash << (Long.SIZE - R) >>> Long.SIZE - R);
  }

  /**
   * Creates a new tuple/register value for the LL-Beta by bit-packing the number
   * of leading zeros with the rightmost R bits.
   *
   * @param leftmostOnebitPosition
   * @param rightmostRBits
   * @return
   */
  private short packIntoRegister(byte leftmostOnebitPosition, short rightmostRBits) {
    // Q is at most 6, which means that with R<=10, we should be able to store these two
    // numbers in the same register
    return (short) ((leftmostOnebitPosition << R) | rightmostRBits);
  }

  public long cardinality() {
    return BetaMinHashCardinalityGetter.cardinality(this);
  }

  /**
   * @param sketches
   * @return Merged sketch representing the input sketches
   */
  public static BetaMinHash merge(BetaMinHash... sketches) {
    return BetaMinHashMergeGetter.merge(sketches);
  }

  /**
   * @param sketches
   * @return Union cardinality estimation
   */
  public static long union(BetaMinHash... sketches) {
    return merge(sketches).cardinality();
  }

  /**
   * @param sketches
   * @return Intersection cardinality estimation
   */
  public static long intersection(BetaMinHash... sketches) {
    return BetaMinHashIntersectionGetter.getIntersection(sketches);
  }

  /**
   * @param sketches 
   * @return Jaccard index estimation
   */
  public static double similarity(BetaMinHash... sketches) {
    return BetaMinHashSimilarityGetter.similarity(sketches);
  }
}
