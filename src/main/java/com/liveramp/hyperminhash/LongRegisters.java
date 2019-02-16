package com.liveramp.hyperminhash;

import java.util.Arrays;
import java.util.Objects;

class LongRegisters implements Registers<LongRegisters> {

  final long[] registers;
  private final int p;
  private final int r;

  LongRegisters(int p, int r) {
    this(p, r, new long[1 << p]);
  }

  LongRegisters(int p, int r, long[] registers) {
    this.p = p;
    this.r = r;
    this.registers = registers;
  }

  @Override
  public boolean updateIfGreaterThan(int registerIndex, long incomingRegister) {
    long currentRegister = registers[registerIndex];
    int currentLeadingOnePosition = LongPacker.unpackPositionOfFirstOne(currentRegister, r);
    int incomingLeadingOnePosition = LongPacker.unpackPositionOfFirstOne(incomingRegister, r);

    if (currentLeadingOnePosition < incomingLeadingOnePosition) {
      registers[registerIndex] = incomingRegister;
      return true;
    } else if (currentLeadingOnePosition == incomingLeadingOnePosition) {
      long currentMantissa = LongPacker.unpackMantissa(currentRegister, r);
      long incomingMantissa = LongPacker.unpackMantissa(incomingRegister, r);

      if (currentMantissa > incomingMantissa) {
        registers[registerIndex] = incomingRegister;
        return true;
      }
    }

    return false;
  }

  @Override
  public long getRegisterAtIndex(int index) {
    return registers[index];
  }

  @Override
  public LongRegisters deepCopy() {
    return new LongRegisters(p, r, Arrays.copyOf(registers, registers.length));
  }

  @Override
  public int getNumRegisters() {
    return registers.length;
  }

  @Override
  public int getPositionOfFirstOneAtRegister(int index) {
    return LongPacker.unpackPositionOfFirstOne(registers[index], r);
  }

  @Override
  public long getMantissaAtRegister(int index) {
    return LongPacker.unpackMantissa(registers[index], r);
  }

  @Override
  public int getNumZeroRegisters() {
    int count = 0;
    for (long register : registers) {
      if (register == 0) {
        count++;
      }
    }
    return count;
  }

  @Override
  public String toString() {
    return "LongRegisters{" +
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
    LongRegisters that = (LongRegisters) o;
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
