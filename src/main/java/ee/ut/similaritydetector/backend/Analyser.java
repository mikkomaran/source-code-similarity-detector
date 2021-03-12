package main.java.ee.ut.similaritydetector.backend;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static main.java.ee.ut.similaritydetector.backend.LevenshteinDistance.normalisedLevenshteinSimilarity;

public class Analyser extends Task<Void> {

    public static final float SimilarityThreshold = 0.9f;
    private final File zipDirectory;
    private int totalSolutionPairsCount;
    private int analysedSolutionPairsCount;
    private int similarCount;

    public Analyser(File zipDirectory) {
        this.zipDirectory = zipDirectory;
        totalSolutionPairsCount = 0;
        analysedSolutionPairsCount = 0;
    }

    @Override
    protected Void call() {
        startAnalysis();
        return null;
    }

    public void startAnalysis() {
        SolutionParser solutionParser = new SolutionParser(zipDirectory);
        Map<String, List<Solution>> allSolutions = solutionParser.parseSolutions2();

        for (String key : allSolutions.keySet()) {
            int exerciseSolutionCount = allSolutions.get(key).size();
            totalSolutionPairsCount += exerciseSolutionCount * (exerciseSolutionCount - 1) / 2;
        }
        System.out.println("Total solution pairs: " + totalSolutionPairsCount);
        compareSolutions(allSolutions);
        System.out.println("Analysed solution pairs: " + analysedSolutionPairsCount);
        System.out.println("Similar pairs found: " + similarCount);
    }

    /**
     * Pairwise comparison of all solutions
     *
     * @param allSolutions parsed solutions as a {@code Map} where every exercise has a list of solutions
     */
    private void compareSolutions(Map<String, List<Solution>> allSolutions) {
        for (String key : allSolutions.keySet()) {
            List<Solution> singleExerciseSolutions = allSolutions.get(key);
            for (int i = 0, singleExerciseSolutionsSize = singleExerciseSolutions.size(); i < singleExerciseSolutionsSize; i++) {
                Solution solution1 = singleExerciseSolutions.get(i);
                for (int j = i + 1; j < singleExerciseSolutionsSize; j++) {
                    Solution solution2 = singleExerciseSolutions.get(j);
                    double similarity = findSimilarity(solution1, solution2);
                    if (similarity > Analyser.SimilarityThreshold) {
                        similarCount++;
                        solution1.similarSolutions.add(solution2);
                        solution2.similarSolutions.add(solution1);
                        System.out.println(solution1.getExerciseName() + ": " + solution1.getAuthor() + ", " + solution2.getAuthor() + " - " + similarity);
                    }
                    analysedSolutionPairsCount++;
                    updateProgress(analysedSolutionPairsCount, totalSolutionPairsCount);
                }
            }
        }
    }


    private double findSimilarity(Solution sol1, Solution sol2) {
        double similarity;
        String sol1Code = readSolutionCode(sol1);
        String sol2Code = readSolutionCode(sol2);
        try {
            similarity = normalisedLevenshteinSimilarity(sol1Code, sol2Code);
        } catch (NullPointerException e) {
            // If at least one of the solution codes could not be read
            similarity = 0;
        }
        return similarity;
    }

    /**
     * @param solution
     * @return
     */
    private String readSolutionCode(Solution solution) {
        String solutionCode;
        // We try to read the preprocessed code file
        try {
            solutionCode = Files.readString(solution.getPreprocessedCodeFile().toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // If the preprocessed solution code cannot be read or the file doesn't exist
            // the source code file is the fallback
            try {
                solutionCode = Files.readString(solution.getSourceCodeFile().toPath(), StandardCharsets.UTF_8);
            } catch (IOException ioException) {
                // If the source code file also cannot be read or doesn't exist
                solutionCode = null;
            }
        }
        return solutionCode;
    }


}
