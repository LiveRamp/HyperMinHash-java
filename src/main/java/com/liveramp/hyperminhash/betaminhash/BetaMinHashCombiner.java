package com.liveramp.hyperminhash.betaminhash;

import com.liveramp.hyperminhash.SketchCombiner;

public class BetaMinHashCombiner implements SketchCombiner<BetaMinHash> {

  private static final BetaMinHashCombiner INSTANCE = new BetaMinHashCombiner();
  private static final long serialVersionUID = 1L;

  public static BetaMinHashCombiner getInstance() {
    return INSTANCE;
  }

  private BetaMinHashCombiner() {
  }

  @Override
  public final BetaMinHash union(BetaMinHash... sketches) {
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

  @Override
  public long intersectionCardinality(BetaMinHash... sketches) {
    if (sketches.length == 0) {
      throw new IllegalArgumentException("Input sketches cannot be empty.");
    }

    return (long) (similarity(sketches) * union(sketches).cardinality());
  }

  @Override
  public double similarity(BetaMinHash... sketches) {
    // Algorithm 4 in HyperMinHash paper
    if (sketches.length == 0) {
      throw new IllegalArgumentException("Input sketches cannot be empty.");
    }

    if (sketches.length == 1) {
      return 1.0;
    }

    long C = 0;
    long N = 0;
    for (int i = 0; i < sketches[0].registers.length; i++) {
      if (sketches[0].registers[i] != 0) {
        boolean itemInIntersection = true;
        for (BetaMinHash sketch : sketches) {
          itemInIntersection =
              itemInIntersection && sketches[0].registers[i] == sketch.registers[i];
        }

        if (itemInIntersection) {
          C++;
        }
      }

      for (BetaMinHash sketch : sketches) {
        if (sketch.registers[i] != 0) {
          N++;
          break;
        }
      }
    }

    if (C == 0) {
      return 0;
    }

    double[] cardinalities = new double[sketches.length];
    int i = 0;
    for (BetaMinHash sk : sketches) {
      cardinalities[i++] = sk.cardinality();
    }

    int p = BetaMinHash.P;
    int q = BetaMinHash.Q;
    int r = BetaMinHash.R;
    double numExpectedCollisions = expectedCollision(p, q, r, cardinalities);

    if (C < numExpectedCollisions) {
      return 0;
    }

    return (C - numExpectedCollisions) / (double) N;
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
