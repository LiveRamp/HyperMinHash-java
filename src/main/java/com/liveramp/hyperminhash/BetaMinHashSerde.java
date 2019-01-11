package com.liveramp.hyperminhash;

import java.nio.ByteBuffer;

import static com.liveramp.hyperminhash.BetaMinHash.NUM_REGISTERS;

public class BetaMinHashSerde implements IntersectionSketch.SerDe<BetaMinHash> {

  /*
    Format:

      serde token (byte)
      version (byte)
      registers (NUM_REGISTERS * short)


   */
  @Override
  public BetaMinHash fromBytes(byte[] bytes) {
    short[] registers = new short[BetaMinHash.NUM_REGISTERS];
    ByteBuffer inputBuffer = ByteBuffer.wrap(bytes);

    byte serdeToken = inputBuffer.get();
    if (!BetaMinHash.class.equals(SerializationTokens.getClassForToken(serdeToken).get())) {
      throw new IllegalArgumentException("Input bytes do not represent a BetaMinHash object!");
    }

    byte version = inputBuffer.get();
    if (version != 1) { // 1 is the only supported version thus far
      throw new IllegalArgumentException(
          "Sketch version is " + version + ". Only version 1 is supported.");
    }

    for (int i = 0; i < NUM_REGISTERS; i++) {
      registers[i] = inputBuffer.getShort();
    }

    return BetaMinHash.wrapRegisters(registers);
  }

  @Override
  public byte[] toBytes(BetaMinHash sketch) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(sizeInBytes(sketch));
    byteBuffer.put(SerializationTokens.getTokenForClass(BetaMinHash.class).get());
    byteBuffer.put(BetaMinHash.VERSION);
    for (short s : sketch.registers) {
      byteBuffer.putShort(s);
    }
    return byteBuffer.array();
  }

  @Override
  public int sizeInBytes(BetaMinHash sketch) {
    return Byte.BYTES + // serde token
        Byte.BYTES + // version
        Integer.BYTES + // p
        Integer.BYTES + // q
        Integer.BYTES + // r
        Integer.BYTES + // num registers
        NUM_REGISTERS * Short.BYTES; // size of registers
  }
}
