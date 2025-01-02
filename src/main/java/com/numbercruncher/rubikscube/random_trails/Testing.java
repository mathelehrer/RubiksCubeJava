package com.numbercruncher.rubikscube.random_trails;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * The class Testing
 *
 * @author NumberCruncher
 * Since 1/1/25
 * @version 1/1/25
 */

public class Testing {

    public Testing() {


        int maxGenerateCounter =20;
        System.out.println("Using Splitter: ");
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<String>()
        {
            int counter = 0;
            @Override
            public boolean hasNext () {
                // simplistic solution, see below for explanation
                return counter < maxGenerateCounter;
            }
            @Override
            public String next () {
                // executing stuff
                // providing info for 'stopping' the stream
                counter++; // for simplicity
                if (counter > maxGenerateCounter) {
                    return null; // this could be any answer. It will be filtered out.
                }
                return "String-" + counter;
            }
        }, Spliterator.IMMUTABLE),false).forEach(System.out::println);
    }

    public static void main(String[] args) {
        new Testing();
    }
}
