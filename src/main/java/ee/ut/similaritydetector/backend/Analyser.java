package main.java.ee.ut.similaritydetector.backend;

import java.io.File;

public class Analyser {

    private File contentDirectory;

    public Analyser(File contentDirectory) {
        this.contentDirectory = contentDirectory;
    }

    public File getContentDirectory() {
        return contentDirectory;
    }

    public void setContentDirectory(File contentDirectory) {
        this.contentDirectory = contentDirectory;
    }

    public void startAnalysis() {

    }

}
