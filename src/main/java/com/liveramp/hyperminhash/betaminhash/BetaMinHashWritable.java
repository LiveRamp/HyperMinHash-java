package com.liveramp.hyperminhash.betaminhash;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.Writable;

public class BetaMinHashWritable implements Writable {
  private short[] registers;

  public BetaMinHashWritable() {
    registers = new short[BetaMinHash.NUM_REGISTERS];
  }

  public BetaMinHashWritable(BetaMinHash sketch) {
    this.registers = sketch.registers;
  }

  public BetaMinHash getSketch() {
    return new BetaMinHash(registers);
  }

  public BetaMinHashWritable combine(BetaMinHashWritable other) {
    BetaMinHash mergedSketch = BetaMinHashCombiner
        .getInstance()
        .union(new BetaMinHash(registers), other.getSketch());
    return new BetaMinHashWritable(mergedSketch);
  }

  public void write(DataOutput dataOutput) throws IOException {
    for (short register : registers) {
      dataOutput.writeShort(register);
    }
  }

  @Override
  public void readFields(DataInput dataInput) throws IOException {
    for (int i = 0; i < registers.length; i++) {
      registers[i] = dataInput.readShort();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BetaMinHashWritable that = (BetaMinHashWritable)o;

    return Arrays.equals(registers, that.registers);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(registers);
  }

  @Override
  public String toString() {
    return "WritableBetaMinHash{" +
        "registers=" + Arrays.toString(registers) +
        '}';
  }
}
