package com.liveramp.hyperminhash.demo;

import java.util.Arrays;

import com.liveramp.hyperminhash.BetaMinHash;

public class DemoBetaMinHash {
  public static void main(String[] args) {
    runDemo();
  }

  private static void runDemo() {
    runSkewTest();
    runTestLoopForNSketches(2);
    runTestLoopForNSketches(3);
    runTestLoopForNSketches(4);
  }

  private static long[] getSketchSizes(int numSketches, long size) {
    long[] out = new long[numSketches];
    Arrays.fill(out, size);
    return out;
  }

  private static void runSkewTest() {
    System.out.println(getFormattedHeader(2));

    for (long smallSetSize = 1000; smallSetSize <= 10_000_000; smallSetSize *= 10) {
      for (double frac = 0.1; frac <= 1.0; frac += 0.3) {
        runTestIteration((long)(frac * smallSetSize), smallSetSize, 10_000_000_000L);
      }
    }

    System.out.println("\n\n\n\n");
  }

  private static void runTestLoopForNSketches(int numSketches) {
    System.out.println(getFormattedHeader(numSketches));
    // 1k, 10k, 100k
    for (long sketchSizeOrder = 1_000; sketchSizeOrder < 100_000; sketchSizeOrder *= 10) {
      for (long i = 1; i <= 10; i++) {
        for (double jaccardIndex = 0.001; jaccardIndex <= 1; jaccardIndex *= 10) {
          runTestIteration((long)(jaccardIndex * sketchSizeOrder * i), getSketchSizes(numSketches, sketchSizeOrder * i));
        }
      }
    }

    // 100k, 1mil, 10mil, 100mil, 1bil, 10bil
    for (long sketchSizeOrder = 100_000; sketchSizeOrder < 10_000_000_000L; sketchSizeOrder *= 10) {
      for (long i = 1; i < 10; i *= 5) {
        for (double jaccardIndex = 0.0001; jaccardIndex <= 1; jaccardIndex *= 10) {
          runTestIteration((long)(jaccardIndex * sketchSizeOrder * i), getSketchSizes(numSketches, sketchSizeOrder * i));
        }
      }
    }

    System.out.println("\n\n\n\n");
  }

  private static void runTestIteration(long exactIntersectionSize, long... sketchSizes) {
    BetaMinHash[] sketches = buildIntersectingSketches(exactIntersectionSize, sketchSizes);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sketches.length; i++) {
      sb.append(sketchSizes[i] + ", ");
      sb.append(sketches[i].cardinality() + ", ");
    }

    long exactUnionSize = sum(sketchSizes) - ((sketchSizes.length - 1) * exactIntersectionSize);
    sb.append(exactUnionSize + ", ");
    sb.append(BetaMinHash.union(sketches) + ", ");

    sb.append(exactIntersectionSize + ", ");
    sb.append(BetaMinHash.intersection(sketches) + ", ");

    // jaccard
    sb.append(exactIntersectionSize / (double)exactUnionSize + ", ");
    sb.append(BetaMinHash.similarity(sketches));

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
        sketch.add(val);
      }
    }

    // add disjoint items
    for (int i = 0; i < sketchSizes.length; i++) {
      for (int j = 0; j < (sketchSizes[i] - intersectionSize); j++) {
        out[i].add((counter++ + "").getBytes());
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
}
