package ee.ut.similaritydetector.backend;

import javafx.concurrent.Task;
import ee.ut.similaritydetector.ui.controllers.MainViewController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static ee.ut.similaritydetector.backend.LevenshteinDistance.normalisedLevenshteinSimilarity;

public class Analyser extends Task<Void> {

    private final boolean preprocessSourceCode;
    private final boolean anonymousResults;
    private final File zipDirectory;
    private final List<SimilarSolutionPair> similarSolutionPairs;
    private final List<SimilarSolutionCluster> similarSolutionClusters;
    private List<Exercise> exercises;
    private double similarityThreshold = -1;

    private int totalSolutionPairsCount;
    private int analysedSolutionPairsCount;
    private double analysisDuration;

    private final MainViewController mainViewController;

    public Analyser(File zipDirectory, boolean preprocessSourceCode, boolean anonymousResults, MainViewController mainViewController) {
        this.zipDirectory = zipDirectory;
        this.preprocessSourceCode = preprocessSourceCode;
        this.anonymousResults = anonymousResults;
        this.mainViewController = mainViewController;
        exercises = new ArrayList<>();
        similarSolutionPairs = new ArrayList<>();
        similarSolutionClusters = new ArrayList<>();
    }

    public Analyser(File zipDirectory, double similarityThreshold, boolean preprocessSourceCode, boolean anonymousResults, MainViewController mainViewController) {
        this.zipDirectory = zipDirectory;
        this.similarityThreshold = similarityThreshold;
        this.preprocessSourceCode = preprocessSourceCode;
        this.anonymousResults = anonymousResults;
        this.mainViewController = mainViewController;
        exercises = new ArrayList<>();
        similarSolutionPairs = new ArrayList<>();
        similarSolutionClusters = new ArrayList<>();
    }

    public File getZipDirectory() {
        return zipDirectory;
    }

    public List<SimilarSolutionPair> getSimilarSolutionPairs() {
        return similarSolutionPairs;
    }

