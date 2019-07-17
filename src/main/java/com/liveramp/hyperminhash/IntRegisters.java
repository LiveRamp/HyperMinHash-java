package com.liveramp.hyperminhash;

import java.util.Arrays;
import java.util.Objects;

class IntRegisters implements Registers<IntRegisters> {

  final int[] registers;
  private final int p;
  private final int r;

  IntRegisters(int p, int r) {
    this(p, r, new int[1 << p]);
  }

  IntRegisters(int p, int r, int[] registers) {
    if (r > 25) {
      throw new IllegalArgumentException("R cannot be greater than 25 when using IntRegisters");
    }

    this.p = p;
    this.r = r;
    this.registers = registers;
  }

  @Override
  public boolean updateIfGreaterThan(int registerIndex, long incomingRegister) {
    int intIncomingRegister = Math.toIntExact(incomingRegister);
    int currentRegister = registers[registerIndex];
    int currentLeadingOnePosition = IntPacker.unpackPositionOfFirstOne(currentRegister, r);
    int incomingLeadingOnePosition = IntPacker.unpackPositionOfFirstOne(intIncomingRegister, r);

    if (currentLeadingOnePosition < incomingLeadingOnePosition) {
      registers[registerIndex] = intIncomingRegister;
      return true;
    } else if (currentLeadingOnePosition == incomingLeadingOnePosition) {
      int currentMantissa = IntPacker.unpackMantissa(currentRegister, r);
      int incomingMantissa = IntPacker.unpackMantissa(intIncomingRegister, r);

      if (currentMantissa > incomingMantissa) {
        registers[registerIndex] = intIncomingRegister;
        return true;
      }
    }

    return false;
  }

  @Override
  public long getRegisterAtIndex(int index) {
    return (long) registers[index];
  }

  @Override
  public IntRegisters deepCopy() {
    return new IntRegisters(p, r, Arrays.copyOf(registers, registers.length));
  }

  @Override
  public int getNumRegisters() {
    return registers.length;
  }

  @Override
  public int getPositionOfFirstOneAtRegister(int index) {
    return IntPacker.unpackPositionOfFirstOne(registers[index], r);
  }

  @Override
  public long getMantissaAtRegister(int index) {
    return (long) IntPacker.unpackMantissa(registers[index], r);
  }

  @Override
  public int getNumZeroRegisters() {
    int count = 0;
    for (int register : registers) {
      if (register == 0) {
        count++;
      }
    }
    return count;
  }

  @Override
  public String toString() {
    return "IntRegisters{" +
        "registers=" + Arrays.toString(registers) +
        ", p=" + p +
        ", r=" + r +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IntRegisters that = (IntRegisters) o;
    return p == that.p &&
        r == that.r &&
        Arrays.equals(registers, that.registers);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(p, r);
    result = 31 * result + Arrays.hashCode(registers);
    return result;
  }
}
