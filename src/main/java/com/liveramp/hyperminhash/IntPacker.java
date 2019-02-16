package com.liveramp.hyperminhash;

/**
 * Class used to pack an int with the position of the first zero in a bitstring and trailing bits.
 */
public class IntPacker {
  static int pack(int positionOfFirstOne, int mantissa, int r) {
    if (positionOfFirstOne > (1L << 6) - 1) {
      throw new IllegalArgumentException("position of first one must fit into 6 bits");
    }

    if (mantissa > (1L << 25) - 1) {
      throw new IllegalArgumentException("mantissa must fit into 25 bits");
    }

    return (positionOfFirstOne << r) | mantissa;
  }

  static int unpackMantissa(int register, int r) {
    // Just clear the exponent bits + any unused bits if 2^q + r < 32
    return (register << (Integer.SIZE - r)) >>> (Integer.SIZE - r);
  }

  static int unpackPositionOfFirstOne(int register, int r) {
    return Math.toIntExact(register >>> r);
  }
}
