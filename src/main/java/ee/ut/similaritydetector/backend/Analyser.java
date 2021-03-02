package main.java.ee.ut.similaritydetector.backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

public class Analyser {

    private final Path zipDirectoryPath;

    public Analyser(Path zipDirectoryPath) {
        this.zipDirectoryPath = zipDirectoryPath;
    }

    public void startAnalysis() {
        ZipParser zipParser = new ZipParser(zipDirectoryPath);
        Map<String, List<Solution>> solutions = zipParser.parseZip();
        System.out.println();
    }

}
