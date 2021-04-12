# Source code similarity detector
## Description 
JavaFX application for detecting similarity between Python source code files using Levenshtein distance as a metric. Presents the results as similar clusters and pairs.
## Requirements
* Java JDK version 11+
* Tested & working on Windows OS 
## Running the application
Download the ***Source.code.similarity.detector.jar*** file from the latest [release](https://github.com/mikkomaran/source-code-similarity-detector/releases) and run the executable JAR. If double-clicking the JAR doesn't start the program then try running the the following commands from the command line:
*  ***javaw -jar "path/to/file/Source_code_similarity_detector.jar"***
* ***javaw -cp "path/to/file/Source_code_similarity_detector.jar" ee.ut.similaritydetector.ui.App***
## Input files
The application takes input as a ZIP file that is generated from Moodle Virtual Programming Lab (VPL) submissions. The ZIP file contains folders for each student named by the student's name, user number and Moodle username separated with spaces (i.e. "Maran Mikko 999999 xxxx"). Inside student folders are one or multiple submission folders, named by the date and time of that submission. Each submission folder contains all submitted files (i.e. "exercise1.py", "exercise2.py",...). Only the latest submission's files are taken into the analysis.
## Features
* **Custom similarity threshold** - the user can select a **similarity threshold**, that is the percentage of similarity for two solutions to be considered *suspiciously similar*
* **Preproccesing source code files** - all comments and empty lines will be removed from source codes before starting the analysis
* **Anonymous results** - the results are presented by student user numbers rather than names
* **Code review window** - allows reviewing the source codes of *suspicious solutions* with syntax highlighting
* **Analysis statistics** 
* **Light & dark theme** 
* **2 languages for GUI** - Estonian & English
