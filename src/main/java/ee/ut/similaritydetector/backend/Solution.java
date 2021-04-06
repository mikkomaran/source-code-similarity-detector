package ee.ut.similaritydetector.backend;

import org.python.util.PythonInterpreter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solution {

    private final String author;
    private final String exerciseName;
    private final File sourceCodeFile;
    private final Set<Solution> similarSolutions;
    private File preprocessedCodeFile;
    private File sourceCodeHTMLLight;
    private File sourceCodeHTMLDark;

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

    public File getSourceCodeHTMLLight() {
        return sourceCodeHTMLLight;
    }

    public File getSourceCodeHTMLDark() {
        return sourceCodeHTMLDark;
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

    /**
     * Reads the solution's code from either the {@link Solution#sourceCodeFile} or {@link Solution#preprocessedCodeFile},
     * depending on the priority.
     *
     * @param sourceCodePrioritised if {@code true}, tries to read {@link Solution#sourceCodeFile} first, if that fails then
     *                              falls back to reading {@link Solution#preprocessedCodeFile}, if {@code false} then priorities are reversed
     * @return {@link Solution#sourceCodeFile}, {@link Solution#preprocessedCodeFile} or {@code null} if no code file could be read
     */
    public String readSolutionCode(boolean sourceCodePrioritised) {
        String solutionCode;
        Path first = null;
        Path second = null;
        if (sourceCodePrioritised) {
            first = sourceCodeFile.toPath();
            if (preprocessedCodeFile != null)
                second = preprocessedCodeFile.toPath();
        } else {
            if (preprocessedCodeFile != null)
                first = preprocessedCodeFile.toPath();
            second = sourceCodeFile.toPath();
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

    public void generateSyntaxHighlightedHTML() {
        final String sourceCodePath = sourceCodeFile.getAbsolutePath();
        final String sourceCodePathWithoutFileExtension = sourceCodePath.substring(0, sourceCodePath.length() - 3);
        final String darkHTMLPath = sourceCodePathWithoutFileExtension + "_dark.html";
        final String lightHTMLPath = sourceCodePathWithoutFileExtension + "_light.html";
        final String syntaxHighlighterScript = "/ee/ut/similaritydetector/python/SyntaxHighlighter.py";

        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.set("source_code_filepath", sourceCodePath);
        interpreter.set("style", "native");
        interpreter.set("html_file_path", darkHTMLPath);
        interpreter.execfile(getClass().getResourceAsStream(syntaxHighlighterScript));
        sourceCodeHTMLDark = new File(darkHTMLPath);

        interpreter.set("style", "default");
        interpreter.set("html_file_path", lightHTMLPath);
        interpreter.execfile(getClass().getResourceAsStream(syntaxHighlighterScript));
        sourceCodeHTMLLight = new File(lightHTMLPath);
    }


}
