package com.liveramp.hyperminhash;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class TestBetaMinHashSerde {

  @Test
  public void testRoundtripEmptySketch() {
    BetaMinHashSerde serde = new BetaMinHashSerde();
    BetaMinHash sketch = new BetaMinHash();
    Assert.assertEquals(sketch, serde.fromBytes(serde.toBytes(sketch)));
  }

  @Test
  public void testRoundtripFilledSketch() {
    BetaMinHashSerde serde = new BetaMinHashSerde();
    int iterations = 5_000;
    RandomTestRunner.runRandomizedTest(iterations, rng -> {
      BetaMinHash sketch = new BetaMinHash();

      int numElements = 1000;
      for (int j = 0; j < numElements; j++) {
        sketch.offer(randomByteArrayOfLength(rng, 50));
      }

      Assert.assertEquals(sketch, serde.fromBytes(serde.toBytes(sketch)));
    });
  }

  @Test
  public void testSizeInBytes() {
    // not super valuable right now since we store a fixed size sketch, but futureproofing
    BetaMinHashSerde serde = new BetaMinHashSerde();
    RandomTestRunner.runRandomizedTest(1_000, rng -> {
      BetaMinHash sketch = new BetaMinHash();
      int numElements = 1000;
      for (int j = 0; j < numElements; j++) {
        sketch.offer(randomByteArrayOfLength(rng, 50));
      }

      Assert.assertEquals(serde.sizeInBytes(sketch), serde.toBytes(sketch).length);
    });

  }

  private byte[] randomByteArrayOfLength(Random rng, int n) {
    byte[] bytes = new byte[n];
    rng.nextBytes(bytes);
    return bytes;
  }
}
