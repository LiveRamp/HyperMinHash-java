package com.liveramp.hyperminhash;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class TestBetaMinHash {

  @Test
  public void testZeroCardinality() {
    BetaMinHash sk = new BetaMinHash();
    assertEquals(0, sk.cardinality());
  }

  @Test
  public void testCardinality() {
<<<<<<< HEAD:src/test/java/com/liveramp/hyperminhash/TestBetaMinHash.java
    BetaMinHash sk = new BetaMinHash();
    int step = 10_000;
    Map<String, Boolean> unique = new HashMap<>();
    for (int i = 1; unique.size() <= 1_000_000; i++) {
      String str = randomStringWithLength(randPositiveInt() % 32);
      sk.add(str.getBytes());
      unique.put(str, true);

      if (unique.size() % step == 0) {
        long exact = unique.size();
        long res = sk.cardinality();
        step *= 10;

        double pctError = 100 * getError(res, exact);
        assertTrue(pctError <= 2.5);
      }
    }
=======
    final int maxUniqueElements = 10_000_000;
    final int minTestCardinality = 10_000;
    final double pctErr = 2.0;
    RandomTestRunner.runRandomizedTest(
        3,
        (random) -> CommonTests.testCardinality(
            new BetaMinHash(),
            maxUniqueElements,
            minTestCardinality,
            random,
            pctErr)
    );
>>>>>>> d3e26e8c42647e7aae2a4e91eb6941d3ad206ab0:test/main/java/com/liveramp/hyperminhash/TestBetaMinHash.java
  }

  @Test
  public void testUnion() {
    final BetaMinHashCombiner combiner = BetaMinHashCombiner.getInstance();
    final int elementsPerSketch = 1_500_000;
    final double pctErr = 2.0;
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
<<<<<<< HEAD:src/test/java/com/liveramp/hyperminhash/TestBetaMinHash.java
  public void testIntersection() {
    int iters = 20;
    int k = 1_000_000;

    for (int j = 1; j < iters; j++) {

      BetaMinHash sk1 = new BetaMinHash();
      BetaMinHash sk2 = new BetaMinHash();

      double frac = j / (double) iters;

      for (int i = 0; i < k; i++) {
        String str = i + "";
        sk1.add(str.getBytes());
      }

      for (int i = (int) (frac * k); i < 2 * k; i++) {
        String str = i + "";
        sk2.add(str.getBytes());
      }

      long expected = (long) (k - k * frac);
      long result = BetaMinHash.intersection(sk1, sk2);

      double pctError = 100 * getError(result, expected);
      double expectedPctError = 5.0;
      assertTrue(
          String.format("Expected error ratio to be at most %s but found %f", expectedPctError,
              pctError),
          pctError <= expectedPctError);
    }
=======
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

>>>>>>> d3e26e8c42647e7aae2a4e91eb6941d3ad206ab0:test/main/java/com/liveramp/hyperminhash/TestBetaMinHash.java
  }

  @Test
<<<<<<< HEAD:src/test/java/com/liveramp/hyperminhash/TestBetaMinHash.java
  public void testManyWayIntersection() {

    long intersectionSize = 3000;
    long sketchSize = 10_000;
    // union size gets bigger, jaccard gets smaller
    for (int i = 0; i < 5; i++) {
      BetaMinHash sk1 = new BetaMinHash();
      BetaMinHash sk2 = new BetaMinHash();
      BetaMinHash sk3 = new BetaMinHash();
      BetaMinHash sk4 = new BetaMinHash();

      buildIntersectingSketches(sketchSize, intersectionSize, sk1, sk2, sk3, sk4);

      long expectedIntersection = intersectionSize;
      long actualIntersection = BetaMinHash.intersection(sk1, sk2, sk3);
      double pctError = 100 * getError(actualIntersection, expectedIntersection);
      assertTrue(
          String.format("Expected pctError to be <2, found %f. Expected: %d, Actual: %d", pctError,
              expectedIntersection, actualIntersection),
          pctError <= 5
      );

      intersectionSize <<= 1;
      sketchSize <<= 1;
    }
  }

  // builds equally sized sketches which share numSharedElements items
  private void buildIntersectingSketches(
      long sketchSize, long numSharedElements, BetaMinHash... sketches) {
    assert sketchSize >= numSharedElements;

    for (int i = 0; i < ((sketchSize - numSharedElements) * sketches.length + numSharedElements);
        i++) {
      byte[] val = (i + "").getBytes();
      if (i < numSharedElements) {
        for (BetaMinHash sketch : sketches) {
          sketch.add(val);
        }
      } else {
        sketches[i % sketches.length].add(val);
      }
    }
  }

  /**
   * Helper Methods
   **/
  private Random rng = new Random();

  private String randomStringWithLength(int n) {
    byte[] b = new byte[n];
    rng.nextBytes(b);
    return new String(b, StandardCharsets.US_ASCII);
=======
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

>>>>>>> d3e26e8c42647e7aae2a4e91eb6941d3ad206ab0:test/main/java/com/liveramp/hyperminhash/TestBetaMinHash.java
  }

  @Test
  public void testToFromBytes() {
    final BetaMinHash original = new BetaMinHash();
    original.offer("test data".getBytes());

//    final byte[] serialized = original.getBytes();
//    final BetaMinHash deSerialized = BetaMinHash.fromBytes(serialized);

<<<<<<< HEAD:src/test/java/com/liveramp/hyperminhash/TestBetaMinHash.java
    long delta = Math.abs(result - expected);
    return delta / (double) expected;
  }

  int randPositiveInt() {
    return Math.abs(rng.nextInt());
  }
=======
//    Assert.assertEquals(original, deSerialized);
  }


>>>>>>> d3e26e8c42647e7aae2a4e91eb6941d3ad206ab0:test/main/java/com/liveramp/hyperminhash/TestBetaMinHash.java
}
