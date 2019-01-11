package com.liveramp.hyperminhash;

import org.junit.Assert;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class CommonTests {

  private static double getError(long result, long expected) {
    if (result == expected) {
      return 0;
    }

    if (expected == 0) {
      return result;
    }

    long delta = Math.abs(result - expected);
    return delta / (double) expected;
  }

  static <Sketch extends IntersectionSketch<Sketch>> void testCardinality(
      final Sketch sk,
      final int maxUniqueElements,
      final int minTestCardinality,
      final Random random,
      final double maxPctErr) {
    final Set<ByteBuffer> unique = new HashSet<>();
    int assertionCheckpoint = minTestCardinality;

    while (unique.size() < maxUniqueElements) {
      byte[] bytes = randomByteArray(random);
      sk.offer(bytes);
      unique.add(ByteBuffer.wrap(bytes));

      if (unique.size() % assertionCheckpoint == 0) {
        long exact = unique.size();
        long res = sk.cardinality();
        assertionCheckpoint *= 10;

        double pctError = 100 * getError(res, exact);

        Assert.assertTrue(
            String.format("Got %f%% error, but expected at most %f%%", pctError, maxPctErr),
            pctError <= maxPctErr);
      }
    }
  }

  static <Sketch extends IntersectionSketch<Sketch>> void testUnion(
      final Sketch sk1,
      final Sketch sk2,
      final SketchCombiner<Sketch> combiner,
      final int elementsPerSketch,
      final double maxPctError,
      final Random random) {
    final Set<ByteBuffer> unique = new HashSet<>(2 * elementsPerSketch);

    for (int i = 1; i <= elementsPerSketch; i++) {
      byte[] bytes = randomByteArray(random);
      sk1.offer(bytes);
      unique.add(ByteBuffer.wrap(bytes));

      bytes = randomByteArray(random);
      sk2.offer(bytes);
      unique.add(ByteBuffer.wrap(bytes));
    }

    final Sketch msk = combiner.union(Arrays.asList(sk1, sk2));
    final long exact = unique.size();
    final long res = msk.cardinality();

    final double pctError = 100 * getError(res, exact);
    Assert.assertTrue(
        String.format("Got %f%% error, but expected at most %f%%", pctError, maxPctError),
        pctError <= maxPctError
    );
  }

  static <Sketch extends IntersectionSketch<Sketch>> void testIntersection(
      final Sketch emptySketch,
      final SketchCombiner<Sketch> combiner,
      final int overlapSlices,
      final int elementsInLeftSketches,
      final double maxPctError) {
    for (int j = 1; j < overlapSlices; j++) {

      final Sketch sk1 = emptySketch.deepCopy();
      final Sketch sk2 = emptySketch.deepCopy();

      final double frac = j / (double) overlapSlices;

      for (int i = 0; i < elementsInLeftSketches; i++) {
        sk1.offer(intToByteArray(i));
      }

      for (int i = (int) (frac * elementsInLeftSketches); i < 2 * elementsInLeftSketches; i++) {
        sk2.offer(intToByteArray(i));
      }

      final long expected = (long) (elementsInLeftSketches - elementsInLeftSketches * frac);
      final long result = combiner.intersectionCardinality(Arrays.asList(sk1, sk2));

      final double pctError = 100 * getError(result, expected);
      Assert.assertTrue(
          String.format(
              "Expected error ratio to be at most %s but found %f",
              maxPctError,
              pctError),
          pctError <= maxPctError);
    }
  }

  static <Sketch extends IntersectionSketch<Sketch>> void testIntersectLargeSetWithSmall(
      final Sketch emptySketch,
      final SketchCombiner<Sketch> combiner,
      final int smallSetSize,
      final int bigSetSize,
      final double maxPctErr) {

    final Sketch smallSketch = emptySketch.deepCopy();
    for (int i = 1; i < smallSetSize; i++) {
      smallSketch.offer(longToByteArray((long) i));
    }

    final Sketch bigSketch = emptySketch.deepCopy();
    for (long i = 1; i <= bigSetSize; i++) {
      bigSketch.offer(longToByteArray(i));
    }

    final double expectedJaccardIndex = smallSetSize / (double) bigSetSize;
    final long expectedIntersection = (long) (expectedJaccardIndex * bigSetSize);
    final long actualIntersection = combiner.intersectionCardinality(
        Arrays.asList(smallSketch, bigSketch)
    );
    final double pctError = 100 * getError(actualIntersection, expectedIntersection);

    Assert.assertTrue(
        String.format(
            "Percent error for a small jaccard index (%s) should be less than %f, but found %f",
            expectedJaccardIndex, maxPctErr, pctError),
        pctError < maxPctErr
    );
  }

  static <Sketch extends IntersectionSketch<Sketch>> void testMultiwayIntersection(
      final Sketch emptySketch,
      final SketchCombiner<Sketch> combiner,
      final int initialSketchSize,
      final int initialIntersectionSize,
      final int numIter) {

    long sketchSize = initialSketchSize;
    long intersectionSize = initialIntersectionSize;
    // union size gets bigger, jaccard gets smaller
    for (int i = 0; i < numIter; i++) {
      Sketch sk1 = emptySketch.deepCopy();
      Sketch sk2 = emptySketch.deepCopy();
      Sketch sk3 = emptySketch.deepCopy();
      Sketch sk4 = emptySketch.deepCopy();

      buildIntersectingSketches(sketchSize, intersectionSize, sk1, sk2, sk3, sk4);

      long expectedIntersection = intersectionSize;
      long actualIntersection = combiner.intersectionCardinality(Arrays.asList(sk1, sk2, sk3));
      double pctError = 100 * getError(actualIntersection, expectedIntersection);
      Assert.assertTrue(
          String.format("Expected pctError to be <2, found %f. Expected: %d, Actual: %d", pctError,
              expectedIntersection, actualIntersection),
          pctError <= 5
      );

      intersectionSize <<= 1;
      sketchSize <<= 1;
    }
  }

  // builds equally sized sketches which share numSharedElements items
  private static <Sketch extends IntersectionSketch<Sketch>> void buildIntersectingSketches(
      long sketchSize,
      long numSharedElements,
      Sketch... sketches) {
    assert sketchSize >= numSharedElements;

    final long numIter = ((sketchSize - numSharedElements) * sketches.length + numSharedElements);
    for (int i = 0; i < numIter; i++) {
      byte[] val = intToByteArray(i);
      if (i < numSharedElements) {
        for (Sketch sketch : sketches) {
          sketch.offer(val);
        }
      } else {
        sketches[i % sketches.length].offer(val);
      }
    }
  }

  private static byte[] randomByteArray(final Random random) {
    final int n = Math.abs(random.nextInt()) % 32;
    byte[] b = new byte[n];
    random.nextBytes(b);
    return b;
  }

  public static final byte[] intToByteArray(int value) {
    return new byte[]{
        (byte) (value >>> 24),
        (byte) (value >>> 16),
        (byte) (value >>> 8),
        (byte) value};
  }

  public static final byte[] longToByteArray(long value) {
    return new byte[]{
        (byte) (value >>> 56),
        (byte) (value >>> 48),
        (byte) (value >>> 40),
        (byte) (value >>> 32),
        (byte) (value >>> 24),
        (byte) (value >>> 16),
        (byte) (value >>> 8),
        (byte) value
    };
  }

  private static int randPositiveInt(final Random random) {
    return Math.abs(random.nextInt());
  }
}
