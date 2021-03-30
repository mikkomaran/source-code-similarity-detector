package ee.ut.similaritydetector.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ee.ut.similaritydetector.backend.Analyser.readSolutionCode;

public class Exercise {

    private final String name;
    private final List<Solution> solutions;
    private double averageSolutionSourceCodeLength;
    private double averageSolutionPreprocessedCodeLength;
    private double similarityThreshold = 0.95;

    public Exercise(String name, Solution... solutions) {
        this.name = name;
        this.solutions = new ArrayList<>();
        Collections.addAll(this.solutions, solutions);
    }

    public String getName() {
        return name;
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public void addSolution(Solution solution) {
        solutions.add(solution);
    }

    public double getAverageSolutionSourceCodeLength() {
        return averageSolutionSourceCodeLength;
    }

    public double getAverageSolutionPreprocessedCodeLength() {
        return averageSolutionPreprocessedCodeLength;
    }

    public int getExerciseSolutionCount() {
        return solutions.size();
    }

    public void findAverageSolutionSourceCodeLength() {
        float avgSolutionLength = 0;
        int solutionCount = solutions.size();
        for (Solution solution : solutions) {
            avgSolutionLength += readSolutionCode(solution, true).length();
        }
        avgSolutionLength /= solutionCount;
        averageSolutionSourceCodeLength = avgSolutionLength;
    }

    public void findAverageSolutionPreprocessedCodeLength() {
        float avgSolutionLength = 0;
        int solutionCount = solutions.size();
        for (Solution solution : solutions) {
            avgSolutionLength += readSolutionCode(solution, false).length();
        }
        avgSolutionLength /= solutionCount;
        averageSolutionPreprocessedCodeLength = avgSolutionLength;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public void calculateSimilarityThreshold() {

    }

}
