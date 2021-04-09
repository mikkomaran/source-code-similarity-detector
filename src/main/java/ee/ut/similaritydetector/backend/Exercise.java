package ee.ut.similaritydetector.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public int getSolutionCount() {
        return solutions.size();
    }

    public void findAverageSolutionSourceCodeLength() {
        float avgSolutionLength = 0;
        int solutionCount = solutions.size();
        for (Solution solution : solutions) {
            avgSolutionLength += solution.readSolutionCode(true).length();
        }
        avgSolutionLength /= solutionCount;
        averageSolutionSourceCodeLength = avgSolutionLength;
    }

    public void findAverageSolutionPreprocessedCodeLength() {
        float avgSolutionLength = 0;
        int solutionCount = solutions.size();
        for (Solution solution : solutions) {
            avgSolutionLength += solution.readSolutionCode(false).length();
        }
        avgSolutionLength /= solutionCount;
        averageSolutionPreprocessedCodeLength = avgSolutionLength;
    }

    public void calculateSimilarityThreshold() {
        double averageSolutionLength = averageSolutionPreprocessedCodeLength != 0 ?
                averageSolutionPreprocessedCodeLength :
                averageSolutionSourceCodeLength;
        double lengthMultiplier = 1;
        if (averageSolutionLength > 100)
            lengthMultiplier = averageSolutionLength / 100;

        similarityThreshold = Math.pow(0.985, lengthMultiplier);
        System.out.println(name + " - Used similarity threshold: " +similarityThreshold);
    }

}
