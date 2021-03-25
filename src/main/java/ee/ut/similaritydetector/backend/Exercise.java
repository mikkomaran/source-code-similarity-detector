package main.java.ee.ut.similaritydetector.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static main.java.ee.ut.similaritydetector.backend.Analyser.readSolutionCode;

public class Exercise {

    private final String name;
    private final List<Solution> solutions;
    private double averageSolutionSourceCodeLength;
    private double averageSolutionPreprocessedCodeLength;

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

    public double getAverageSolutionSourceCodeLength() {
        return averageSolutionSourceCodeLength;
    }

    public double getAverageSolutionPreprocessedCodeLength() {
        return averageSolutionPreprocessedCodeLength;
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


    public void addSolution(Solution solution) {
        solutions.add(solution);
    }


}
