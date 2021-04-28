package ee.ut.similaritydetector.backend;

public class LevenshteinDistance {

    /**
     * <p>Adapted from: https://github.com/tdebatty/java-string-similarity/blob/master/src/main/java/info/debatty/java/stringsimilarity/Levenshtein.java [06.03.2021]</p>
     * <p>Calculates the customised Levenshtein distance metric of two strings.</p>
     * <p>There is an optimisation made, that if the distance metric surpasses the given threshold, the algorithm is halted.</p>
     * <p>Customisations made:
     * <ol>
     *     <li>Same characters with case difference have an edit cost of 0.2 (instead of 1).</li>
     *     <li>String literal markers change cost 0.2:</li>
     *     <ul><li>" <-> ' </li></ul>
     *     <li>Estonian accent characters changed to ascii characters (and vice versa) has an edit cost 0.2:
     *     <ul>
     *         <li>õ <-> o, 6</li>
     *         <li>ä <-> a, 2</li>
     *         <li>ö <-> o</li>
     *         <li>ü <-> u, y</li>
     *         <li>š <-> s</li>
     *         <li>ž <-> z</li>
     *     </ul>
     *     </li>
     * </ol>
     * </p>
     * @param s1        String 1
     * @param s2        String 2
     * @param threshold the max difference threshold
     * @return the Levenshtein distance between s1 and s2
     */
    public static double distance(String s1, String s2, int threshold) {
        if (s1 == null || s2 == null || s1.length() == 0 || s2.length() == 0) {
            throw new NullPointerException("Strings cannot be null");
        }

        if (s1.equals(s2)) {
            return 0;
        }

        // Find the common prefix and suffix of the two strings
        // and remove them from the strings in order to make the algorithm faster
        StringBuilder sb1 = new StringBuilder(s1);
        StringBuilder sb2 = new StringBuilder(s2);
        int prefixMismatchIndex;
        if (sb1.length() <= sb2.length()) {
            prefixMismatchIndex = findFirstMismatchIndex(sb1, sb2);
        } else {
            prefixMismatchIndex = findFirstMismatchIndex(sb2, sb1);
        }

        // If strings are equal, return distance 0
        if (prefixMismatchIndex == -1) {
            return 0;
        }
        sb1 = new StringBuilder(sb1.substring(prefixMismatchIndex));
        sb2 = new StringBuilder(sb2.substring(prefixMismatchIndex));

        // For finding suffix mismatch the strings are reversed
        sb1.reverse();
        sb2.reverse();

        int suffixMismatchIndex;
        if (sb1.length() <= sb2.length()) {
            suffixMismatchIndex = findFirstMismatchIndex(sb1, sb2);
        } else {
            suffixMismatchIndex = findFirstMismatchIndex(sb2, sb1);
        }

        sb1 = new StringBuilder(sb1.substring(suffixMismatchIndex));
        sb2 = new StringBuilder(sb2.substring(suffixMismatchIndex));
        s1 = sb1.reverse().toString();
        s2 = sb2.reverse().toString();

        int n = s1.length();
        int m = s2.length();

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
                currCosts[j + 1] = findCost(s1.charAt(i), s2.charAt(j), prevCosts, currCosts, j);
                minCost = Math.min(minCost, currCosts[j + 1]);
            }

            // If the distance threshold is surpassed,
            // then considers the two strings not similar enough
            if (minCost > threshold) {
                return -1;
            }

            // Swap previous and current row
            temp = prevCosts;
            prevCosts = currCosts;
            currCosts = temp;
        }
        return prevCosts[m];
    }

    private static int findFirstMismatchIndex(StringBuilder s1, StringBuilder s2) {
        int mismatchIndex = 0;
        for (int i = 0, n = s1.length(); i < n; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return mismatchIndex;
            }
            mismatchIndex ++;
        }
        if (s1.length() > mismatchIndex || s2.length() > mismatchIndex) {
            return mismatchIndex;
        }
        // Strings are equal
        else {
            return -1;
        }
    }


    /**
     * Finds the minimal edit cost for the current characters
     *
     * @param c1 character from String 1 that is compared
     * @param c2 character from String 2 that is compared
     * @param prevCosts previous row of costs
     * @param currCosts current row of costs
     * @param j the index of character c2 in String 2
     * @return the cost for the edit
     */
    public static double findCost(char c1, char c2, double[] prevCosts, double[] currCosts, int j) {
        // Initial substitution cost is 1
        double subCost = 1;
        // Same characters
        if (c1 == c2) {
            subCost = 0;
        }
        // Char case difference
        else if (Character.toLowerCase(c1) == c2 && Character.toLowerCase(c1) != c1 ||
                Character.toLowerCase(c2) == c1 && Character.toLowerCase(c2) != c2) {
            subCost = 0.2;
        }
        // Char substitution cost special cases
        else if (charSubstitutionCondition(c1, c2, '\"', '\'') ||
                charSubstitutionCondition(c1, c2, 'o', 'õ') ||
                charSubstitutionCondition(c1, c2, 'o', 'ö') ||
                charSubstitutionCondition(c1, c2, 'a', 'ä') ||
                charSubstitutionCondition(c1, c2, 'u', 'ü') ||
                charSubstitutionCondition(c1, c2, 'y', 'ü') ||
                charSubstitutionCondition(c1, c2, '2', 'ä') ||
                charSubstitutionCondition(c1, c2, '6', 'õ') ||
                charSubstitutionCondition(c1, c2, 's', 'š') ||
                charSubstitutionCondition(c1, c2, 'z', 'ž')) {
            subCost = 0.2;
        }
        return Math.min(
                currCosts[j] + 1,                 // Cost of insertion
                Math.min(
                        prevCosts[j + 1] + 1,     // Cost of removal
                        prevCosts[j] + subCost)); // Cost of substitution
    }

    private static boolean charSubstitutionCondition(char curr1, char curr2, char sub1, char sub2) {
        return curr1 == sub1 && curr2 == sub2 || curr1 == sub2 && curr2 == sub1;
    }

    /**
     * <p>Finds the normalised Levenshtein similarity between the given solutions s1 and s2 by the formula:</p>
     * <p>normalisedLevenshteinSimilarity(s1, s2) = 1 - LevenshteinDistance(s1, s2) / max(s1.length, s2.length)</p>
     *
     * @param s1 Solution 1 code
     * @param s2 Solution 2 code
     * @param similarityThreshold given similarity threshold above which solutions are considered similar
     * @return normalised Levenshtein similarity between the two solutions
     */
    public static double normalisedLevenshteinSimilarity(String s1, String s2, float similarityThreshold) {
        int maxLength = Math.max(s1.length(), s2.length());
        int distanceThreshold = Math.round((1 - similarityThreshold) * maxLength);
        double levenshteinDistance;
        try {
            levenshteinDistance = distance(s1, s2, distanceThreshold);
        } catch (NullPointerException e) {
            // If a string was null or empty then return similarity 0
            return 0;
        }
        if (levenshteinDistance == -1) {
            // If the difference threshold was surpassed, then return similarity 0
            return 0;
        }
        return 1.0 - levenshteinDistance / maxLength;
    }

}