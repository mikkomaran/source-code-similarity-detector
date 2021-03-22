package main.java.ee.ut.similaritydetector.backend;

import java.util.*;

import static main.java.ee.ut.similaritydetector.backend.Analyser.readSolutionCode;

public class Exercise {

    private final String name;
    private final List<Solution> solutions;
    private double averageSolutionLength;

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

    public double getAverageSolutionLength() {
        return averageSolutionLength;
    }

    public void findAverageSolutionLength() {
        float avgSolutionLength = 0;
        int solutionCount = solutions.size();
        for (Solution solution : solutions) {
            avgSolutionLength += readSolutionCode(solution).length();
        }
        avgSolutionLength /= solutionCount;
        averageSolutionLength = avgSolutionLength;
    }

    public void addSolution(Solution solution) {
        solutions.add(solution);
    }
}
