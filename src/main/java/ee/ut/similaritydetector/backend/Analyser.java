package main.java.ee.ut.similaritydetector.backend;

import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Analyser extends Task<Void> {

    private final Path zipDirectoryPath;

    private Map<String, List<Solution>> solutions;

    private int solutionCount;
    private int solutionsAnalysed;

    public Analyser(Path zipDirectoryPath) {
        this.zipDirectoryPath = zipDirectoryPath;
        solutionCount = 0;
        solutionsAnalysed = 0;
    }

    @Override
    protected Void call() {
        startAnalysis();
        return null;
    }

    public void startAnalysis() {
        ZipParser zipParser = new ZipParser(zipDirectoryPath);
        solutions = zipParser.parseSolutions();

        for (String key : solutions.keySet()) {
            solutionCount += solutions.get(key).size();
        }

        for (String key : solutions.keySet()) {
            for (Solution solution : solutions.get(key)) {
                //TODO: similarity analysis

                solutionsAnalysed += 1;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateProgress(solutionsAnalysed, solutionCount);
            }
        }

    }
}
