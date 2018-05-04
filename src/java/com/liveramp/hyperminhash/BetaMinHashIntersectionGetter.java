package com.liveramp.hyperminhash;

class BetaMinHashIntersectionGetter {
  public static long getIntersection(BetaMinHash... sketches) {
    if (sketches.length == 0) {
      throw new IllegalArgumentException("Input sketches cannot be empty.");
    }
    double similarity = BetaMinHash.similarity(sketches);
    long unionSize = BetaMinHash.union(sketches);
    return (long)(similarity * unionSize);
  }
}
