package com.liveramp.hyperminhash;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestBetaMinHash {
  @Test
  public void testZeroCardinality() {
    BetaMinHash sk = new BetaMinHash();
    assertEquals(0, sk.cardinality());
  }

  @Test
  public void testCardinality() {
    BetaMinHash sk = new BetaMinHash();
    int step = 10_000;
    Map<String, Boolean> unique = new HashMap<>();
    for (int i = 1; unique.size() < 1_000_000; i++) {
      String str = randomStringWithLength(randPositiveInt() % 32);
      sk.add(str.getBytes());
      unique.put(str, true);

      if (unique.size() % step == 0) {
        long exact = unique.size();
        long res = sk.cardinality();
        step *= 10;

        double pctError = 100 * getError(res, exact);
        assertTrue(pctError <= 2);
      }
    }
  }

  @Test
  public void testReallyLargeCardinality() {
    // This test succeeds up to 10 billion elements, but to keep build times sane it's kept at 1 bil.
    BetaMinHash sk = new BetaMinHash();
    int step = 10_000;
    for (long i = 1; i <= 100_000_000; i++) {

      sk.add((i + "").getBytes());


      if (i % step == 0) {
        long exact = i;
        long res = sk.cardinality();
        step *= 10;

        double pctError = 100 * getError(res, exact);
        assertTrue(pctError <= 3);
      }
    }
  }

  @Test
  public void testMerge() {
    BetaMinHash sk1 = new BetaMinHash();
    BetaMinHash sk2 = new BetaMinHash();

    Set<String> unique = new HashSet<>(3_500_000);

    for (int i = 1; i <= 1_500_000; i++) {
      String str = randomStringWithLength(randPositiveInt() % 32);
      sk1.add(str.getBytes());
      unique.add(str);

      str = randomStringWithLength(randPositiveInt() % 32);
      sk2.add(str.getBytes());
      unique.add(str);
    }

    BetaMinHash msk = BetaMinHash.merge(sk1, sk2);
    long exact = unique.size();
    long res = msk.cardinality();

    double pctError = 100 * getError(res, exact);
    assertTrue(pctError <= 2);
  }

  @Test
  public void testIntersection() {
    int iters = 20;
    int k = 1_000_000;

    for (int j = 1; j <= iters; j++) {

      BetaMinHash sk1 = new BetaMinHash();
      BetaMinHash sk2 = new BetaMinHash();

      double frac = j / (double)iters;

      for (int i = 0; i < k; i++) {
        String str = i + "";
        sk1.add(str.getBytes());
      }

      for (int i = (int)(frac * k); i < 2 * k; i++) {
        String str = i + "";
        sk2.add(str.getBytes());
      }
      //      buildIntersectingSketches(2 * k, k - (int)(frac * k), sk1, sk2);
      long expected = (long)(k - k * frac);
      long result = BetaMinHash.intersection(sk1, sk2);

      double pctError = 100 * getError(result, expected);
      assertTrue(String.format("Expected error ratio to be at most %s but found %f", 10, pctError),
          pctError <= 3);
    }
  }

  @Test
  public void testIntersectLargeSetWithSmallSet() {
    // Our other test uses string, which puts an upper bound on how big that test can be due to GC errors. With numbers
    // we can estimate cardinalities on the order of billions without running out of memory, unlike strings.
    BetaMinHash smallSketch = new BetaMinHash();
    long smallSetSize = 100_000;
    for (int i = 1; i < smallSetSize; i++) {
      smallSketch.add((i + "").getBytes());
    }

    BetaMinHash bigSketch = new BetaMinHash();
    long bigSetSize = 100_000_000;
    for (long i = 1; i <= bigSetSize; i++) {
      bigSketch.add((i + "").getBytes());
    }

    double expectedJaccardIndex = smallSetSize / (double)bigSetSize;
    long expectedIntersection = (long)(expectedJaccardIndex * bigSetSize);
    long actualIntersection = BetaMinHash.intersection(smallSketch, bigSketch);
    double pctError = 100 * getError(actualIntersection, expectedIntersection);

    // HyperMinHash performance starts decreasing as jaccard index becomes < 1%. On a Jaccard index this small
    // we should hope for <100% error.
    assertTrue(
        String.format("Percent error for a small jaccard index (%s) should be less than 100, but found %f", expectedJaccardIndex, pctError),
        pctError < 100
    );
  }

  @Test
  public void testManyWayIntersection() {

    long intersectionSize = 3000;
    long sketchSize = 10_000;
    // union size gets bigger, jaccard gets smaller
    for (int i = 0; i < 15; i++) {
      BetaMinHash sk1 = new BetaMinHash();
      BetaMinHash sk2 = new BetaMinHash();
      BetaMinHash sk3 = new BetaMinHash();
      BetaMinHash sk4 = new BetaMinHash();

      buildIntersectingSketches(sketchSize, intersectionSize, sk1, sk2, sk3, sk4);

      long expectedIntersection = intersectionSize;
      long actualIntersection = BetaMinHash.intersection(sk1, sk2, sk3);
      double pctError = 100 * getError(actualIntersection, expectedIntersection);
      assertTrue(
          String.format("Expected pctError to be <2, found %f. Expected: %d, Actual: %d", pctError, expectedIntersection, actualIntersection),
          pctError <= 5
      );

      intersectionSize <<= 1;
      sketchSize <<= 1;
    }
  }

  // builds equally sized sketches which share numSharedElements items
  private void buildIntersectingSketches(long sketchSize, long numSharedElements, BetaMinHash... sketches) {
    assert sketchSize >= numSharedElements;

    for (int i = 0; i < ((sketchSize - numSharedElements) * sketches.length + numSharedElements); i++) {
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

  @Test
  public void testNoIntersection() {
    BetaMinHash sk1 = new BetaMinHash();
    BetaMinHash sk2 = new BetaMinHash();

    for (int i = 0; i < 1000000; i++) {
      sk1.add((i + "").getBytes());
    }

    for (int i = 1000000; i < 2000000; i++) {
      sk2.add((i + "").getBytes());
    }

    long intersection = BetaMinHash.intersection(sk1, sk2);
    assertEquals(String.format("Expected zero intersection but found %s", intersection), 0, intersection);
  }

  /**
   * Helper Methods
   **/
  private static final String LETTER_BYTES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  private String randomStringWithLength(int n) {
    byte[] b = new byte[n];
    for (int i = 0; i < n; i++) {
      b[i] = (byte)LETTER_BYTES.charAt(randPositiveInt() % LETTER_BYTES.length());
    }
    return Hex.encodeHexString(b);
  }

  private double getError(long result, long expected) {
    if (result == expected) {
      return 0;
    }

    if (expected == 0) {
      return result;
    }

    long delta = Math.abs(result - expected);
    return delta / (double)expected;
  }

  private Random rng = new Random();

  int randPositiveInt() {
    return Math.abs(rng.nextInt());
  }
}
