# Paper Curl Field

A Processing (Java) generative art sketch that generates a paper-like field of curling ribbon strokes with soft shading and occasional highlights. Each run uses a new random seed, producing a unique composition.

## Requirements

- Java 21+
- Maven 3.9+

## Run

```bash
mvn exec:java
```

This launches the `art.PaperCurlSketch` class and renders a 900×900 canvas.

## Customize

Open `src/main/java/art/PaperCurlSketch.java` and tweak the sketch parameters:

- `ribbons`, `maxSteps`, `stepSize`, `baseWidth` for density and stroke size
- `noiseScale` and `curliness` for the flow field behavior
- `alphaBase` for transparency
- `accents` and paper tones for color palette

## Build

```bash
mvn package
```
