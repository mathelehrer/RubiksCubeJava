package com.numbercruncher.rubikscube.random_trails;

import com.numbercruncher.rubikscube.math.Base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The class HashIntArrays
 *
 * @author NumberCruncher
 * Since 1/1/25
 * @version 1/1/25
 */

public class HashIntArrays {
    public static void main(String[] args) {
        byte[] a = {0, 4, 5, 0};   // hash 927520
        byte[] b = {0, 3, 36, 0};   // hash 927520
        byte[] c = {0,4,5,0};

        Base a1 = new Base(a);
        Base a2 = new Base(b);
        Base a3 = new Base(c);

        System.out.println(Arrays.toString(a) + ": " + Arrays.hashCode(a));
        System.out.println(Arrays.toString(b) + ": " + Arrays.hashCode(b));
        System.out.println(Arrays.toString(c) + ": " + Arrays.hashCode(c));

        System.out.println("a1=a2?: "+a1.equals(a2));
        System.out.println("a1=a3?: "+a1.equals(a3));
        System.out.println("a3=a2?: "+a3.equals(a2));


        //Nevertheless the HashSet can deal with these hash collisions
        //So I don't have to worry

        Set<Base> arrays = new HashSet<>();
        Set<Integer> hashes = new HashSet<>();
        for (int i = 0; i < 100000; i++) {
            byte b1 = (byte) (Math.random() * 5);
            byte b2 = (byte) (Math.random() * 5);
            byte b3 = (byte) (Math.random() * 5);
            byte b4 = (byte) (Math.random() * 5);
            byte[] e = {b1, b2, b3, b4};
            arrays.add(new Base(e));
            hashes.add(Arrays.hashCode(e));

        }


        System.out.println(arrays.size());
        System.out.println(hashes.size());

    }
}


