package main.java.ee.ut.similaritydetector.backend;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solution {

    private String author;
    private String exerciseName;
    private File sourceCodeFile;
    private File preprocessedCodeFile;

    private final Set<Solution> similarSolutions;

    public Solution() {
        similarSolutions = new HashSet<>();
    }

    public Solution(String author, String exerciseName, File sourceCodeFile) {
        this.author = author;
        this.exerciseName = exerciseName;
        this.sourceCodeFile = sourceCodeFile;
        similarSolutions = new HashSet<>();
    }

    public Set<Solution> getSimilarSolutions() {
        return similarSolutions;
    }

    public void addSimilarSolution(Solution solution) {
        similarSolutions.add(solution);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public File getSourceCodeFile() {
        return sourceCodeFile;
    }

    public void setSourceCodeFile(File sourceCodeFile) {
        this.sourceCodeFile = sourceCodeFile;
    }

    public File getPreprocessedCodeFile() {
        return preprocessedCodeFile;
    }

    public void setPreprocessedCodeFile(File preprocessedCodeFile) {
        this.preprocessedCodeFile = preprocessedCodeFile;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

}
