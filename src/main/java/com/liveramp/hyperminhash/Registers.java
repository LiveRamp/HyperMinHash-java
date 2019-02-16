package com.liveramp.hyperminhash;

/**
 * Interface over the HMH sketch's array of registers. This iface allows us to use the smallest
 * representation possible for registers without HMH's "business logic" having to know about it.
 * <p>
 * For example, if R=20, then we can use an int to represent a register instead of a long (
 * since we'd use 6 bits for leading zeros and 20 bits for the minHash portion).
 */
interface Registers<T extends Registers<T>> {

  /**
   * @return a {@link Registers} object of the appropriate size.
   */
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

  /**
   * Returns the value of the register at the given index, casting to a long if needed.
   *
   * @param index index of the register whose value should be retrieved
   * @return the value of the register at the given index
   */
  long getRegisterAtIndex(int index);

  /**
   * @return deep copy of this object
   */
  T deepCopy();

  /**
   * @return number of registers in this container object
   */
  int getNumRegisters();

  /**
   * @param index index of the register of interest
   * @return the position of the first one, as recorded in that register
   */
  int getPositionOfFirstOneAtRegister(int index);

  /**
   * @param index of the register from which to extract the mantissa
   * @return Returns the value of the mantissa packed at the given index, casting to a long if needed.
   */
  long getMantissaAtRegister(int index);

  /**
   * @return The number of registers whose value is 0
   */
  int getNumZeroRegisters();
}
