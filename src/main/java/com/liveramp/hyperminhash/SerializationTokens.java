package com.liveramp.hyperminhash;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class to store serialization tokens.
 */
class SerializationTokens {

  static final Map<Class<? extends IntersectionSketch<?>>, Byte> classToToken;
  static final Map<Byte, Class<? extends IntersectionSketch<?>>> tokenToClass;

  static {
    // Tokens can only be added. They should not be removed or edited.
    classToToken = new HashMap<>();
    tokenToClass = new HashMap<>();

    classToToken.put(HyperMinHash.class, (byte) 1);
    tokenToClass.put((byte) 1, HyperMinHash.class);

    classToToken.put(BetaMinHash.class, (byte) 2);
    tokenToClass.put((byte) 2, BetaMinHash.class);
  }

  static <T extends IntersectionSketch<T>> Optional<Byte> getTokenForClass(Class<T> clazz) {
    return Optional.ofNullable(classToToken.get(clazz));
  }

  static <T extends IntersectionSketch<T>> Optional<Class<T>> getClassForToken(byte token) {
    return Optional.ofNullable((Class<T>) tokenToClass.get(token));
  }

}
