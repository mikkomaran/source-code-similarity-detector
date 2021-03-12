package main.java.ee.ut.similaritydetector.backend;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class SolutionParser {

    private final File contentDirectory;
    private final File outputDirectory;

    public SolutionParser(File contentDirectory) {
        this.contentDirectory = contentDirectory;
        this.outputDirectory = new File("resources/");
    }

    public Map<String, List<Solution>> parseSolutions2() {
        Map<String, List<Solution>> solutions = new HashMap<>();

        // File unzipping taken from https://www.baeldung.com/java-compress-and-uncompress
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(contentDirectory), StandardCharsets.UTF_8)) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(outputDirectory, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    try {
                        Solution solution = parseSolution2(zipEntry, newFile);
                        solutions.computeIfAbsent(solution.getExerciseName(), k -> new ArrayList<>()).add(solution);
                    } catch (InvalidPathException | InterruptedException | IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return solutions;
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private Solution parseSolution2(ZipEntry zipEntry, File sourceCode) throws IOException, InterruptedException {
        Solution solution = new Solution();
        Pattern solutionPathPattern = Pattern.compile(".+_(.+)/(.+)");
        Matcher matcher = solutionPathPattern.matcher(zipEntry.getName());
        if (matcher.find()) {
            String author = matcher.group(1);
            String solutionName = matcher.group(2);
            solution.setAuthor(author);
            solution.setExerciseName(solutionName);
        } else {
            throw new InvalidPathException(zipEntry.getName(), " is an invalid file path.");
        }
        solution.setSourceCodeFile(sourceCode);
        removeCommentsPy(sourceCode.getAbsolutePath());
        String sourceCodePath = sourceCode.getAbsolutePath();
        File preProcessedCodeFile = new File(sourceCodePath.substring(0, sourceCodePath.length() - 3) + "_preprocessed.py");
        solution.setPreprocessedCodeFile(preProcessedCodeFile);
        return solution;
    }

    public void removeCommentsPy(String filePath) throws InterruptedException, IOException {
        String path = "src/main/python/ee/ut/similaritydetector/Preprocesser.py";
        String[] command = {"python", path, filePath};
        ProcessBuilder processBuilder = new ProcessBuilder(command).inheritIO();
        Process process = processBuilder.start();
        process.waitFor();
    }

    /**
     * Parses the zip folder of solutions as a map of lists, where
     * the key is the exercise name and the list contains all the
     * solutions to that exercise.
     *
     * @return parsed solutions as a {@code Map} where every exercise has a list of solutions
     */
    public Map<String, List<Solution>> parseSolutions() {
        Map<String, List<Solution>> solutions = new HashMap<>();

        try (ZipFile zipDirectory = new ZipFile(contentDirectory)) {
            Enumeration<? extends ZipEntry> zipEntries = zipDirectory.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                if (zipEntry.isDirectory())
                    continue;
                Solution solution = parseSolution(zipDirectory, zipEntry);
                solutions.computeIfAbsent(solution.getExerciseName(), k -> new ArrayList<>()).add(solution);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return solutions;
    }

    /**
     * Parses a {@code Solution} from the given {@code ZipEntry}.
     *
     * @param zipDirectory ZIP directory where the solution file is
     * @param zipEntry     the {@code ZipEntry} of the solution file
     * @return a {@code Solution} parsed from the given {@code ZipEntry}
     */
    private Solution parseSolution(ZipFile zipDirectory, ZipEntry zipEntry) {
        Solution solution = new Solution();
        Pattern solutionPathPattern = Pattern.compile(".+_(.+)/(.+)");
        Matcher matcher = solutionPathPattern.matcher(zipEntry.getName());
        if (matcher.find()) {
            String author = matcher.group(1);
            String solutionName = matcher.group(2);
            solution.setAuthor(author);
            solution.setExerciseName(solutionName);
        } else {
            System.out.println("Invalid filepath");
        }
        String sourceCode = parseSourceCode(zipDirectory, zipEntry);
        //List<String> sourceCodeLines = parseSourceCodeLines(zipDirectory, zipEntry);
        solution.setSourceCode(sourceCode);
        //solution.setSourceCodeLines(sourceCodeLines);

        return solution;
    }

    /**
     * Parses the source code of a zipped solution file as a list of strings,
     * where each code line is a string in the list.
     *
     * @param zipDirectory ZIP directory where the solution file is
     * @param zipEntry     the {@code ZipEntry} of the solution file
     * @return list of strings of the solution's source code
     */
    private List<String> parseSourceCodeLines(ZipFile zipDirectory, ZipEntry zipEntry) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zipDirectory.getInputStream(zipEntry)))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private String parseSourceCode(ZipFile zipDirectory, ZipEntry zipEntry) {
        StringBuilder sourceCode = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zipDirectory.getInputStream(zipEntry)))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sourceCode.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sourceCode.toString();
    }


}
