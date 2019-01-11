package com.liveramp.hyperminhash;

import java.nio.ByteBuffer;


public class HyperMinHashSerDe implements IntersectionSketch.SerDe<HyperMinHash> {

  /*
        Serialized format:
          serializationToken (byte)
          version (byte)
          p (int)
          r (int)
          num_registers (int)
          registers (long[] of size num_registers)
   */
  @Override
  public HyperMinHash fromBytes(byte[] bytes) {
    ByteBuffer inputBuffer = ByteBuffer.wrap(bytes);
    byte serdeToken = inputBuffer.get();
    if (!HyperMinHash.class.equals(SerializationTokens.getClassForToken(serdeToken).get())) {
      throw new IllegalArgumentException("Input bytes do not represent a HyperMinHash object!");
    }

    byte version = inputBuffer.get();
    if (version != 1) { // 1 is the only supported version thus far
      throw new IllegalArgumentException(
          "Sketch version is " + version + ". Only version 1 is supported.");
    }

    int p = inputBuffer.getInt();
    int r = inputBuffer.getInt();
    int numRegisters = inputBuffer.getInt();
    long[] registers = new long[numRegisters];
    for (int i = 0; i < numRegisters; i++) {
      registers[i] = inputBuffer.getLong();
    }
    return HyperMinHash.wrapRegisters(p, r, registers);
  }

  @Override
  public byte[] toBytes(HyperMinHash sketch) {
    ByteBuffer outputBuffer = ByteBuffer.allocate(sizeInBytes(sketch));
    outputBuffer.put(SerializationTokens.getTokenForClass(HyperMinHash.class).get());
    outputBuffer.put(HyperMinHash.VERSION);
    outputBuffer.putInt(sketch.p);
    outputBuffer.putInt(sketch.r);
    outputBuffer.putInt(sketch.registers.length);
    for (long register : sketch.registers) {
      outputBuffer.putLong(register);
    }
    return outputBuffer.array();
  }

  @Override
  public int sizeInBytes(HyperMinHash sketch) {
    return Byte.BYTES + // serde token
        Byte.BYTES + // version
        Integer.BYTES + // p
        Integer.BYTES + // r
        Integer.BYTES + // num registers
        Long.BYTES * sketch.registers.length; // registers
  }
}
