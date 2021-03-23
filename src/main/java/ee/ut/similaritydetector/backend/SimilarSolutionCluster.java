package main.java.ee.ut.similaritydetector.backend;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SimilarSolutionCluster {

    private final Set<Solution> solutions;
    private final String exerciseName;
    private final Set<SimilarSolutionPair> solutionPairs;
    private String name;

    public SimilarSolutionCluster(String exerciseName, Solution... solutions) {
        this.exerciseName = exerciseName;
        Set<Solution> sols = new HashSet<>();
        Collections.addAll(sols, solutions);
        this.solutions = sols;
        this.solutionPairs = new HashSet<>();
    }

    public Set<Solution> getSolutions() {
        return solutions;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public Set<SimilarSolutionPair> getSolutionPairs() {
        return solutionPairs;
    }

    public void addSolution(Solution solution) {
        solutions.add(solution);
    }

    public boolean containsSolution(Solution solution) {
        return solutions.contains(solution);
    }

    public void addSolutionPair(SimilarSolutionPair pair) {
        solutionPairs.add(pair);
    }

    public void createName() {
        StringBuilder sb = new StringBuilder(exerciseName + " - ");
        for (Solution solution : solutions) {
            sb.append(solution.getAuthor()).append(", ");
        }
        this.name = sb.substring(0, sb.length() - 2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cluster of " + solutions.size() + " for exercise " + exerciseName + ":" + System.lineSeparator());
        for (Solution sol : solutions) {
            sb.append(sol.getAuthor()).append(System.lineSeparator());
        }
        return sb.toString();
    }

    public String getName() {
        return name;
    }
}
