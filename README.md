[![Build Status](https://travis-ci.org/LiveRamp/HyperMinHash-java.svg?branch=master)](https://travis-ci.org/LiveRamp/HyperMinHash-java)

# HyperMinHash-java
A Java implementation of the HyperMinHash algorithm, presented by
[Yu and Weber](https://arxiv.org/pdf/1710.08436.pdf).
HyperMinHash allows approximating set unions, intersections, Jaccard Indices,
and cardinalities of very large sets with high accuracy using only loglog space.
It also supports streaming updates and merging sketches, just the same
as HyperLogLog.

This repo implements two flavors of HyperMinHash:
1) **HyperMinHash**: An implementation based on HyperLogLog with the
addition of the bias correction seen in HyperLogLog++.
2) **BetaMinHash**: An implementation which uses [LogLog-Beta](http://cse.seu.edu.cn/PersonalPage/csqjxiao/csqjxiao_files/papers/INFOCOM17.pdf)
for the underlying LogLog implementation. Loglog-beta is almost identical in
accuracy to HyperLogLog++, except it performs better on cardinality
estimations for small datasets (n <= 200k). Since we use Loglog-Beta,
we refer to our implementation as BetaMinHash. However, our implementation
currently only supports a fixed precision `p=14`.

Both implementations are equipped with serialization/deserialization
capabilities out of the box for sending sketches over the wire or
persisting them to disk.

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
Collection<BetaMinHash> sketches = getSketches();
SketchCombiner<BetaMinHash> combiner = BetaMinHashCombiner.getInstance();
BetaMinHash combined = combiner.union(sketches);

// to get cardinality of the union
long unionCardinality = combined.cardinality();

// using HyperMinHash instead of BetaMinHash
Collection<HyperMinHash> sketches = getSketches();
SketchCombiner<HyperMinHash> combiner = HyperMinHashCombinre.getInstance();
HyperMinHash combined = combiner.union(sketches);
```

### Cardinality of unions
```
BetaMinHash combined = combiner.union(sketches);
long estimatedCardinality = combined.cardinality();
```

### Cardinality of intersection
```
Collection<BetaMinHash> sketches = getSketches();
SketchCombiner<BetaMinHash> combiner = BetaMinHashComber.getInstance();
long intersectionCardinality = combiner.intersectionCardinality(sketches);
```

### Serializing a sketch
To get a byte[] representation of a sketch, use the `IntersectionSketch.SerDe` interface:
```
HyperMinHash sketch = new
HyperMinHashSerde serde = new HyperMinHashSerde();
```

## Acknowledgements
Thanks to Seif Lotfy for implementing a
[Golang version of HyperMinHash](http://github.com/axiomhq/hyperminhash).
We use some of his tests in our library, and our BetaMinHash implementation
references his implementation.
