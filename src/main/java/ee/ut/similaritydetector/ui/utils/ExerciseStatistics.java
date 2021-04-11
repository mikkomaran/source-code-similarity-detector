package ee.ut.similaritydetector.ui.utils;

import ee.ut.similaritydetector.backend.Analyser;
import ee.ut.similaritydetector.backend.Exercise;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Data class for convenient inserting of data to the table in results view.
 */
public class ExerciseStatistics {
    private final String exerciseName;
    private final int totalSolutions;
    private final int suspiciousSolutions;
    private final double percentageSuspiciousSolutions;
    private final int similarPairs;
    private final int similarClusters;
    private final int averageSolutionLength;
    private final String similarityThreshold;

    public String getExerciseName() {
        return exerciseName;
    }

    public int getTotalSolutions() {
        return totalSolutions;
    }

    public int getSuspiciousSolutions() {
        return suspiciousSolutions;
    }

    public double getPercentageSuspiciousSolutions() {
        return percentageSuspiciousSolutions;
    }

    public int getSimilarPairs() {
        return similarPairs;
    }

    public int getSimilarClusters() {
        return similarClusters;
    }

    public int getAverageSolutionLength() {
        return averageSolutionLength;
    }

    public String getSimilarityThreshold() {
        return similarityThreshold;
    }

    public ExerciseStatistics(Exercise exercise, Analyser analyser) {
        this.exerciseName = exercise.getName();
        this.totalSolutions = exercise.getSolutionCount();
        this.suspiciousSolutions = analyser.getSimilarSolutionClusters().stream().filter(cluster ->
                cluster.getExerciseName().equals(exerciseName)).mapToInt(cluster ->
                cluster.getSolutions().size()).sum();
        BigDecimal percentage = new BigDecimal(Double.toString((double) suspiciousSolutions / totalSolutions * 100));
        percentage = percentage.setScale(1, RoundingMode.HALF_UP);
        this.percentageSuspiciousSolutions = percentage.doubleValue();
        this.similarPairs = (int) analyser.getSimilarSolutionPairs().stream().filter(pair ->
                pair.getFirstSolution().getExerciseName().equals(exerciseName)).count();
        this.similarClusters = (int) analyser.getSimilarSolutionClusters().stream().filter(cluster ->
                cluster.getExerciseName().equals(exerciseName)).count();
        this.averageSolutionLength = (int) Math.round(
                analyser.isPreprocessSourceCode() ?
                exercise.getAverageSolutionPreprocessedCodeLength() :
                exercise.getAverageSolutionSourceCodeLength());
        this.similarityThreshold = String.format("%.1f%%", exercise.getSimilarityThreshold() * 100);
    }

}