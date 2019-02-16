package com.liveramp.hyperminhash;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class to store serialization tokens.
 */
class SerializationTokens {

  private static final Map<Class, Byte> classToToken;
  private static final Map<Byte, Class> tokenToClass;

  static {
    // Tokens can only be added. They should not be removed or edited.
    classToToken = new HashMap<>();
    tokenToClass = new HashMap<>();

    classToToken.put(HyperMinHash.class, (byte) 1);
    tokenToClass.put((byte) 1, HyperMinHash.class);

    classToToken.put(BetaMinHash.class, (byte) 2);
    tokenToClass.put((byte) 2, BetaMinHash.class);

    classToToken.put(LongRegisters.class, (byte) 3);
    tokenToClass.put((byte) 3, LongRegisters.class);

    classToToken.put(IntRegisters.class, (byte) 4);
    tokenToClass.put((byte) 4, IntRegisters.class);
  }

  static Optional<Byte> getTokenForClass(Class clazz) {
    return Optional.ofNullable(classToToken.get(clazz));
  }

  static Optional<Class> getClassForToken(byte token) {
    return Optional.ofNullable(tokenToClass.get(token));
  }

}
