package com.numbercruncher.rubikscube.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermutationGroupS5 {
    private PermutationGroup s5;

    @BeforeEach
    void setUp() {
        Permutation a = Permutation.parse("(0 1 2 3 4)");
        Permutation b = Permutation.parse("(3 4)");

        String[] labels = {"a","b"};
        s5 = new PermutationGroup("Symmetric group S5", labels, a,b);

        s5.addWordRule(s->{
            String previous;
            do{
                previous=s;
                s=s.replace("aaaa","A").replace("AAAA","a")
                        .replace("bb","").replace("BB","");
            }while(!s.equals(previous));
            return s;
        });
    }


    @Test
    void getDegree() {
        assertEquals(5,s5.getDegree());
    }

    @Test
    void getStabilizerChain() {
        long start = System.currentTimeMillis();
        StabilizerChain chain = s5.getStabilizerChain();
        long end = System.currentTimeMillis();
        System.out.println(chain);
        System.out.println("Calculated in: " + (end - start) + " ms");
    }

    @Test
    void contains() {
        for (int i = 0; i < 200; i++) {
           GroupElement element = s5.randomElement(20);
           assertTrue(s5.contains(element));
        }
    }


    @Test
    void testToString() {
        System.out.println(s5);
    }

    @Test
    void getSize() {
        assertEquals(120,s5.getSize());
    }
}