package ee.ut.similaritydetector.backend;

import java.util.Locale;

public class SimilarSolutionPair {

    private final double similarity;
    private final Solution firstSolution;
    private final Solution secondSolution;

    // For JavaFX tableview usage
    public String similarityPercentage;
    public String author1;
    public String author2;

    public SimilarSolutionPair(double similarity, Solution firstSolution, Solution secondSolution) {
        this.similarity = similarity;
        this.firstSolution = firstSolution;
        this.secondSolution = secondSolution;
        author1 = firstSolution.getAuthor();
        author2 = secondSolution.getAuthor();
        similarityPercentage = String.format(Locale.ENGLISH,"%.1f%%", similarity * 100);
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

    public String getSimilarityPercentage() {
        return similarityPercentage;
    }

    public String getAuthor1() {
        return author1;
    }

    public String getAuthor2() {
        return author2;
    }

    @Override
    public String toString() {
        return author1 + " & " + author2 + ":  " + similarityPercentage;
    }
}
