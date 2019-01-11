package com.liveramp.hyperminhash;


import java.util.Arrays;

public class HyperMinHash implements IntersectionSketch<HyperMinHash> {

  private static final int HASH_SEED = 1738;

  // used in serialization
  static final byte VERSION = 1;

  /* There are 2^p registers. Per the HyperMinHash algorithm, hashes are bucketed based on the value
   * of their bitstring's first p bits. The r least significant bits in the bitstring in are stored
   * as the r least significant bits in the register.
   * The number of leading zeroes in positions 2^p through 2^p + 2^q - 1 in the bitstring is stored
   * in the registers bits that are the q + 1 next least significant bits after the r least
   * significant bits i.e. number of leading zeroes is stored in bits r through r + q - 1 of the
   * long.
   */
  final long[] registers;
  final int p; // must be at least 4
  // This is 2^q + 1 in the HMH paper. We use this to represent the space that we're searching for a
  // leading zero.
  final int numZeroSearchBits;
  final int r;

  /**
   * @param p HLL precision parameter
   * @param r Number of MinHash bits to keep
   */
  public HyperMinHash(int p, int r) {
    this(p, r, null);
  }

  static HyperMinHash wrapRegisters(int p, int r, long[] registers) {
    return new HyperMinHash(p, r, registers);
  }

  private HyperMinHash(int p, int r, long[] registers) {
    // Ensure that the number of registers isn't larger than the largest array java can hold in
    // memory biggest java array can be of size Integer.MAX_VALUE
    if (!(p >= 4 && p < 31)) {
      throw new IllegalArgumentException(
          "precision (p) must be between 4 (inclusive) and 31 (exclusive)."
      );
    }

    // Ensure that we can pack the number of leading zeroes and the least significant r bits from
    // the hash bitstring into a long "register."
    if (!(r > 1 && r < 58)) {
      throw new IllegalArgumentException(
          "number of bits to take for minhash (r) must be between 1 and 58.");
    }

    this.p = p;
    this.numZeroSearchBits = Long.SIZE - p;
    this.r = r;
    if (registers == null) {
      this.registers = new long[1 << p];
    } else {
      this.registers = registers;
    }
  }

  @Override
  public long cardinality() {
    return HmhCardinalityEstimator.estimateCardinality(registers, p, r);
  }

  @Override
  public boolean offer(byte[] bytes) {
//    MetroHash128 hash = MetroHash.hash128(HASH_SEED, bytes);
    final long[] hash = Murmur3.hash128(bytes);

    // the left half of the hash is used for HLL
    final long hllHash = hash[0];

    // Unsafely cast to int because we assume numZeroSearchBits > 32
    final int registerIndex = (int) (hllHash >>> numZeroSearchBits);

    // zero out leftmost p bits and find position of leftmost one
    // We add a one to the right of the zero search space just in case the entire space is zeros
    final long zeroSearchSpace = (hllHash << p) | (long) (1 << (p - 1));
    final int leftmostOnePosition = Long.numberOfLeadingZeros(zeroSearchSpace) + 1;

    // the right half of the hash is used for min hash
    final long hmhHash = hash[1];
    // We take the leftmost R bits as the minHash bits
    final long minHashBits = hmhHash >>> (Long.SIZE - r);

    final long incomingRegister = LongPacker.pack(leftmostOnePosition, minHashBits, r);

    if (shouldReplace(registers[registerIndex], incomingRegister, r)) {
      registers[registerIndex] = incomingRegister;
      return true;
    }

    return false;
  }

  @Override
  public HyperMinHash deepCopy() {
    return new HyperMinHash(p, r, Arrays.copyOf(registers, registers.length));
  }

  // we could replace this with a single comparison of the registers but it'd be less clear
  // it could be swapped if it meaningfully affects performance
  static boolean shouldReplace(long currentRegister, long incomingRegister, int r) {
    int currentLeadingOnePosition = LongPacker.unpackPositionOfFirstOne(currentRegister, r);
    int incomingLeadingOnePosition = LongPacker.unpackPositionOfFirstOne(incomingRegister, r);

    if (currentLeadingOnePosition < incomingLeadingOnePosition) {
      return true;
    } else if (currentLeadingOnePosition == incomingLeadingOnePosition) {
      long currentMantissa = LongPacker.unpackMantissa(currentRegister, r);
      long incomingMantissa = LongPacker.unpackMantissa(incomingRegister, r);

      if (currentMantissa > incomingMantissa) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    HyperMinHash that = (HyperMinHash) o;

    if (p != that.p) {
      return false;
    }
    if (numZeroSearchBits != that.numZeroSearchBits) {
      return false;
    }
    if (r != that.r) {
      return false;
    }
    return Arrays.equals(registers, that.registers);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(registers);
    result = 31 * result + p;
    result = 31 * result + numZeroSearchBits;
    result = 31 * result + r;
    return result;
  }

  @Override
  public String toString() {
    return "HyperMinHash{" +
        "registers=" + Arrays.toString(registers) +
        ", p=" + p +
        ", numZeroSearchBits=" + numZeroSearchBits +
        ", r=" + r +
        '}';
  }
}
