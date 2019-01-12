package com.liveramp.hyperminhash;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class TestLongPacker {

  @Test
  public void testPacking() {
    int iterations = 100_000;

    Random rng = new Random();
    for (int i = 0; i < iterations; i++) {

      // leading zeroes is at most 6 bits i.e: 63
      int leftmostOnePos = rng.nextInt(63) + 1;

      // r can be at most 58
      int r = Math.max(rng.nextInt(58), 10);
      long minHashBits = rng.nextLong() >>> (Long.SIZE - r);

      long packedRegister = LongPacker.pack(leftmostOnePos, minHashBits, r);
      Assert.assertEquals(leftmostOnePos, LongPacker.unpackPositionOfFirstOne(packedRegister, r));
      Assert.assertEquals(minHashBits, LongPacker.unpackMantissa(packedRegister, r));
    }
  }
}