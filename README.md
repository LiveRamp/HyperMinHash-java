[![Build Status](https://travis-ci.org/LiveRamp/HyperMinHash-java.svg?branch=master)](https://travis-ci.org/LiveRamp/HyperMinHash-java)

# HyperMinHash-java
A Java implementation of the HyperMinHash algorithm, presented in [Yu and Weber](https://arxiv.org/pdf/1710.08436.pdf). HyperMinHash allows approximating cardinalities, intersections, and Jaccard indices of sets with very high accuracy, in loglog space, and in a streaming fashion.

This library uses [Loglog-Beta](https://arxiv.org/pdf/1612.02284.pdf) for the underlying LogLog implementation. Loglog-beta is almost identical in accuracy to HyperLogLog++, except it performs better on cardinality estimations for small datasets (n <= 200k). Since we use Loglog-Beta, we refer to our implementation as BetaMinHash.

In addition to the features described above, this library adds the ability to do many-way intersections between sets, a new feature not described in the original paper (though, credit to the authors, easy to deduce from their examples). We also provide an implementation of the Hadoop Writable interface for easy use with MapReduce.

## Demo Usage

### Cardinality estimation
```
Set<byte[]> mySet = getMySet();
BetaMinHash sketch = new BetaMinHash();
for (byte[] element : mySet){
    sketch.add(element);
}

long estimatedCardinality = sketch.cardinality();
```


### Merging (unioning) sketches
```
BetaMinHash[] sketches = getSketches();
SketchCombiner<BetaMinHash> combiner = BetaMinHashComber.getInstance();
BetaMinHash combined = combiner.union(sketches);
```

### Cardinality of unions
```
BetaMinHash combined = combiner.union(sketches);
long estimatedCardinality = combined.cardinality();
```

### Cardinality of intersection
```
BetaMinHash[] sketches = getSketches();
SketchCombiner<BetaMinHash> combiner = BetaMinHashComber.getInstance();
long intersectionCardinality = combiner.intersectionCardinality(sketches);
```

## Acknowledgements
Thanks to Seif Lotfy for implementing a [Golang version of HyperMinHash](http://github.com/axiomhq/hyperminhash). We use some of his tests in our library, and the decision to use LogLog-Beta was due to the example he set.
