package com.liveramp.hyperminhash;

import java.nio.ByteBuffer;

import static com.liveramp.hyperminhash.BetaMinHash.NUM_REGISTERS;

public class BetaMinHashSerde extends IntersectionSketch.SerDe<BetaMinHash> {

  /*
    Format:

      serde token (byte)
      version (byte)
      registers (NUM_REGISTERS * short)
   */
  @Override
  public BetaMinHash readFrom(ByteBuffer inputBuffer) {
    short[] registers = new short[BetaMinHash.NUM_REGISTERS];

    byte serdeToken = inputBuffer.get();
    if (!BetaMinHash.class.equals(SerializationTokens.getClassForToken(serdeToken).get())) {
      throw new IllegalArgumentException("Input bytes do not represent a BetaMinHash object!");
    }

    byte version = inputBuffer.get();
    if (version != 1) { // 1 is the only supported version thus far
      throw new IllegalArgumentException(
          "Sketch version is " + version + ". Only version 1 is supported.");
    }

    for (int i = 0; i < NUM_REGISTERS;) {
      short value = inputBuffer.getShort();
      if (value < 0) {
        i -= value;
      }
      else {
        registers[i] = value;
        i++;
      }
    }

    return BetaMinHash.wrapRegisters(registers);
  }

  @Override
  public void writeTo(BetaMinHash sketch, ByteBuffer byteBuffer) {
    byteBuffer.put(SerializationTokens.getTokenForClass(BetaMinHash.class).get());
    byteBuffer.put(BetaMinHash.VERSION);
    for (short i = 0; i < sketch.registers.length;) {
      if (sketch.registers[i] == 0) {
        short zeros = 0;
        while (i < sketch.registers.length && sketch.registers[i] == 0) {
          zeros++;
          i++;
        }
        byteBuffer.putShort((short) -zeros);
      }
      else {
        byteBuffer.putShort(sketch.registers[i]);
        i++;
      }
    }
  }

  @Override
  public int sizeInBytes(BetaMinHash sketch) {
    int numBytes = 0;
    for (short i = 0; i < sketch.registers.length;) {
      if (sketch.registers[i] == 0) {
        while (i < sketch.registers.length && sketch.registers[i] == 0) {
          i++;
        }
      }
      else {
        i++;
      }
      numBytes += Short.BYTES;
    }
    return Byte.BYTES + // serde token
        Byte.BYTES + // version
        numBytes; // size of registers
  }
}