    public List<SimilarSolutionCluster> getSimilarSolutionClusters() {
        return similarSolutionClusters;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public int getAnalysedSolutionPairsCount() {
        return analysedSolutionPairsCount;
    }

    public double getAnalysisDuration() {
        return analysisDuration;
    }

    public void setAnalysisDuration(double analysisDuration) {
        this.analysisDuration = analysisDuration;
    }

    @Override
    protected Void call() {
        updateProgress(0,100);
        startAnalysis();
        return null;
    }

    public void updateProcessingProgress(int done, int total) {
            updateProgress(done * 0.3, total);
    }

    private void updateAnalysingProgress() {
            updateProgress(totalSolutionPairsCount * 0.3 + analysedSolutionPairsCount * 0.6, totalSolutionPairsCount);
    }

    private void updateGeneratingProgress(int done, int total) {
        updateProgress(total * 0.9 + done * 0.1, total);
    }

    public void startAnalysis() {
        mainViewController.setProgressText("Processing solutions...");
        SolutionParser solutionParser = new SolutionParser(zipDirectory, preprocessSourceCode, anonymousResults, this);
        exercises = solutionParser.parseSolutions();

        mainViewController.setProgressText("Analysing solutions...");
        analyseSolutions();

        // Performing the pairwise comparison of solutions for each exercise
        exercises.forEach(this::compareSolutions);

        // Clustering similar pairs
        clusterSimilarPairs();

        // Generating syntax highlighted solution code HTML
        generateSyntaxHighlightingHTML();
    }

    /**
     * For each exercise finds the average solution length of source code and preprocessed code
     * (if preprocessing is done) and sets similarity threshold, if custom threshold is not used,
     * then calculates a similarity threshold based on solution length.
     */
    private void analyseSolutions() {
        for (Exercise exercise : exercises) {
            // Finds the average solution length for this exercise
            exercise.findAverageSolutionSourceCodeLength();
            System.out.println("\n" + exercise.getName() + " - Average length of source codes: " + exercise.getAverageSolutionSourceCodeLength() );
            if (preprocessSourceCode){
                exercise.findAverageSolutionPreprocessedCodeLength();
                System.out.println(exercise.getName() + " - Average length of preprocessed codes: " + exercise.getAverageSolutionPreprocessedCodeLength());
                System.out.println(exercise.getName() + " - Preprocessing removed on average " + Math.round(exercise.getAverageSolutionSourceCodeLength()
                        - exercise.getAverageSolutionPreprocessedCodeLength()) + " characters per solution");
            }

            // If user chose a custom similarity threshold then it is used, otherwise it is calculated
            if (similarityThreshold != -1) {
                exercise.setSimilarityThreshold(similarityThreshold);
            } else {
                exercise.calculateSimilarityThreshold();
            }
            // Finds the total number of pairs that will be compared
            int exerciseSolutionCount = exercise.getSolutions().size();
            totalSolutionPairsCount += exerciseSolutionCount * (exerciseSolutionCount - 1) / 2;
        }
    }

    /**
     * Performs pairwise comparison of all solutions of an exercise.
     *
     * @param exercise the {@code Exercise} on which the comparison is performed
     */
    private void compareSolutions(Exercise exercise) {
        System.out.println("\nSimilar pairs for " + exercise.getName() + ":");
        List<Solution> solutions = exercise.getSolutions();
        for (int i = 0, solutionCount = solutions.size(); i < solutionCount; i++) {
            Solution solution1 = solutions.get(i);
            for (int j = i + 1; j < solutionCount; j++) {
                Solution solution2 = solutions.get(j);
                double similarity = findSimilarity(solution1, solution2, exercise.getSimilarityThreshold());
                if (similarity > exercise.getSimilarityThreshold()) {
                    solution1.addSimilarSolution(solution2);
                    solution2.addSimilarSolution(solution1);
                    similarSolutionPairs.add(new SimilarSolutionPair(similarity, solution1, solution2));
                    System.out.println("\t" + String.format("%.1f%%", similarity * 100) + "\t" +
                            solution1.getAuthor() + ", " + solution2.getAuthor() );
                }
                analysedSolutionPairsCount++;
                updateAnalysingProgress();
            }
        }
    }

    /**
     * Finds the similarity between two given solutions based on Levenshtein distance.
     *
     * @param sol1 {@link Solution} nr 1
     * @param sol2 {@link Solution} nr 2
     * @return the similarity of the two solutions in the range 0 to 1 (both inclusive),
     *          0 meaning no similarities between the two solutions and 1 meaning solutions are identical.
     */
    private double findSimilarity(Solution sol1, Solution sol2, double similarityThreshold) {
        double similarity;
        String sol1Code = sol1.readSolutionCode(false);
        String sol2Code = sol2.readSolutionCode(false);
        try {
            similarity = normalisedLevenshteinSimilarity(sol1Code, sol2Code, (float) similarityThreshold);
        } catch (NullPointerException e) {
            // If at least one of the solution codes could not be read
            similarity = 0;
        }
        return similarity;
    }

    /**
     * Clusters all found similar solution pairs based on the assumption
     * that given solutions A, B and C, if A is similar to B and A is similar to C
     * then A, B and C all belong to the same cluster.
     */
    private void clusterSimilarPairs() {
        for (SimilarSolutionPair pair1 : similarSolutionPairs) {
            Solution sol1 = pair1.getFirstSolution();
            Solution sol2 = pair1.getSecondSolution();
            SimilarSolutionCluster cluster = null;
            // If both solutions are not in an existing cluster
            if (similarSolutionClusters.stream().noneMatch(c -> c.containsSolution(sol1)) &&
                    similarSolutionClusters.stream().noneMatch(c -> c.containsSolution(sol2))) {
                cluster = new SimilarSolutionCluster(sol1.getExerciseName(), sol1, sol2);
            }
            // If only first solution is in an existing cluster
            else if (similarSolutionClusters.stream().anyMatch(c -> c.containsSolution(sol1)) && similarSolutionClusters.stream().noneMatch(c -> c.containsSolution(sol2))) {
                SimilarSolutionCluster existingCluster = similarSolutionClusters.stream().filter(x -> x.getSolutions().contains(sol1)).findAny().get();
                existingCluster.addSolution(sol2);
                existingCluster.addSolutionPair(pair1);
            }
            // If only second solution is in an existing cluster
            else if (similarSolutionClusters.stream().noneMatch(c -> c.containsSolution(sol1)) && similarSolutionClusters.stream().anyMatch(c -> c.containsSolution(sol2))) {
                SimilarSolutionCluster existingCluster = similarSolutionClusters.stream().filter(x -> x.getSolutions().contains(sol2)).findAny().get();
                existingCluster.addSolution(sol1);
                existingCluster.addSolutionPair(pair1);
            }
            // If both are in an existing cluster
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

    /**
     * Generates syntax highlighting HTML for each suspicious solution.
     */
    private void generateSyntaxHighlightingHTML() {
        mainViewController.setProgressText("Preparing results...");
        int total = similarSolutionClusters.stream().mapToInt(cluster -> cluster.getSolutions().size()).sum();
        AtomicInteger done = new AtomicInteger();
        similarSolutionClusters.forEach(cluster -> cluster.getSolutions().forEach(solution -> {
            try {
                solution.generateSyntaxHighlightedHTML();
            } catch (Exception e) {
                e.printStackTrace();
            }
            done.getAndIncrement();
            updateGeneratingProgress(done.get(), total);
        }));
    }

}
