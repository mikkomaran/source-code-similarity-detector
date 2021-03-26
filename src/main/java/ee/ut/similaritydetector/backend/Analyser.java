package main.java.ee.ut.similaritydetector.backend;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static main.java.ee.ut.similaritydetector.backend.LevenshteinDistance.normalisedLevenshteinSimilarity;

public class Analyser extends Task<Void> {

    private double similarityThreshold = -1;
    private final boolean preprocessSourceCode;
    private final File zipDirectory;
    private final List<SimilarSolutionPair> similarSolutionPairs;
    private final List<SimilarSolutionCluster> similarSolutionClusters;
    private List<Exercise> exercises;

    private int totalSolutionPairsCount;
    private int analysedSolutionPairsCount;
    private int similarCount;

    public Analyser(File zipDirectory, boolean preprocessSourceCode) {
        this.zipDirectory = zipDirectory;
        this.preprocessSourceCode = preprocessSourceCode;
        exercises = new ArrayList<>();
        similarSolutionPairs = new ArrayList<>();
        similarSolutionClusters = new ArrayList<>();
    }

    public Analyser(File zipDirectory, double similarityThreshold, boolean preprocessSourceCode) {
        this.zipDirectory = zipDirectory;
        this.similarityThreshold = similarityThreshold;
        this.preprocessSourceCode = preprocessSourceCode;
        exercises = new ArrayList<>();
        similarSolutionPairs = new ArrayList<>();
        similarSolutionClusters = new ArrayList<>();
    }

    public List<SimilarSolutionCluster> getSimilarSolutionClusters() {
        return similarSolutionClusters;
    }

    @Override
    protected Void call() {
        updateProgress(0,100);
        startAnalysis();
        return null;
    }

    public void startAnalysis() {
        SolutionParser solutionParser = new SolutionParser(zipDirectory, preprocessSourceCode);
        exercises = solutionParser.parseSolutions();
        for (Exercise exercise : exercises) {
            // If user chose a custom similarity threshold then it is used, otherwise it is calculated
            if (similarityThreshold != -1) {
                exercise.setSimilarityThreshold(similarityThreshold);
            } else {
                exercise.calculateSimilarityThreshold();
            }
            // Finds the total number of pairs that will be compared
            int exerciseSolutionCount = exercise.getSolutions().size();
            totalSolutionPairsCount += exerciseSolutionCount * (exerciseSolutionCount - 1) / 2;
            // Finds the average solution length for this exercise
            exercise.findAverageSolutionSourceCodeLength();
//            exercise.findAverageSolutionPreprocessedCodeLength();
//            System.out.println(exercise.getName() + " - " + exercise.getAverageSolutionSourceCodeLength() + " preprocessed codes");
//            System.out.println(exercise.getName() + " - " + exercise.getAverageSolutionPreprocessedCodeLength() + " source codes");
//            System.out.println("Preprocessing removed on average " + Math.round(exercise.getAverageSolutionSourceCodeLength()
//                    - exercise.getAverageSolutionPreprocessedCodeLength()) + " characters per solution for exercise " + exercise.getName());
        }
        System.out.println("Total solution pairs: " + totalSolutionPairsCount);

        // Performing the pairwise comparison of solutions for each exercise
        exercises.forEach(this::compareSolutions);
        System.out.println("Analysed solution pairs: " + analysedSolutionPairsCount);
        System.out.println("Similar pairs found: " + similarCount);

        clusterSimilarPairs();
        System.out.println("Similar clusters: " + similarSolutionClusters.size());
        similarSolutionClusters.forEach(cluster -> System.out.println(cluster.toString()));
        System.out.println();
    }

    /**
     * Performs pairwise comparison of all solutions of an exercise.
     *
     * @param exercise the {@code Exercise} on which the comparison is performed
     */
    private void compareSolutions(Exercise exercise) {
        List<Solution> solutions = exercise.getSolutions();
        for (int i = 0, solutionCount = solutions.size(); i < solutionCount; i++) {
            Solution solution1 = solutions.get(i);
            for (int j = i + 1; j < solutionCount; j++) {
                Solution solution2 = solutions.get(j);
                double similarity = findSimilarity(solution1, solution2, exercise.getSimilarityThreshold());
                if (similarity > exercise.getSimilarityThreshold()) {
                    similarCount++;
                    solution1.addSimilarSolution(solution2);
                    solution2.addSimilarSolution(solution1);
                    similarSolutionPairs.add(new SimilarSolutionPair(similarity, solution1, solution2));
                    System.out.println(solution1.getExerciseName() + ": " + solution1.getAuthor() + ", " +
                            solution2.getAuthor() + " - " + String.format("%.1f%%", similarity * 100));
                }
                analysedSolutionPairsCount++;
                updateProgress(totalSolutionPairsCount / 2.0 + analysedSolutionPairsCount / 2.0, totalSolutionPairsCount);
            }
        }
    }

