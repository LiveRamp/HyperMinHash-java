[![Build Status](https://travis-ci.com/LiveRamp/HyperMinHash-java.svg?branch=master)](https://travis-ci.com/LiveRamp/HyperMinHash-java)

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
2) **BetaMinHash**: An implementation which uses [LogLog-Beta](https://arxiv.org/abs/1612.02284)
for the underlying LogLog implementation. Loglog-beta is almost identical in
accuracy to HyperLogLog++, except it performs better on cardinality
estimations for small datasets (n <= 80k), holding memory fixed. Since we use Loglog-Beta,
we refer to our implementation as BetaMinHash. However, our implementation
currently only supports a fixed precision `p=14`.

If you expect to be dealing with low cardinality datasets (<= 80,000 unique elements),
we recommend using BetaMinHash as it has a smaller memory footprint and is more accurate
than HyperLogLog in the range from 20,000-80,000, holding memory fixed. However, note that
different sketch types are not interchangeable i.e: obtaining the intersection of an
HMH and a BMH is not currently supported.

Both implementations are equipped with serialization/deserialization
capabilities out of the box for sending sketches over the wire or
persisting them to disk.

## Usage

### Importing via Maven
```xml
<dependency>
  <groupId>com.liveramp</groupId>
  <artifactId>hyperminhash</artifactId>
  <version>0.2</version>
</dependency>
```

### Cardinality estimation
```java
Set<byte[]> mySet = getMySet();
BetaMinHash sketch = new BetaMinHash();
for (byte[] element : mySet){
    sketch.add(element);
}

long estimatedCardinality = sketch.cardinality();
```


### Merging (unioning) sketches
```java
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
```java
BetaMinHash combined = combiner.union(sketches);
long estimatedCardinality = combined.cardinality();
```

### Cardinality of intersection
```java
Collection<BetaMinHash> sketches = getSketches();
SketchCombiner<BetaMinHash> combiner = BetaMinHashComber.getInstance();
long intersectionCardinality = combiner.intersectionCardinality(sketches);
```

### Serializing a sketch
To get a byte[] representation of a sketch, use the `IntersectionSketch.SerDe` interface:
```java
HyperMinHash sketch = getSketch();
HyperMinHashSerde serde = new HyperMinHashSerde();

byte[] serialized = serde.toBytes(sketch);
HyperMinHash deserialized = serde.fromBytes(serialized);

int sizeInBytes = serde.sizeInBytes(sketch);
```

## Maintainers

Commit authorship was lost when merging code. The maintainers of the library, in alphabetical order, are:

1) Christian Hansen (github.com/ChristianHansen)
2) Harry Rackmil (github.com/harryrackmil)
3) Shrif Nada (github.com/sherifnada)

## Acknowledgements

Thanks to Seif Lotfy for implementing a
[Golang version of HyperMinHash](http://github.com/axiomhq/hyperminhash).
We use some of his tests in our library, and our BetaMinHash implementation
references his implementation.
