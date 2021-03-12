package main.java.ee.ut.similaritydetector.backend;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Preprocesser {

    public Preprocesser() {
    }

    private String preprocessCode(String sourceCode) {
        String preprocessedCode = removeComments(sourceCode);
        preprocessedCode = removeTrailingWhitespaceAndEmptyLines(preprocessedCode);
        return preprocessedCode;
    }

    public String removeComments(String sourceCode) {
        boolean inStringLiteral = false;
        Pattern lineCommentPattern = Pattern.compile(".*(#.*\\r?\\n)");
        Matcher lineCommentMatcher = lineCommentPattern.matcher(sourceCode);
        String preprocessedCode = sourceCode;
        while (lineCommentMatcher.find()) {
            String lineComment = lineCommentMatcher.group(1);
            System.out.println(lineComment);
            preprocessedCode = preprocessedCode.replace(lineComment, System.lineSeparator());
        }
        Pattern docstringCommentPattern = Pattern.compile("(?m)(''')|(\"\"\").*(''')|(\"\"\")");
        preprocessedCode = preprocessedCode.replaceAll("(?m)(''')|(\"\"\").*(''')|(\"\"\")", " ");
        return preprocessedCode;
    }

    public String removeTrailingWhitespaceAndEmptyLines(String sourceCode) {
        return sourceCode.replaceAll("\\s*\r?\n", "\n");
    }

    public void removeCommentsPy(String filePath) throws InterruptedException, IOException {
        String path = "src/main/python/ee/ut/similaritydetector/Preprocesser.py";
        ProcessBuilder processBuilder = new ProcessBuilder("python", path, filePath).inheritIO();
        Process process = processBuilder.start();
        process.waitFor();
    }

}
