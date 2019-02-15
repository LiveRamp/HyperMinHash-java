package com.liveramp.hyperminhash;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoHyperMinHash {

  private static final int NUM_THREADS = 1;
  private static ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);

  public static void main(String[] args) {
    System.out.println(getFormattedHeader(2));

    buildManyIntersecting(0.00001, 100000, 1000000, 10000);
  }


  private static void printLine(
      List<HyperMinHash> sketchPair, double jaccard, long unionSize, long buildTime) {

    List<String> rowData = new ArrayList<>();
    HyperMinHashCombiner combiner = HyperMinHashCombiner.getInstance();
    long exactIntersectionSize = (long) (unionSize * jaccard);
    long singleCardinality = (unionSize - exactIntersectionSize) / 2 + exactIntersectionSize;

    addToList(rowData, buildTime);

    for (int i = 0; i < sketchPair.size(); i++) {
      long cardinalityStartTime = System.currentTimeMillis();
      Long cardinalityEstimate = sketchPair.get(i).cardinality();
      Long cardinalityElapsedTime = System.currentTimeMillis() - cardinalityStartTime;

      addToList(rowData, singleCardinality);
      addToList(rowData, cardinalityEstimate);
      addToList(rowData, cardinalityElapsedTime);
    }

    addToList(rowData, unionSize);
    long unionStartTime = System.currentTimeMillis();
    Long unionEstimate = combiner.union(Arrays.asList(sketchPair.get(0), sketchPair.get(1)))
        .cardinality();
    Long unionElapsedTime = System.currentTimeMillis() - unionStartTime;
    addToList(rowData, unionEstimate);
    addToList(rowData, unionElapsedTime);

    addToList(rowData, exactIntersectionSize);
    long intersectionStartTime = System.currentTimeMillis();
    Long intersectionEstimate = combiner
        .intersectionCardinality(Arrays.asList(sketchPair.get(0), sketchPair.get(1)));
    Long intersectionElapsedTime = System.currentTimeMillis() - intersectionStartTime;
    addToList(rowData, intersectionEstimate);
    addToList(rowData, intersectionElapsedTime);

    // jaccard
    addToList(rowData, (double) exactIntersectionSize / unionSize);
    long jaccardStartTime = System.currentTimeMillis();
    double jaccardEstimate = combiner
        .similarity(Arrays.asList(sketchPair.get(0), sketchPair.get(1)));
    long jaccardElapsedTime = System.currentTimeMillis() - jaccardStartTime;
    addToList(rowData, jaccardEstimate);
    addToList(rowData, jaccardElapsedTime);
    addToList(rowData, "False");

    System.out.println(String.join(",", rowData));
  }

  private static void addToList(List<String> list, Object object) {
    list.add(object.toString());
  }


  private static List<List<IntersectionSketch>> buildManyIntersecting(
      double jaccard, long minUnion, long maxUnion, long intervalUnion) {

    List<List<IntersectionSketch>> setSketches = new ArrayList<>();

    HyperMinHash left = new HyperMinHash(21, 57);
    HyperMinHash right = new HyperMinHash(21, 57);

    long counter = 0;
    long previousUnionSize = 0;

    for (long unionSize = minUnion; unionSize <= maxUnion; unionSize += intervalUnion) {

      long startTime = System.currentTimeMillis();

      long toAdd = unionSize - previousUnionSize;
      previousUnionSize = unionSize;
      long intersectSize = (int) (toAdd * jaccard);
      long nonIntersectSize = toAdd - intersectSize;

      // add intersecting items
      for (long i = 0; i < intersectSize; i++) {
        byte[] val = (counter++ + "").getBytes();
        left.offer(val);
        right.offer(val);
      }

      // add disjoint items
      for (long i = 0; i < nonIntersectSize / 2; i++) {
        left.offer((counter++ + "").getBytes());
        right.offer((counter++ + "").getBytes());
      }

      printLine(
          Arrays.asList(
              left,
              right
          ),
          jaccard,
          unionSize,
          System.currentTimeMillis() - startTime);

    }

    return setSketches;
  }

  /**
   * formatting helpers
   **/
  private static String getFormattedHeader(int numSketches) {
    // actual A | estimate A | actual B | estimate B | actual A U B | estimate A U B | actual A ? B | estimated A ? B
    StringBuilder sb = new StringBuilder();
    sb.append("sketch build time, ");
    for (int i = 1; i <= numSketches; i++) {
      sb.append("actual " + i + ", ");
      sb.append("estimated " + i + ", ");
      sb.append("cardinality time, ");

    }

    sb.append("actual ");
    sb.append(getUnionHeader(numSketches));
    sb.append(", ");

    sb.append("estimated ");
    sb.append(getUnionHeader(numSketches));
    sb.append(", ");

    sb.append("union time, ");

    sb.append("actual ");
    sb.append(getIntersectionHeader(numSketches));
    sb.append(", ");

    sb.append("estimated ");
    sb.append(getIntersectionHeader(numSketches));
    sb.append(", ");

    sb.append("intersection time, ");

    sb.append("actual Jaccard index, estimated Jaccard index, Jaccard time, Compute E(C)?");

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

