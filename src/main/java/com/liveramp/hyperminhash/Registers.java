package com.liveramp.hyperminhash;

/**
 * Interface over the HMH sketch's array of registers. This iface allows us to use the smallest
 * representation possible for registers without HMH's "business logic" having to know about it.
 */
interface Registers<T extends Registers<T>> {

  static Registers newRegisters(int p, int r) {
    if (r > 25) {
      return new LongRegisters(p, r);
    } else {
      return new IntRegisters(p, r);
    }
  }

  /**
   * @param registerIndex
   * @param incomingRegister
   * @return
   */
  boolean updateIfGreaterThan(int registerIndex, long incomingRegister);

  long getRegisterAtIndex(int index);

  T deepCopy();

  int getNumRegisters();

  int getPositionOfFirstOneAtRegister(int index);

  long getMantissaAtRegister(int index);

  int getNumZeroRegisters();
}
