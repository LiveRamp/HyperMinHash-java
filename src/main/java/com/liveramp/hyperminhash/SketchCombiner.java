package com.liveramp.hyperminhash;

import java.io.Serializable;
import java.util.Collection;

/**
 * {@code SketchCombiner} instances support aggregate operations over corresponding {@link}
 * IntersectionSketch instances. Generally speaking, {@code SketchCombiner} should be singletons, as
 * they should not need to store any state related to particular sketch(es) and should be within the
 * same package as the corresponding {@link IntersectionSketch} to facilitate encapsulation of
 * sketch internals that the combiner may need access to.
 */
public interface SketchCombiner<T extends IntersectionSketch<T>> extends Serializable {

  /**
   * Return a sketch representing the union of the sets represented by the sketches in {@code
   * sketches}. Sketches passed will not be mutated. If only a single sketch is passed, this method
   * will return a deep copy.
   */
  T union(Collection<T> sketches);

  /**
   * Return an estimate of the cardinality of the intersection of the elements in the sets
   * represented by {@code sketches}.
   */
  long intersectionCardinality(Collection<T> sketches);

  /**
   * Return an estimate of the Jaccard index of the sets represented by {@code sketches}. The
   * Jaccard index is the ratio of the cardinality of the intersection of sets divided by the
   * cardinality of the union of those sets.
   */
  double similarity(Collection<T> sketches);

}