    /**
     * @param sol1
     * @param sol2
     * @return
     */
    private double findSimilarity(Solution sol1, Solution sol2, double similarityThreshold) {
        double similarity;
        String sol1Code = readSolutionCode(sol1, false);
        String sol2Code = readSolutionCode(sol2, false);
        try {
            similarity = normalisedLevenshteinSimilarity(sol1Code, sol2Code, (float) similarityThreshold);
        } catch (NullPointerException e) {
            // If at least one of the solution codes could not be read
            similarity = 0;
        }
        return similarity;
    }

    /**
     *
     *
     * @param solution
     * @param sourceCodePrioritised
     * @return
     */
    public static String readSolutionCode(Solution solution, boolean sourceCodePrioritised) {
        String solutionCode;
        Path first = null;
        Path second = null;
        if (sourceCodePrioritised) {
            first = solution.getSourceCodeFile().toPath();
            if(solution.getPreprocessedCodeFile() != null)
                second = solution.getPreprocessedCodeFile().toPath();
        } else {
            if(solution.getPreprocessedCodeFile() != null)
                first = solution.getPreprocessedCodeFile().toPath();
            second = solution.getSourceCodeFile().toPath();
        }
        // We try to read the higher priority code file
        try {
            solutionCode = Files.readString(first, StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            // If the higher priority code cannot be read or the file doesn't exist
            // the second priority file is the fallback
            try {
                solutionCode = Files.readString(second, StandardCharsets.UTF_8);
            } catch (IOException | NullPointerException exception) {
                // If the second priority file also cannot be read or doesn't exist
                solutionCode = null;
            }
        }
        return solutionCode;
    }

    public static String readCode(Solution solution) {
        String solutionCode;
        // We try to read the preprocessed code file
        try {
            solutionCode = Files.readString(solution.getSourceCodeFile().toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // If the preprocessed solution code cannot be read or the file doesn't exist
            // the source code file is the fallback
            try {
                solutionCode = Files.readString(solution.getPreprocessedCodeFile().toPath(), StandardCharsets.UTF_8);
            } catch (IOException ioException) {
                // If the source code file also cannot be read or doesn't exist
                solutionCode = null;
            }
        }
        return solutionCode;
    }

    private void clusterSimilarPairs() {
        for (SimilarSolutionPair pair1 : similarSolutionPairs) {
            Solution sol1 = pair1.getFirstSolution();
            Solution sol2 = pair1.getSecondSolution();
            SimilarSolutionCluster cluster = null;
            // If both solutions are not in a cluster
            if (similarSolutionClusters.stream().noneMatch(c -> c.containsSolution(sol1)) &&
                    similarSolutionClusters.stream().noneMatch(c -> c.containsSolution(sol2))) {
                cluster = new SimilarSolutionCluster(sol1.getExerciseName(), sol1, sol2);
            }
            // If only first solution is in a cluster
            else if (similarSolutionClusters.stream().anyMatch(c -> c.containsSolution(sol1)) && similarSolutionClusters.stream().noneMatch(c -> c.containsSolution(sol2))) {
                SimilarSolutionCluster existingCluster = similarSolutionClusters.stream().filter(x -> x.getSolutions().contains(sol1)).findAny().get();
                existingCluster.addSolution(sol2);
                existingCluster.addSolutionPair(pair1);
            }
            // If only second solution is in a cluster
            else if (similarSolutionClusters.stream().noneMatch(c -> c.containsSolution(sol1)) && similarSolutionClusters.stream().anyMatch(c -> c.containsSolution(sol2))) {
                SimilarSolutionCluster existingCluster = similarSolutionClusters.stream().filter(x -> x.getSolutions().contains(sol2)).findAny().get();
                existingCluster.addSolution(sol1);
                existingCluster.addSolutionPair(pair1);
            }
            // If both are in a cluster
            else if (similarSolutionClusters.stream().anyMatch(c -> c.containsSolution(sol1)) && similarSolutionClusters.stream().anyMatch(c -> c.containsSolution(sol2))) {
                SimilarSolutionCluster existingCluster1 = similarSolutionClusters.stream().filter(x -> x.getSolutions().contains(sol1)).findAny().get();
                SimilarSolutionCluster existingCluster2 = similarSolutionClusters.stream().filter(x -> x.getSolutions().contains(sol2)).findAny().get();
                if (existingCluster1 == existingCluster2) {
                    existingCluster1.addSolutionPair(pair1);
                } else {
                    existingCluster1.addSolution(sol2);
                    existingCluster1.addSolutionPair(pair1);
                    existingCluster2.addSolution(sol1);
                    existingCluster2.addSolutionPair(pair1);
                }
            }
            if (cluster != null) {
                cluster.addSolutionPair(pair1);
                for (SimilarSolutionPair pair2 : similarSolutionPairs) {
                    if (pair1 == pair2) continue;
                    if (cluster.containsSolution(pair2.getFirstSolution())) {
                        cluster.addSolution(pair2.getSecondSolution());
                        cluster.addSolutionPair(pair2);
                    } else if (cluster.containsSolution(pair2.getSecondSolution())) {
                        cluster.addSolution(pair2.getFirstSolution());
                        cluster.addSolutionPair(pair2);
                    }
                }
                similarSolutionClusters.add(cluster);
            }
        }
        for (SimilarSolutionCluster cluster : similarSolutionClusters) {
            cluster.createName();
        }
    }

}
