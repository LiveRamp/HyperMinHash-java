package com.liveramp.hyperminhash;

import java.util.Collection;

public class BetaMinHashCombiner implements SketchCombiner<BetaMinHash> {

  private static final BetaMinHashCombiner INSTANCE = new BetaMinHashCombiner();
  private static final long serialVersionUID = 1L;

  private BetaMinHashCombiner() {
  }

  public static BetaMinHashCombiner getInstance() {
    return INSTANCE;
  }

  @Override
  public final BetaMinHash union(Collection<BetaMinHash> sketches) {
    if (sketches.isEmpty()) {
      throw new IllegalArgumentException("Input sketches cannot be empty.");
    }

    final BetaMinHash firstSketch = sketches.stream()
        .findFirst()
        .get();
    if (sketches.size() == 1) {
      return firstSketch.deepCopy();
    }

    final int numRegisters = firstSketch.registers.length;
    final BetaMinHash mergedSketch = firstSketch.deepCopy();
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

  @Override
  public long intersectionCardinality(Collection<BetaMinHash> sketches) {
    if (sketches.size() == 0) {
      throw new IllegalArgumentException("Input sketches cannot be empty.");
    }

    return (long) (similarity(sketches) * union(sketches).cardinality());
  }

  @Override
  public double similarity(Collection<BetaMinHash> sketches) {
    // Algorithm 4 in HyperMinHash paper
    if (sketches.size() == 0) {
      throw new IllegalArgumentException("Input sketches cannot be empty.");
    }

    if (sketches.size() == 1) {
      return 1.0;
    }

    long c = 0;
    long n = 0;
    final BetaMinHash firstSketch = sketches.stream()
        .findFirst()
        .get();
    for (int i = 0; i < firstSketch.registers.length; i++) {
      if (firstSketch.registers[i] != 0) {
        boolean itemInIntersection = true;
        for (BetaMinHash sketch : sketches) {
          itemInIntersection =
              itemInIntersection && firstSketch.registers[i] == sketch.registers[i];
        }

        if (itemInIntersection) {
          c++;
        }
      }

      for (BetaMinHash sketch : sketches) {
        if (sketch.registers[i] != 0) {
          n++;
          break;
        }
      }
    }

    if (c == 0) {
      return 0;
    }

    double[] cardinalities = new double[sketches.size()];
    int i = 0;
    for (BetaMinHash sk : sketches) {
      cardinalities[i++] = sk.cardinality();
    }

    int p = BetaMinHash.P;
    int q = BetaMinHash.Q;
    int r = BetaMinHash.R;
    double numExpectedCollisions = expectedCollision(p, q, r, cardinalities);

    if (c < numExpectedCollisions) {
      return 0;
    }

    return (c - numExpectedCollisions) / (double) n;
  }

  private static double expectedCollision(int p, int q, int r, double... cardinalities) {
    final int _2q = 1 << q;
    final int _2r = 1 << r;

    double x = 0;
    double b1 = 0;
    double b2 = 0;

    for (int i = 1; i <= _2q; i++) {
      for (int j = 1; j <= _2r; j++) {
        if (i != _2q) {
          double den = Math.pow(2, p + r + i);
          b1 = (_2r + j) / den;
          b2 = (_2r + j + 1) / den;
        } else {
          double den = Math.pow(2, p + r + i - 1);
          b1 = j / den;
          b2 = (j + 1) / den;
        }

        double product = 1;
        for (double cardinality : cardinalities) {
          product *= Math.pow(1 - b2, cardinality) - Math.pow(1 - b1, cardinality);
        }

        x += product;
      }
    }
    return x * Math.pow(2, p);
  }

  private static short max(short a, short b) {
    return a > b ? a : b;
  }
}
