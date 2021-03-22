package main.java.ee.ut.similaritydetector.backend;

public class SimilarSolutionPair {

    private final double similarity;
    private final Solution firstSolution;
    private final Solution secondSolution;

    public SimilarSolutionPair(double similarity, Solution firstSolution, Solution secondSolution) {
        this.similarity = similarity;
        this.firstSolution = firstSolution;
        this.secondSolution = secondSolution;
    }

    public double getSimilarity() {
        return similarity;
    }

    public Solution getFirstSolution() {
        return firstSolution;
    }

    public Solution getSecondSolution() {
        return secondSolution;
    }
}
