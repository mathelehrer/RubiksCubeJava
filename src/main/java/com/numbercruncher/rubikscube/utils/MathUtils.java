package com.numbercruncher.rubikscube.utils;

import java.util.ArrayList;
import java.util.List;

public class MathUtils {

    /**
     * Generate all variations of a list of bytes to order k.
     *
     * @param set The list of bytes to generate variations from.
     * @param k     The order of variations to generate.
     * @return A list of variations, where each variation is a list of bytes.
     */
    public static List<List<Byte>> variations(List<Byte> set, int k){
        List<List<Byte>> results = new ArrayList<>();
        generateVariationsRecursive(set, k, new ArrayList<>(), results);
        return results;

    }

    /**
     * Recursive helper function to generate variations.
     *
     * @param bytes    The list of bytes to generate variations from.
     * @param k        Remaining length of the variation.
     * @param current  The current variation being generated.
     * @param results  The final list of variations.
     */
    private static void generateVariationsRecursive(List<Byte> bytes, int k, List<Byte> current, List<List<Byte>> results) {
        if (k == 0) {
            // Base case: add the current combination to the results and return
            results.add(new ArrayList<>(current));
            return;
        }

        for (int i = 0; i < bytes.size(); i++) {
            // Skip bytes that are already in the current variation
            if (current.contains(bytes.get(i))) continue;

            // Add the current byte to the current variation
            current.add(bytes.get(i));

            // Recurse to generate the next element of the variation
            generateVariationsRecursive(bytes, k - 1, current, results);

            // Backtrack by removing the last element to explore other possibilities
            current.remove(current.size() - 1);
        }

    }

    // Example usage
    public static void main(String[] args) {
        List<Byte> bytes = List.of((byte) 1, (byte) 2, (byte) 3);
        int k = 2;

        List<List<Byte>> variations = variations(bytes, k);
        System.out.println("Variations of length " + k + ": " + variations);
    }

}
