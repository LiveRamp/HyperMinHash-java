package com.liveramp.hyperminhash;

import java.util.Random;
import java.util.function.Consumer;

public class RandomTestRunner {

  static void runRandomizedTest(int iterations, Consumer<Random> test) {
    Random rng = new Random();
    for (int i = 0; i < iterations; i++) {
      long seed = rng.nextLong();
      runRandomizedTestWithSeed(test, seed);
    }
  }

  // Can be called directly to recreate bugs
  static void runRandomizedTestWithSeed(Consumer<Random> test, long seed) {
    try {
      test.accept(new Random(seed));
    } catch (Exception e) {
      throw new Error("Test failure. Seed = " + seed, e);
    }
  }

}
