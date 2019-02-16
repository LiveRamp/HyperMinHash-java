package com.liveramp.hyperminhash;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class TestHyperMinHashSerDe {

  @Test
  public void testRoundtripEmptySketch() {
    HyperMinHashSerDe serde = new HyperMinHashSerDe();
    int iterations = 1_000; // multiple iterations so we can try for different values of P/R

    RandomTestRunner.runRandomizedTest(iterations, rng -> {
      int p = Math.max(rng.nextInt(21), 4);
      int r = Math.max(rng.nextInt(57), 4);
      HyperMinHash hyperMinHash = new HyperMinHash(p, r);
      Assert.assertEquals(hyperMinHash, serde.fromBytes(serde.toBytes(hyperMinHash)));
    });
  }

  @Test
  public void testRoundtripFilledSketch() {
    HyperMinHashSerDe serde = new HyperMinHashSerDe();
    int iterations = 5_000;
    RandomTestRunner.runRandomizedTest(iterations, rng -> {
      int p = Math.max(rng.nextInt(21), 4);
      int r = Math.max(rng.nextInt(57), 4);
      HyperMinHash hmh = new HyperMinHash(p, r);

      int numElements = 1000;
      for (int j = 0; j < numElements; j++) {
        hmh.offer(randomByteArrayOfLength(rng, 50));
      }

      Assert.assertEquals(hmh, serde.fromBytes(serde.toBytes(hmh)));
    });
  }

  @Test
  public void testSizeInBytes() {
    HyperMinHashSerDe serde = new HyperMinHashSerDe();
    RandomTestRunner.runRandomizedTest(1_000, rng -> {
      int p = Math.max(rng.nextInt(21), 4);
      int r = Math.max(rng.nextInt(57), 4);
      HyperMinHash hmh = new HyperMinHash(p, r);
      int numElements = 1000;
      for (int j = 0; j < numElements; j++) {
        hmh.offer(randomByteArrayOfLength(rng, 50));
      }

      Assert.assertEquals(serde.sizeInBytes(hmh), serde.toBytes(hmh).length);
    });

  }

  private byte[] randomByteArrayOfLength(Random rng, int n) {
    byte[] bytes = new byte[n];
    rng.nextBytes(bytes);
    return bytes;
  }
}
