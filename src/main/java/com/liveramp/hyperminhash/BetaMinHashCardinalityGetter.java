package com.liveramp.hyperminhash;

class BetaMinHashCardinalityGetter {

  static long cardinality(BetaMinHash sketch) {
    // Formula (2) in Qin et al.
    SumAndZeros saz = getRegisterSumAndZeros(sketch);
    double sum = saz.sum;
    double zeros = saz.zeros;
    double mHat = (double) BetaMinHash.NUM_REGISTERS;
    double alpha = alpha(BetaMinHash.NUM_REGISTERS);
    return (long) (alpha * mHat * (mHat - zeros) / (beta(zeros) + sum));
  }

  private static SumAndZeros getRegisterSumAndZeros(BetaMinHash sketch) {
    double sum = 0, uninitializedRegisters = 0;
    for (short register : sketch.registers) {
      byte leadingZeros = leadingZeros(register, BetaMinHash.Q);
      if (leadingZeros == 0) {
        uninitializedRegisters++;
      }
      sum += 1 / Math.pow(2, (double) leadingZeros);
    }
    return new SumAndZeros(sum, uninitializedRegisters);
  }

  private static byte leadingZeros(short register, int q) {
    return (byte) (register >>> (Short.SIZE - q));
  }

  /**
   * Alpha parameter as shown in Figure 3 of the Hyperloglog paper by Flajolet, Philippe, et al.
   * found here: http://algo.inria.fr/flajolet/Publications/FlFuGaMe07.pdf.
   *
   * @param hllSize number of registers used in the HLL.
   * @return the alpha value
   */
  private static double alpha(int hllSize) {
    switch (hllSize) {
      case 16:
        return 0.673;
      case 32:
        return 0.697;
      case 64:
        return 0.709;
      default:
        return 0.7213 / (1 + 1.079 / (double) hllSize);
    }
  }

  /**
   * @param zeros The number of leading 0s in the first (2^Q)-1 bits
   * @return the beta value
   */
  private static double beta(double zeros) {
    double log = Math.log(zeros + 1);
    return -0.370393911 * zeros +
        0.070471823 * log +
        0.17393686 * Math.pow(log, 2) +
        0.16339839 * Math.pow(log, 3) +
        -0.09237745 * Math.pow(log, 4) +
        0.03738027 * Math.pow(log, 5) +
        -0.005384159 * Math.pow(log, 6) +
        0.00042419 * Math.pow(log, 7);
  }

  private static class SumAndZeros {

    final double sum;
    final double zeros;

    /**
     * @param sum
     * @param zeros
     */
    SumAndZeros(double sum, double zeros) {
      this.zeros = zeros;
      this.sum = sum;
    }
  }

}
