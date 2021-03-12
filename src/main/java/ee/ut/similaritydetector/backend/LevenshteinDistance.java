package main.java.ee.ut.similaritydetector.backend;

import java.util.Arrays;

/**
 * The Levenshtein distance between two words is the minimum number of
 * single-character edits (insertions, deletions or substitutions) required to
 * change one string into the other.
 *
 * @author Thibault Debatty
 */

public class LevenshteinDistance {

    /**
     * The Levenshtein distance, or edit distance, between two words is the
     * minimum number of single-character edits (insertions, deletions or
     * substitutions) required to change one word into the other.
     * <p>
     * http://en.wikipedia.org/wiki/Levenshtein_distance
     * <p>
     * It is always at least the difference of the sizes of the two strings.
     * It is at most the length of the longer string.
     * It is zero if and only if the strings are equal.
     * If the strings are the same size, the Hamming distance is an upper bound
     * on the Levenshtein distance.
     * The Levenshtein distance verifies the triangle inequality (the distance
     * between two strings is no greater than the sum Levenshtein distances from
     * a third string).
     * <p>
     * Implementation uses dynamic programming (Wagner–Fischer algorithm), with
     * only 2 rows of data. The space requirement is thus O(m) and the algorithm
     * runs in O(mn).
     *
     * @param s1        The first string to compare.
     * @param s2        The second string to compare.
     * @param threshold The maximum result to compute before stopping. This
     *                  means that the calculation can terminate early if you
     *                  only care about strings with a certain similarity.
     *                  Set this to Integer.MAX_VALUE if you want to run the
     *                  calculation to completion in every case.
     * @return The computed Levenshtein distance.
     * @throws NullPointerException if s1 or s2 is null.
     */
    public static double distance(char[] s1, char[] s2, int threshold) {
        if (s1 == null || s2 == null) {
            throw new NullPointerException("Strings cannot be null");
        }

        if (Arrays.equals(s1, s2)) {
            return 0;
        }

        int n = s1.length;
        int m = s2.length;

        // create two work vectors of integer distances
        int[] prevCosts = new int[m + 1];
        int[] currCosts = new int[m + 1];
        int[] temp;

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

            int minCost = currCosts[0];

            // use formula to fill in the rest of the row
            for (int j = 0; j < m; j++) {
                currCosts[j + 1] = findCost(s1, s2, prevCosts, currCosts, i, j);
                minCost = Math.min(minCost, currCosts[j + 1]);
            }

            if (minCost >= threshold) {
                return -1;
            }

            // copy v1 (current row) to v0 (previous row) for next iteration
            //System.arraycopy(v1, 0, v0, 0, v0.length);

            // Swap previous and current row
            temp = prevCosts;
            prevCosts = currCosts;
            currCosts = temp;
        }
        return prevCosts[m];
    }

    public static int findCost(char[] s1, char[] s2, int[] prevCosts, int[] currCosts, int i, int j) {
        int cost = 1;
        if (s1[i] == s2[j]) {
            cost = 0;                          // Cost is 0 when same chars
        }
        //Special cases

        return Math.min(
                currCosts[j] + 1,              // Cost of insertion
                Math.min(
                        prevCosts[j + 1] + 1,  // Cost of remove
                        prevCosts[j] + cost)); // Cost of substitution
    }

    public static double normalisedLevenshteinSimilarity(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        int distanceThreshold = Math.round((1 - Analyser.SimilarityThreshold) * maxLength);
        //double distance = levenshteinDistance(s1.toCharArray(), s2.toCharArray(), distanceThreshold);
        double distance = distance(s1.toCharArray(), s2.toCharArray(), distanceThreshold);
        if (distance == -1) {
            return 0;
        }
        return 1.0 - distance / maxLength;
    }

    /**
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
    public static double levenshteinDistance(char[] s1, char[] s2, int threshold) {
        if (s1 == null || s2 == null) {
            throw new NullPointerException("Strings cannot be null");
        }

        int n = s1.length;
        int m = s2.length;

        //TODO: if a string is empty what to do?
        //      or check it before in Analyser?
        if (n == 0) {
            return m <= threshold ? m : -1;
        } else if (m == 0) {
            return n <= threshold ? n : -1;
        }

        if (n > m) {
            // swap the two strings to consume less memory
            final char[] tmp = s1;
            s1 = s2;
            s2 = tmp;
            n = m;
            m = s2.length;
        }

        int[] prevCosts = new int[n + 1]; // 'previous' cost array, horizontally
        int[] currCosts = new int[n + 1]; // cost array, horizontally
        int[] temp; // placeholder to assist in swapping p and d

        // fill in starting table values
        final int boundary = Math.min(n, threshold) + 1;
        for (int i = 0; i < boundary; i++) {
            prevCosts[i] = i;
        }

        // these fills ensure that the value above the rightmost entry of our
        // stripe will be ignored in following loop iterations
        Arrays.fill(prevCosts, boundary, prevCosts.length, Integer.MAX_VALUE);
        Arrays.fill(currCosts, Integer.MAX_VALUE);

        // iterates through t
        for (int j = 1; j <= m; j++) {
            final char rightJ = s2[j - 1]; // jth character of right
            currCosts[0] = j;

            // compute stripe indices, constrain to array size
            final int min = Math.max(1, j - threshold);
            final int max = j > Integer.MAX_VALUE - threshold ? n : Math.min(
                    n, j + threshold);

            // the stripe may lead off of the table if s and t are of different
            // sizes
            if (min > max) {
                return -1;
            }

            // ignore entry left of leftmost
            if (min > 1) {
                currCosts[min - 1] = Integer.MAX_VALUE;
            }

            // iterates through [min, max] in s
            for (int i = min; i <= max; i++) {
                if (s1[i - 1] == rightJ) {
                    // diagonally left and up
                    currCosts[i] = prevCosts[i - 1];
                } else {
                    // 1 + minimum of cell to the left, to the top, diagonally
                    // left and up
                    currCosts[i] = 1 + Math.min(Math.min(currCosts[i - 1], prevCosts[i]), prevCosts[i - 1]);
                }
            }

            // Swap previous and current row
            temp = prevCosts;
            prevCosts = currCosts;
            currCosts = temp;
        }

        // if p[n] is greater than the threshold, there's no guarantee on it
        // being the correct distance
        if (prevCosts[n] <= threshold) {
            return prevCosts[n];
        }
        return -1;
    }
}