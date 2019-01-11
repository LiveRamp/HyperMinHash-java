package com.liveramp.hyperminhash;

import util.hash.MetroHash128;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Implementation of HyperMinHash described in Yu and Weber: https://arxiv.org/pdf/1710.08436.pdf.
 * This class implements LogLog-Beta described in Qin, Kim, et al. here:
 * https://arxiv.org/pdf/1612.02284.pdf. Loglog-Beta is almost identical in accuracy to HyperLogLog
 * and HyperLogLog++ except it performs better on cardinality estimations for small datasets (n <=
 * 200_000). It's also much simpler to implement.
 * <p>
 * The log log implementation uses the values of p and beta coefficients tested in the Loglog-beta
 * paper. It's possible to use different values of P but we'd need to recompute the beta
 * coefficients which is a computationally intensive process. So for now, this impl doesn't support
 * using different values of P. This being said the current value of P works with high accuracy for
 * very large cardinalities and small jaccard  indices. See the paper for more details.
 * <p>
 * <p>
 * Similarly, we use values of Q and R suggested in the HyperMinHash paper. Those are theoretically
 * changeable, but the current values should provide sufficient accuracy for set cardinalities up to
 * 2^89 (see Hyperminhash paper for reference).
 * <p>
 * If you want to be able to combine multiple BetaMinHash instances, or compute their intersection,
 * you can use {@link BetaMinHashCombiner}.
 * <p>
 * If you'd like this class to support custom Q or R or P values, please open a github issue.
 * <p>
 */
public class BetaMinHash implements IntersectionSketch<BetaMinHash> {

  // HLL Precision parameter
  public static final int P = 14;
  public static final int NUM_REGISTERS = (int) Math.pow(2, P);


  // TODO add actual validation if necessary
  // Q + R must always be <= 16 since we're packing values into 16 bit registers
  public static final int Q = 6;
  public static final int R = 10;

  private static final int HASH_SEED = 1337;

  final short[] registers;

  public BetaMinHash() {
    registers = new short[NUM_REGISTERS];
  }

  private BetaMinHash(short[] registers) {
    this.registers = registers;
  }

  /**
   * Create a BetaMinHash from the serialized representation returned by {@link #getBytes()}.
   */
  public static BetaMinHash fromBytes(byte[] bytes) {
    final int expectedNumBytes = 2 * NUM_REGISTERS; // 2 bytes per short
    if (bytes.length != expectedNumBytes) {
      throw new IllegalArgumentException(String.format(
          "Expected exactly %d bytes, but there are %d",
          expectedNumBytes,
          bytes.length));
    }

    final ByteBuffer buffer = ByteBuffer.wrap(bytes);
    final short[] registers = new short[NUM_REGISTERS];
    for (int i = 0; i < NUM_REGISTERS; i++) {
      registers[i] = buffer.getShort();
    }

    return wrapRegisters(registers);
  }

  static BetaMinHash deepCopyFromRegisters(short[] registers) {
    if (registers.length != NUM_REGISTERS) {
      throw new IllegalArgumentException(String.format(
          "Expected exactly %d registers, but there are %d",
          NUM_REGISTERS,
          registers.length));
    }

    final short[] registersCopy = new short[NUM_REGISTERS];
    System.arraycopy(registers, 0, registers, 0, NUM_REGISTERS);

    return wrapRegisters(registersCopy);
  }

  static BetaMinHash wrapRegisters(short[] registers) {
    return new BetaMinHash(registers);
  }

  @Override
  public long cardinality() {
    return BetaMinHashCardinalityGetter.cardinality(this);
  }

  @Override
  public boolean offer(byte[] val) {
    MetroHash128 hash = new MetroHash128(HASH_SEED).apply(ByteBuffer.wrap(val));
    ByteBuffer buf = ByteBuffer.allocate(16);
    hash.writeBigEndian(buf);
    return addHash(buf);
  }

//  @Override
//  public int sizeInBytes() {
//    return NUM_REGISTERS * Short.BYTES;
//  }
//
//  @Override
//  public byte[] getBytes() {
//    ByteBuffer byteBuffer = ByteBuffer.allocate(sizeInBytes());
//    for (short s : registers) {
//      byteBuffer.putShort(s);
//    }
//    return byteBuffer.array();
//  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BetaMinHash)) {
      return false;
    }
    BetaMinHash that = (BetaMinHash) o;
    return Arrays.equals(registers, that.registers);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(registers);
  }

  @Override
  public BetaMinHash deepCopy() {
    return deepCopyFromRegisters(this.registers);
  }

  /**
   * @param _128BitHash
   */
  private boolean addHash(ByteBuffer _128BitHash) {
    if (_128BitHash.array().length != 16) {
      throw new IllegalArgumentException("input hash should be 16 bytes");
    }

    long hashLeftHalf = _128BitHash.getLong(0);
    int registerIndex = (int) BitHelper.getLeftmostBits(hashLeftHalf, P);
    short leftmostOneBitPosition = BitHelper.getLeftmostOneBitPosition(_128BitHash.array(), P, Q);
    /* We take the rightmost bits as what's called h_hat3 in the paper. Note that his differs from
     * the diagram in the paper which draws a parallel to a mantissa in a floating point
     * representation, but still satisfies the criterion of serving as an independent hash function
     * by selecting a set of independent bits from a larger hash. This is slightly simpler to
     * implement. */
    short rBits = (short) BitHelper.getRightmostBits(_128BitHash.array(), R);

    short packedRegister = packIntoRegister(leftmostOneBitPosition, rBits);
    if (registers[registerIndex] < packedRegister) {
      registers[registerIndex] = packedRegister;
      return true;
    }

    return false;
  }

  /**
   * Creates a new tuple/register value for the LL-Beta by bit-packing the number of leading zeros
   * with the rightmost R bits.
   */
  private short packIntoRegister(short leftmostOnebitPosition, short rightmostRBits) {
    // Q is at most 6, which means that with R<=10, we should be able to store these two
    // numbers in the same register
    final int exponent = leftmostOnebitPosition << R;
    final int packedRegister = (exponent | rightmostRBits);
    return (short) packedRegister;
  }
}
