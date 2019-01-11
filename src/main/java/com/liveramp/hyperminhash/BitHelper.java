package com.liveramp.hyperminhash;

import java.util.Arrays;

class BitHelper {

  /**
   * @return the leftmost (most significant) {@code numBits} bits in {@code value} in int as the
   *     {@code numBits} least significant bits in that int.
   */
  static long getLeftmostBits(long value, int numBits) {
    if (numBits >= Long.SIZE) {
      throw new IllegalArgumentException(String.format("numBits must be < %d", Integer.SIZE));
    }

    return (value >>> (Long.SIZE - numBits));
  }

  static boolean[] longToBits(long num) {
    final boolean[] bits = new boolean[Long.SIZE];

    for (int i = 0; i < Long.SIZE; i++) {
      bits[i] = (num & 1) == 1;
      num >>>= 1;
    }

    return bits;
  }

  /**
   * @return the position of the leftmost one-bit among the 2^q bits _after_ the first p bits.
   *
   *     Bit position is 0-indexed within the 2^q bits. e.g. if p = 2 and q = 2, then the value
   *     returned for the hash value with bit string "100010..." would be 2.
   */

  static short getLeftmostOneBitPosition(byte[] hash, int p, int q) {
    boolean[] bits = BitHelper.getBitsAsBooleans(hash);
    return getLeftmostOneBitPosition(bits, p, q);
  }

  /**
   * @return The rightmost {@code r} bits from {@code hash}.
   */
  static long getRightmostBits(byte[] hash, int r) {
    final boolean[] bits = BitHelper.getBitsAsBooleans(hash);
    return BitHelper.bitsToLong(Arrays.copyOfRange(bits, bits.length - r, bits.length));
  }

  /**
   * @return the long represented by {@code bits}, interpretted in little-endian order.
   */
  static long bitsToLong(boolean[] bits) {
    if (bits.length > Long.SIZE) {
      throw new IllegalArgumentException(
          "num bits is greater than size of long. Num bits: " + bits.length);
    }

    long out = 0;
    for (int i = bits.length - 1; i >= 0; i--) {
      if (i != 0) {
        out <<= 1;
      }
      out += bits[i] ? 1 : 0;
    }
    return out;
  }

  private static short getLeftmostOneBitPosition(boolean[] bits, int p, int q) {
    int _2toTheQ = (1 << q);

    int offset = p + 1;
    for (int i = offset; i < _2toTheQ + offset; i++) {
      if (bits[i]) {
        return (short) (i + 1 - offset);
      }
    }
    return (short) (_2toTheQ + 1);
  }

  private static byte[] getBytes(boolean[] bitString) {
    int byteLen = (int) Math.ceil(bitString.length / 8.0);
    byte[] bytes = new byte[byteLen];
    int exp = 0;
    int byteIndex = bytes.length - 1;
    for (int i = bitString.length - 1; i >= 0; i--) {
      if (bitString[i]) {
        bytes[byteIndex] += 1 << exp;
      }
      exp++;
      if (exp > 7) {
        byteIndex--;
        exp = 0;
      }
    }
    return bytes;
  }

  private static boolean[] getBitsAsBooleans(byte[] bytes) {
    boolean[] output = new boolean[bytes.length * 8];

    int mask = 1 << 7;
    int i = 0;
    for (byte b : bytes) {
      for (int j = 0; j < 8; j++) {
        int shiftedMask = mask >>> j;
        int boolIndex = (i * Byte.SIZE) + j;

        output[boolIndex] = ((b & shiftedMask) << j) == mask;
      }
      i++;
    }
    return output;
  }


}
