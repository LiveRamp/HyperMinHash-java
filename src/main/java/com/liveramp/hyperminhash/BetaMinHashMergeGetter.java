package com.liveramp.hyperminhash;

class BetaMinHashMergeGetter {
  static BetaMinHash merge(BetaMinHash... sketches) {
    if (sketches.length == 0) {
      throw new IllegalArgumentException("Input sketches cannot be empty.");
    }

    if (sketches.length == 1) {
      return sketches[0];
    }

    int numRegisters = sketches[0].registers.length;

    BetaMinHash mergedSketch = new BetaMinHash(sketches[0]);
    for (int i = 0; i < numRegisters; i++) {
      for (BetaMinHash sketch : sketches) {
        mergedSketch.registers[i] = max(
            mergedSketch.registers[i],
            sketch.registers[i]
        );
      }
    }

    return mergedSketch;
  }

  private static short max(short a, short b) {
    return a > b ? a : b;
  }
}
