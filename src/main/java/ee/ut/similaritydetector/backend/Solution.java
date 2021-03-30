package ee.ut.similaritydetector.backend;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solution {

    private final String author;
    private final String exerciseName;
    private final File sourceCodeFile;
    private File preprocessedCodeFile;

    private final Set<Solution> similarSolutions;

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

    public File getSourceCodeFile() {
        return sourceCodeFile;
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

    /**
     * Reads the source code file lines as a list of strings.
     *
     * @return {@code List} of strings of the solution's source code lines
     */
    public List<String> getSourceCodeLines() throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceCodeFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new IOException("Unable to read file: " + sourceCodeFile.getCanonicalPath());
        }
        return lines;
    }

}
