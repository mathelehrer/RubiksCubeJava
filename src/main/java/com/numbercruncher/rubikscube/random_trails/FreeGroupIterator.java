package com.numbercruncher.rubikscube.random_trails;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The class FreeGroupIterator
 *
 * @author NumberCruncher
 * Since 1/1/25
 * @version 1/1/25
 */

public class FreeGroupIterator implements Iterator<String> {
    private static final char[] GENERATORS = {'a', 'A', 'b', 'B'};
    private final Deque<String> queue = new ArrayDeque<>();

    public FreeGroupIterator() {
        // Initialize the queue with single-letter generators
        for (char generator : GENERATORS) {
            queue.offer(String.valueOf(generator));
        }
    }

    @Override
    public boolean hasNext() {
        // If the queue is not empty, we still have elements to process
        return !queue.isEmpty();
    }

    @Override
    public String next() {
        if (!hasNext()) throw new IllegalStateException("No more elements");

        // Dequeue the next element
        String element = queue.poll();

        // Generate next elements by appending each valid generator
        for (char generator : GENERATORS) {
            // Skip invalid combinations (e.g., if appending inverse of the last character)
            if (element.isEmpty() || !isInverse(element.charAt(element.length() - 1), generator)) {
                queue.offer(element + generator);
            }
        }

        // Return the current element
        return element;
    }

    // Utility method to check if two characters are inverses
    private boolean isInverse(char c1, char c2) {
        return (c1 == 'a' && c2 == 'A') ||
                (c1 == 'A' && c2 == 'a') ||
                (c1 == 'b' && c2 == 'B') ||
                (c1 == 'B' && c2 == 'b');
    }

    // Utility method to convert this iterator to a stream
    public Stream<String> toStream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED),
                false // false: sequential stream, true: parallel stream
        );
    }

    // Entry point for testing
    public static void main(String[] args) {
        com.numbercruncher.rubikscube.random_trails.FreeGroupIterator iterator = new com.numbercruncher.rubikscube.random_trails.FreeGroupIterator();

        // Stream example: limit the stream to 20 elements and print them
        iterator.toStream()
                .limit(20) // Limit the results to 20 elements
                .forEach(System.out::println);

        for (int i = 0; i < 20; i++) {
            System.out.println(i+": "+iterator.next());
        }
    }
}

