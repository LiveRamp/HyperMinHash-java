package com.liveramp.hyperminhash.demo;

import com.liveramp.hyperminhash.betaminhash.BetaMinHashCombiner;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.liveramp.hyperminhash.betaminhash.BetaMinHash;

public class DemoBetaMinHash {
  private static final int NUM_THREADS = 15;
  private static ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
  private static Random rng = new Random();

  public static void main(String[] args) throws InterruptedException {
    runDemo();
  }

  private static void runDemo() throws InterruptedException {
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

  private static void runJaccardIndexEstimation(int numSketchesToBuild) {
    System.out.println(getFormattedHeader(numSketchesToBuild));
    for (long order = 1_000_000_000; order <= 1_000_000_000; order *= 10) {
      for (int i = 1; i <= 10; i++) {
        for (double jaccardIndex = 0.00_000_000_001; jaccardIndex < 0.0_000_001; jaccardIndex *= 10) {
          for (int j = 1; j <= 10; j++) {
            double jac = jaccardIndex * j;
            long unionSize = plusMinusThreePercent(order * i);
            long intersectionSize = (long)(jac * unionSize);

            if (intersectionSize <= 0) {
              continue;
            }
            long sketchSize = (long)((unionSize - intersectionSize) / (double)numSketchesToBuild) + intersectionSize;
            //            runTestIteration(intersectionSize, getSketchSizes(numSketchesToBuild, sketchSize))
            threadPool.submit(() -> runTestIteration(intersectionSize, getSketchSizes(numSketchesToBuild, sketchSize)));
          }
        }
      }
    }
  }

  private static void runTestIteration(long exactIntersectionSize, long... sketchSizes) {
    final BetaMinHash[] sketches = buildIntersectingSketches(exactIntersectionSize, sketchSizes);
    final BetaMinHashCombiner combiner = BetaMinHashCombiner.getInstance();

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sketches.length; i++) {
      // hack
      sb.append(sketchSizes[i] + ", ");
      sb.append(sketches[i].cardinality() + ", ");
    }

    long exactUnionSize = sum(sketchSizes) - ((sketchSizes.length - 1) * exactIntersectionSize);

    sb.append(exactUnionSize + ", ");
    sb.append(combiner.union(sketches).cardinality() + ", ");

    sb.append(exactIntersectionSize + ", ");
    sb.append(combiner.intersectionCardinality(sketches) + ", ");

    // jaccard
    sb.append(exactIntersectionSize / (double)exactUnionSize + ", ");
    if (exactIntersectionSize / (double)exactUnionSize < 0) {
      System.out.println("oh boy");
      throw new RuntimeException();
    }


    sb.append(combiner.similarity(sketches));
    System.out.println(sb.toString());
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
  private static BetaMinHash[] buildIntersectingSketches(long intersectionSize, long... sketchSizes) {
    BetaMinHash[] out = new BetaMinHash[sketchSizes.length];
    for (int i = 0; i < out.length; i++) {
      out[i] = new BetaMinHash();
    }

    long counter = 0;
    // add intersecting items
    for (int i = 0; i < intersectionSize; i++) {
      byte[] val = (counter++ + "").getBytes();
      for (BetaMinHash sketch : out) {
        sketch.offer(val);
      }

    }
    // add disjoint items
    for (int i = 0; i < sketchSizes.length; i++) {
      for (int j = 0; j < (sketchSizes[i] - intersectionSize); j++) {
        out[i].offer((counter++ + "").getBytes());
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
      sb.append("actual " + i + ", ");
      sb.append("estimated " + i + ", ");
    }

    sb.append("actual ");
    sb.append(getUnionHeader(numSketches));
    sb.append(", ");

    sb.append("estimated ");
    sb.append(getUnionHeader(numSketches));
    sb.append(", ");

    sb.append("actual ");
    sb.append(getIntersectionHeader(numSketches));
    sb.append(", ");

    sb.append("estimated ");
    sb.append(getIntersectionHeader(numSketches));
    sb.append(", ");

    sb.append("actual Jaccard index, estimated Jaccard index");

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

  private static long plusMinusThreePercent(long x) {
    double pct = (rng.nextInt() % 3.1) / 100.0;
    return (long)((1 + pct) * x);
  }
}
