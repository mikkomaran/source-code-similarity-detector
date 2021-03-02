package main.java.ee.ut.similaritydetector.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipParser {

    private final Path contentDirectoryPath;

    public ZipParser(Path contentDirectoryPath) {
        this.contentDirectoryPath = contentDirectoryPath;
    }

    /**
     * Parses the zip folder of solutions as a map of lists, where
     * the key is the exercise name and the list contains all the
     * solutions to that exercise.
     *
     * @return parsed solutions as a {@code Map}
     */
    public Map<String, List<Solution>> parseSolutions() {
        Map<String, List<Solution>> solutions = new HashMap<>();

        try (ZipFile zipDirectory = new ZipFile(contentDirectoryPath.toString())) {
            //System.out.println(String.format("Inspecting contents of: %s\n", zipDirectory.getName()));

            Enumeration<? extends ZipEntry> zipEntries = zipDirectory.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                if (zipEntry.isDirectory())
                    continue;
                //System.out.println(zipEntry.getName());

                Solution solution = parseSolution(zipDirectory, zipEntry);
                solutions.computeIfAbsent(solution.getSolutionName(), k -> new ArrayList<>()).add(solution);
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
     * @param zipEntry the {@code ZipEntry} of the solution file
     * @return a {@code Solution} parsed from the given {@code ZipEntry}
     */
    private Solution parseSolution(ZipFile zipDirectory, ZipEntry zipEntry){
        Solution solution = new Solution();
        Pattern solutionPathPattern = Pattern.compile(".+_(.+)/(.+)");
        Matcher matcher = solutionPathPattern.matcher(zipEntry.getName());
        if (matcher.find()) {
            String author = matcher.group(1);
            String solutionName = matcher.group(2);
            solution.setAuthor(author);
            solution.setSolutionName(solutionName);
        } else {
            System.out.println("Invalid filepath");
        }
        List<String> sourceCode = parseSourceCode(zipDirectory, zipEntry);
        solution.setSourceCode(sourceCode);

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
    private List<String> parseSourceCode(ZipFile zipDirectory, ZipEntry zipEntry) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zipDirectory.getInputStream(zipEntry)))){
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

}
