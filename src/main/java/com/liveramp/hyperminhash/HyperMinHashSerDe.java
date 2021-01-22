package com.liveramp.hyperminhash;

import java.nio.ByteBuffer;

public class HyperMinHashSerDe extends IntersectionSketch.SerDe<HyperMinHash> {

  /*
        Serialized format:
          serializationToken (byte)
          version (byte)
          p (int)
          r (int)
          register serde token (byte)
          num_registers (int)
          registers (variable size depending on type of registers)

          The java purist will cringe at my use of if statements instead of polymorphism
          to serialize different types of registers. This is probably something that should be
          revisited, but the polymorphism approach made it harder to see where the byte buffer
          was being modified which made debugging harder. So I'm going with if statements
          for now.
   */
  @Override
  public HyperMinHash readFrom(ByteBuffer inputBuffer) {
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

    byte registerSerdeToken = inputBuffer.get();
    int numRegisters = inputBuffer.getInt();

    Class registersClass = SerializationTokens.getClassForToken(registerSerdeToken)
        .orElseThrow(() -> new IllegalArgumentException(
            "No class found for serde token: " + registerSerdeToken));
    Registers registers;

    if (LongRegisters.class.equals(registersClass)) {
      long[] registerValues = new long[numRegisters];
      for (int i = 0; i < numRegisters;) {
        long value = inputBuffer.getLong();
        if (value < 0) {
          i -= value;
        }
        else {
          registerValues[i] = value;
          i++;
        }
      }
      registers = new LongRegisters(p, r, registerValues);
    } else if (IntRegisters.class.equals(registersClass)) {
      int[] registerValues = new int[numRegisters];
      for (int i = 0; i < numRegisters;) {
        int value = inputBuffer.getInt();
        if (value < 0) {
          i -= value;
        }
        else {
          registerValues[i] = value;
          i++;
        }
      }
      registers = new IntRegisters(p, r, registerValues);
    } else {
      throw new IllegalArgumentException("Register type not supported: " + registersClass);
    }

    return new HyperMinHash(p, r, registers);
  }

  @Override
  public void writeTo(HyperMinHash sketch, ByteBuffer outputBuffer) {
    outputBuffer.put(SerializationTokens.getTokenForClass(HyperMinHash.class).get());
    outputBuffer.put(HyperMinHash.VERSION);
    outputBuffer.putInt(sketch.p);
    outputBuffer.putInt(sketch.r);

    Class registersClass = sketch.registers.getClass();
    byte registersSerdeToken = SerializationTokens.getTokenForClass(registersClass)
        .orElseThrow(() -> new IllegalArgumentException(
            "Sketch Registers do not have a serialization token!"));

    outputBuffer.put(registersSerdeToken);
    outputBuffer.putInt(sketch.registers.getNumRegisters());

    if (LongRegisters.class.equals(registersClass)) {
      LongRegisters longRegisters = (LongRegisters) sketch.registers;
      int numRegisters = sketch.registers.getNumRegisters();
      for (int i = 0; i < numRegisters;) {
        if (longRegisters.registers[i] == 0) {
          int zeros = 0;
          while (i < numRegisters && longRegisters.registers[i] == 0) {
            zeros++;
            i++;
          }
          outputBuffer.putLong(-zeros);
        } else {
          outputBuffer.putLong(longRegisters.registers[i]);
          i++;
        }
      }
    } else if (IntRegisters.class.equals(registersClass)) {
      IntRegisters intRegisters = (IntRegisters) sketch.registers;
      int numRegisters = sketch.registers.getNumRegisters();
      for (int i = 0; i < numRegisters;) {
        if (intRegisters.registers[i] == 0) {
          int zeros = 0;
          while (i < numRegisters && intRegisters.registers[i] == 0) {
            zeros++;
            i++;
          }
          outputBuffer.putInt(-zeros);
        } else {
          outputBuffer.putInt(intRegisters.registers[i]);
          i++;
        }
      }
    } else {
      throw new IllegalArgumentException("Register type not supported: " + registersClass);
    }
  }

  @Override
  public int sizeInBytes(HyperMinHash sketch) {
    Class registersClass = sketch.registers.getClass();
    int registerSizeInBytes;
    if (LongRegisters.class.equals(registersClass)) {
      registerSizeInBytes = Long.BYTES;
    } else if (IntRegisters.class.equals(registersClass)) {
      registerSizeInBytes = Integer.BYTES;
    } else {
      throw new IllegalArgumentException("Register type not supported: " + registersClass);
    }

    int numBytes = 0;
    int numRegisters = sketch.registers.getNumRegisters();
    for (int i = 0; i < numRegisters;) {
      if (sketch.registers.getRegisterAtIndex(i) == 0) {
        while (i < numRegisters && sketch.registers.getRegisterAtIndex(i) == 0) {
          i++;
        }
      }
      else {
        i++;
      }
      numBytes += registerSizeInBytes;
    }

    return Byte.BYTES + // serde token
        Byte.BYTES + // version
        Integer.BYTES + // p
        Integer.BYTES + // r
        Byte.BYTES + // register serde token
        Integer.BYTES + // num registers
        numBytes; // registers
  }
}
