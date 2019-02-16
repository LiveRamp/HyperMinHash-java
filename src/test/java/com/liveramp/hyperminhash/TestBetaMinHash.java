package com.liveramp.hyperminhash;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestBetaMinHash {

  @Test
  public void testZeroCardinality() {
    BetaMinHash sk = new BetaMinHash();
    assertEquals(0, sk.cardinality());
  }

  @Test
  public void testCardinality() {

    final int maxUniqueElements = 10_000_000;
    final int minTestCardinality = 10_000;
    final double pctErr = 3.0;
    RandomTestRunner.runRandomizedTest(
        3,
        (random) -> CommonTests.testCardinality(
            new BetaMinHash(),
            maxUniqueElements,
            minTestCardinality,
            random,
            pctErr)
    );
  }

  @Test
  public void testUnion() {
    final BetaMinHashCombiner combiner = BetaMinHashCombiner.getInstance();
    final int elementsPerSketch = 1_500_000;
    final double pctErr = 3.0;
    RandomTestRunner.runRandomizedTest(
        3,
        (random) -> CommonTests.testUnion(
            new BetaMinHash(),
            new BetaMinHash(),
            combiner,
            elementsPerSketch,
            pctErr,
            random
        )
    );
  }

  @Test
  public void testIntersectionCardinality() {
    final int overlapSlices = 20;
    final int numElementsLeftSketch = 1_000_000;
    final double pctError = 5.0;
    CommonTests.testIntersection(
        new BetaMinHash(),
        BetaMinHashCombiner.getInstance(),
        overlapSlices,
        numElementsLeftSketch,
        pctError
    );

  }

  @Test

  public void testIntersectLargeSetWithSmallSet() {
    int smallSetSize = 1_000;
    int bigSetSize = 1_000_000;
    for (int i = 0; i < 3; i++) {
      final double maxPctErr = 22.0;
      CommonTests.testIntersectLargeSetWithSmall(
          new BetaMinHash(),
          BetaMinHashCombiner.getInstance(),
          smallSetSize,
          bigSetSize,
          maxPctErr
      );
      smallSetSize *= 10;
      bigSetSize *= 10;
    }
  }

  @Test
  public void testMultiwayIntersection() {
    final int initialIntersectionSize = 3000;
    final int initialSketchSize = 10_000;
    final int numIter = 5;
    CommonTests.testMultiwayIntersection(
        new BetaMinHash(),
        BetaMinHashCombiner.getInstance(),
        initialSketchSize,
        initialIntersectionSize,
        numIter
    );
  }
}
