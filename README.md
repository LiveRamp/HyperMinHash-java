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
Thanks to Seif Lotfy for implementing a
[Golang version of HyperMinHash](http://github.com/axiomhq/hyperminhash).
We use some of his tests in our library, and the decision to use
LogLog-Beta was due to the example he set.
