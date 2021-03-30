package ee.ut.similaritydetector.backend;

import java.util.Arrays;

public class LevenshteinDistance {

    /**
     * <p>Adapted from: https://github.com/tdebatty/java-string-similarity/blob/master/src/main/java/info/debatty/java/stringsimilarity/Levenshtein.java [06.03.2021]</p>
     * <p>Calculates the customised Levenshtein distance metric of two strings.</p>
     * <p>Customisations made:
     * <ol><li></li></ol></p>
     * <p>There are two optimizations made:
     * <ol><li>The strings are presented as char arrays for improved efficiency.</li>
     * <li>If the distance metric surpasses the given threshold, the algorithm is halted.</li></ol></p>
     *
     * @param s1        char array of string 1
     * @param s2        char array of string 2
     * @param threshold the max difference threshold
     * @return the Levenshtein distance between s1 and s2
     */
    public static double distance(char[] s1, char[] s2, int threshold) {
        if (s1 == null || s2 == null) {
            throw new NullPointerException("Strings cannot be null");
        }

        if (Arrays.equals(s1, s2)) {
            return 0;
        }

        // String lengths
        int n = s1.length;
        int m = s2.length;

        //
        double[] prevCosts = new double[m + 1];
        double[] currCosts = new double[m + 1];
        double[] temp;

        // initialize previous row of distances
        // this row is A[0][i]: edit distance for an empty s
        // the distance is just the number of characters to delete from t
        for (int i = 0; i < prevCosts.length; i++) {
            prevCosts[i] = i;
        }

        for (int i = 0; i < n; i++) {
            // calculate current row distances from the previous row
            // first element is A[i+1][0]
            //   edit distance is delete (i+1) chars from s to match empty t
            currCosts[0] = i + 1;

            double minCost = currCosts[0];

            // Find the costs for the current row to get the minimum edit cost
            for (int j = 0; j < m; j++) {
                currCosts[j + 1] = findCost(s1, s2, prevCosts, currCosts, i, j);
                minCost = Math.min(minCost, currCosts[j + 1]);
            }

            // If the distance threshold is passed
            if (minCost >= threshold) {
                return -1;
            }

            // Swap previous and current row
            temp = prevCosts;
            prevCosts = currCosts;
            currCosts = temp;
        }
        return prevCosts[m];
    }

    /**
     * Finds the minimal edit cost for the current characters
     *
     * @param s1 char array of string 1
     * @param s2 char array of string 2
     * @param prevCosts previous row of costs
     * @param currCosts current row of costs
     * @param i current character index of string 1
     * @param j current character index of string 2
     * @return the cost for the edit
     */
    public static double findCost(char[] s1, char[] s2, double[] prevCosts, double[] currCosts, int i, int j) {
        // Initial substitution cost is 1
        double subCost = 1;
        // Same characters
        if (s1[i] == s2[j]) {
            subCost = 0;
        }
        // Char case difference
        else if (Character.toLowerCase(s1[i]) == s2[j] && Character.toLowerCase(s2[j]) != s2[j] ||
                Character.toLowerCase(s2[j]) == s1[i] && Character.toLowerCase(s1[i]) != s1[i]) {
            subCost = 0.2;
        }
        // Char substitution cost special cases
        else if (charSubstitutionCondition(s1[i], s2[j], '\"', '\'') ||
                charSubstitutionCondition(s1[i], s2[j], 'o', 'õ') ||
                charSubstitutionCondition(s1[i], s2[j], 'o', 'ö') ||
                charSubstitutionCondition(s1[i], s2[j], 'a', 'ä') ||
                charSubstitutionCondition(s1[i], s2[j], 'u', 'ü') ||
                charSubstitutionCondition(s1[i], s2[j], 'y', 'ü') ||
                charSubstitutionCondition(s1[i], s2[j], 's', 'š') ||
                charSubstitutionCondition(s1[i], s2[j], 'z', 'ž')) {
            subCost = 0.3;
        }

        double swapCost = 1;

        return Math.min(
                currCosts[j] + 1,                 // Cost of insertion
                Math.min(
                        prevCosts[j + 1] + 1,     // Cost of remove
                        prevCosts[j] + subCost)); // Cost of substitution
    }

    private static boolean charSubstitutionCondition(char curr1, char curr2, char sub1, char sub2) {
        return curr1 == sub1 && curr2 == sub2 || curr1 == sub2 && curr2 == sub1;
    }

    public static double findCost2(char[] s1, char[] s2, double[] prevCosts, double[] currCosts, int i, int j) {
        double cost = 1;
        if (s1[i] == s2[j]) {
            cost = 0;                          // Cost is 0 when same chars
        }
        // Special cases

        return Math.min(
                currCosts[j] + 1,              // Cost of insertion
                Math.min(
                        prevCosts[j + 1] + 1,  // Cost of remove
                        prevCosts[j] + cost)); // Cost of substitution
    }

    public static double normalisedLevenshteinSimilarity(String s1, String s2, float similarityThreshold) {
        int maxLength = Math.max(s1.length(), s2.length());
        int distanceThreshold = Math.round((1 - similarityThreshold) * maxLength);
        //double distance = levenshteinDistance(s1.toCharArray(), s2.toCharArray(), distanceThreshold);
        double distance = distance(s1.toCharArray(), s2.toCharArray(), distanceThreshold);
        if (distance == -1) {
            return 0;
        }
        return 1.0 - distance / maxLength;
    }

}