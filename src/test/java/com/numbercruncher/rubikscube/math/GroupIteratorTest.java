package com.numbercruncher.rubikscube.math;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class GroupIteratorTest {

    @Test
    void next() {
        PermutationGroup s5 = new PermutationGroup("Symmetric group S5",
                new String[]{"a","b"},
                Permutation.parse("(0 1 2 3 4)"),
                Permutation.parse("(3 4)"));

        int count = 0;
        for (GroupIterator it = s5.getIterator(); it.hasNext(); ) {
            GroupElement o = it.next();
            count++;
            System.out.println(count+": "+o.toFullString());
        }

        assertEquals(120,s5.getIterator().toStream().count());
    }
}