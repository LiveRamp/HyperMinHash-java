package com.liveramp.hyperminhash.demo;

import com.liveramp.hyperminhash.HyperMinHash;
import com.liveramp.hyperminhash.HyperMinHashCombiner;
import com.liveramp.hyperminhash.IntersectionSketch;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DemoHyperMinHash {

  private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
  private static ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
  static FileWriter fw;
  private static int R = 57;
  private static int P = 20;

  public static void main(String[] args) throws InterruptedException, IOException {
    if (args.length > 0) {
      P = Integer.parseInt(args[0]);
    }

    if (args.length > 1) {
      R = Integer.parseInt(args[1]);
    }

    long t0 = System.currentTimeMillis();
    for (int k = 0; k < 10; k++) {
      long t = System.currentTimeMillis();
      HyperMinHash sketch = getSketch();
      System.out.println("creation time = " + (System.currentTimeMillis() - t));
      t = System.currentTimeMillis();
      for (int i = 0; i < 1_000_000; i++) {
        sketch.offer(longToByteArray(i));
      }
      System.out.println("addition time: " + (System.currentTimeMillis() - t));
    }
    System.out.println("total time: " + (System.currentTimeMillis() - t0));
//    try {
//      String outputFilePath = System.getProperty("user.dir") + "/data.csv";
//      new File(outputFilePath).delete();
//
////      fw = new FileWriter(outputFilePath, false);
//      runDemo();
//    } finally {
////      fw.close(); // FileWriter isn't autocloseable :)
//    }
  }

  private static void runDemo() throws InterruptedException, IOException {
    System.out.println("---- Begin Demo ----");
    runJaccardIndexEstimation(2);
    threadPool.shutdown();
    threadPool.awaitTermination(2l, TimeUnit.DAYS);
    System.out.println("---- Success ----");
  }

  private static long[] getSketchSizes(int numSketches, long size) {
    long[] out = new long[numSketches];
    Arrays.fill(out, size);
    return out;
  }

  private static void runJaccardIndexEstimation(int numSketchesToBuild) throws IOException {
    writeLine(getFormattedHeader(numSketchesToBuild));
    //
    for (double jaccardIndex = 0.0001; jaccardIndex <= 0.1; jaccardIndex *= 10) {
      // multiply jaccard going 0.1, 0.2, etc...
      for (int jaccardMultiplier = 1; jaccardMultiplier <= 10; jaccardMultiplier++) {

        // order size of the sets we're generating sketches for
        for (long order = 100_000; order <= 10_000_000; order *= 10) {

          // vary the union size, 100k, 200k --> 1m, 2m, etc...
          for (int unionSizeMultipler = 1; unionSizeMultipler <= 10; unionSizeMultipler++) {

            // do this experiment 5 times (this helps get a better spread of results since we have
            // some randomization in which elements we add to the sketches)
            for (int k = 0; k < 5; k++) {
              double jac = jaccardIndex * jaccardMultiplier;
              long unionSize = order * unionSizeMultipler;
              long intersectionSize = (long) (jac * unionSize);

              if (intersectionSize <= 0) {
                continue;
              }

              long sketchSize =
                  (long) ((unionSize - intersectionSize) / (double) numSketchesToBuild)
                      + intersectionSize;

              threadPool.submit(() -> runTestIteration(
                  intersectionSize,
                  getSketchSizes(numSketchesToBuild, sketchSize)));
            }
          }
        }
      }
    }
  }

  private static void runTestIteration(long exactIntersectionSize, long... sketchSizes) {
    final List<HyperMinHash> sketches = buildIntersectingSketches(
        exactIntersectionSize,
        sketchSizes);
    final HyperMinHashCombiner combiner = getCombiner();

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sketches.size(); i++) {
      // hack
      sb.append(sketchSizes[i] + ", ");
      sb.append(sketches.get(i).cardinality() + ", ");
    }

    long exactUnionSize = sum(sketchSizes) - ((sketchSizes.length - 1) * exactIntersectionSize);

    sb.append(exactUnionSize + ", ");
    sb.append(combiner.union(sketches).cardinality() + ", ");

    sb.append(exactIntersectionSize + ", ");
    sb.append(combiner.intersectionCardinality(sketches) + ", ");

    // jaccard
    double realJaccard = exactIntersectionSize / (double) exactUnionSize;

    sb.append(realJaccard + ", ");
    if (exactIntersectionSize / (double) exactUnionSize < 0) {
      System.out.println("oh boy");
      throw new RuntimeException();
    }

    double estimatedJaccard = combiner.similarity(sketches);
    sb.append(estimatedJaccard + ",");
    sb.append((100 * (realJaccard - estimatedJaccard)) / realJaccard);
    try {
      writeLine(sb.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static long sum(long... xs) {
    long sum = 0;
    for (long x : xs) {
      sum += x;
    }
    return sum;
  }


  /**
   * The sum of sketch sizes can't be larger than Long.MAX_VALUE
   */
  private static List<HyperMinHash> buildIntersectingSketches(
      long intersectionSize,
      long... sketchSizes) {
    final List<HyperMinHash> out = new ArrayList<>();
    for (int i = 0; i < sketchSizes.length; i++) {
      out.add(getSketch());
    }

    long counter = 0;
    // add disjoint items
    for (int i = 0; i < sketchSizes.length; i++) {
      for (int j = 0; j < (sketchSizes[i] - intersectionSize); j++) {
        out.get(i).offer(longToByteArray(counter++));
      }
    }

    // add intersecting items
    Random rng = new Random(System.currentTimeMillis() * 31);
    for (int i = 0; i < intersectionSize; i++) {
      // pick random numbers greater than counter to add to the sketches
      byte[] val = longToByteArray(counter + (rng.nextLong() % (Long.MAX_VALUE - counter)));
      for (IntersectionSketch sketch : out) {
        sketch.offer(val);
      }
    }
    return out;
  }

  /**
   * formatting helpers
   **/
  private static String getFormattedHeader(int numSketches) {
    // actual A | estimate A | actual B | estimate B | actual A U B | estimate A U B | actual A ? B | estimated A ? B
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= numSketches; i++) {
      sb.append("real_" + i + ",");
      sb.append("est_" + i + ",");
    }

    sb.append("real_");
    sb.append(getUnionHeader(numSketches));
    sb.append(",");

    sb.append("est_");
    sb.append(getUnionHeader(numSketches));
    sb.append(",");

    sb.append("real_");
    sb.append(getIntersectionHeader(numSketches));
    sb.append(",");

    sb.append("est_");
    sb.append(getIntersectionHeader(numSketches));
    sb.append(",");

    sb.append("real_jaccard,est_jaccard,jaccard_error");

    return sb.toString();
  }

  private static String getUnionHeader(int numSets) {
    return headerWithDelimiter(numSets, "\u222A");
  }

  private static String getIntersectionHeader(int numSets) {
    return headerWithDelimiter(numSets, "\u2229");
  }

  private static String headerWithDelimiter(int numSets, String delim) {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= numSets; i++) {
      sb.append(i + delim);
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  private static long plusMinusFivePercent(long x) {
    return x;
//    double pct = (rng.nextInt() % 5.0) / 100.0;
//    return (long) ((1 + pct) * x);
  }

  static HyperMinHashCombiner getCombiner() {
    return HyperMinHashCombiner.getInstance();
  }

  static HyperMinHash getSketch() {
    return new HyperMinHash(P, R);
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

  static synchronized void writeLine(String line) throws IOException {
    if (fw == null) {
      System.out.println(line);
    } else {
      fw.write(line);
      fw.write("\n");
    }
  }
}
