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

    private String sourceCode;
    private String preprocessedCode;
    private List<String> sourceCodeLines;

    public Set<Solution> similarSolutions;

    public Solution() {
        similarSolutions = new HashSet<>();
    }

    public Set<Solution> getSimilarSolutions() {
        return similarSolutions;
    }

    public void setSimilarSolutions(Set<Solution> similarSolutions) {
        this.similarSolutions = similarSolutions;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPreprocessedCode() {
        return preprocessedCode;
    }

    public void setPreprocessedCode(String preprocessedCode) {
        this.preprocessedCode = preprocessedCode;
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

    public List<String> getSourceCodeLines() {
        return sourceCodeLines;
    }

    public void setSourceCodeLines(List<String> sourceCodeLines) {
        this.sourceCodeLines = sourceCodeLines;
    }

}
