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


    public Map<String, List<Solution>> parseZip() {
        //List<Solution> solutions = new ArrayList<>();
        Map<String, List<Solution>> solutions = new HashMap<>();

        try (ZipFile zipDirectory = new ZipFile(contentDirectoryPath.toString())) {
            //System.out.println(String.format("Inspecting contents of: %s\n", zipDirectory.getName()));

            Enumeration<? extends ZipEntry> zipEntries = zipDirectory.entries();
            while (zipEntries.hasMoreElements()) {
                Solution solution = new Solution();
                ZipEntry zipEntry = zipEntries.nextElement();
                //System.out.println(zipEntry.getName());

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

                List<String> sourceCode = readSourceCode(zipDirectory, zipEntry);
                solution.setSourceCode(sourceCode);

                solutions.computeIfAbsent(solution.getSolutionName(), k -> new ArrayList<>()).add(solution);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return solutions;
    }

    /**
     * Reads the source code of a zipped solution file as a list of strings.
     *
     * @param zipDirectory ZIP directory where the solution file is
     * @param zipEntry     the {@code ZipEntry} of the solution file
     * @return list of strings of the solution's source code
     * @throws IOException
     */
    private List<String> readSourceCode(ZipFile zipDirectory, ZipEntry zipEntry) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zipDirectory.getInputStream(zipEntry)));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();
        return lines;
    }

}
