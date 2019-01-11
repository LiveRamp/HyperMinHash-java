package com.liveramp.hyperminhash;

/**
 * Representation of a set that is able to estimate the cardinality of that set, and perform the
 * operations in {@link SketchCombiner}. Each implementation of this interface should have a corresponding
 * implementation of {@link SketchCombiner} and {@link SerDe}.
 */
public interface IntersectionSketch<T extends IntersectionSketch<T>> {

  /**
   * Returns an estimate of the cardinality of sets represented by the sketch.
   */
  long cardinality();

  /**
   * @param bytes a representative key or serialized representation of the object to be added to
   *              this sketch. If using a representative key instead of a complete serialized
   *              representation, using at least 128 bits for the key where possible is recommended
   *              to maximize accuracy.
   * @return false if the value returned by cardinality() is unaffected by the appearance of o in
   * the stream.
   */
  boolean offer(byte[] bytes);

  /**
   * @return a deep copy of the {@link IntersectionSketch} instance.
   */
  T deepCopy();

  /**
   * Implementing classes provide serialization-related functionality for the relevant
   * {@link IntersectionSketch} type.
   *
   * @param <T> Intersection Sketch Type
   */
  interface SerDe<T extends IntersectionSketch<T>> {

    /**
     * @param bytes serialized representation of the sketch.
     * @return deserialized sketch
     */
    T fromBytes(byte[] bytes);


    /**
     * @param sketch the sketch to be serialized
     * @return serialized representation of the input sketch
     */
    byte[] toBytes(T sketch);

    /**
     * Returns the size of the serialized form of this sketch
     *
     * @param sketch the sketch whose size in bytes we want
     * @return size in bytes
     */
    int sizeInBytes(T sketch);
  }
}

