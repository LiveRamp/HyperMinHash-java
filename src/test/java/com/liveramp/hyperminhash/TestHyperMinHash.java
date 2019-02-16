package com.liveramp.hyperminhash;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestHyperMinHash {

  @Test
  public void testZeroCardinality() {
    HyperMinHash sk = new HyperMinHash(14, 35);
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
            new HyperMinHash(14, 35),
            maxUniqueElements,
            minTestCardinality,
            random,
            pctErr)
    );
  }

  @Test
  public void testUnion() {
    final HyperMinHashCombiner combiner = HyperMinHashCombiner.getInstance();
    final int elementsPerSketch = 1_500_000;
    final double pctErr = 3.0;
    RandomTestRunner.runRandomizedTest(
        3,
        (random) -> CommonTests.testUnion(
            new HyperMinHash(14, 35),
            new HyperMinHash(14, 35),
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
        new HyperMinHash(14, 35),
        HyperMinHashCombiner.getInstance(),
        overlapSlices,
        numElementsLeftSketch,
        pctError
    );

  }

  @Test
  public void testIntersectionWithSmallJaccard() {
    // .001 Jaccard
    int smallSetSize = 1_000;
    int bigSetSize = 1_000_000;
    for (int i = 0; i < 3; i++) {
      final double maxPctErr = 20.0;
      CommonTests.testIntersectLargeSetWithSmall(
          new HyperMinHash(14, 57),
          HyperMinHashCombiner.getInstance(),
          smallSetSize,
          bigSetSize,
          maxPctErr
      );
      smallSetSize *= 10;
      bigSetSize *= 10;
    }
  }

  @Test
  public void testIntersectionWithExtremelySmallJaccard() {
    // .0001 Jaccard
    int smallSetSize = 1_000;
    int bigSetSize = 10_000_000;
    for (int i = 0; i < 2; i++) {
      final double maxPctErr = 20.0;
      CommonTests.testIntersectLargeSetWithSmall(
          // make a beefy sketch since we're asking for a lot here
          new HyperMinHash(21, 57),
          HyperMinHashCombiner.getInstance(),
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
        new HyperMinHash(14, 35),
        HyperMinHashCombiner.getInstance(),
        initialSketchSize,
        initialIntersectionSize,
        numIter
    );
  }
}
