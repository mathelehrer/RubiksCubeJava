package com.numbercruncher.rubikscube.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinkwitzChainTest {

    @Test
    void isConsistent() {
        PermutationGroup s5 = new PermutationGroup(
                "Symmetric group S5",
                new String[]{"a", "b"},
                Permutation.parse("(0 1 2 3 4)"),
                Permutation.parse("(3 4)")
        );
    }

    @Test
    void save(){
        PermutationGroup s5 = new PermutationGroup(
                "Symmetric group S5",
                new String[]{"a", "b"},
                Permutation.parse("(0 1 2 3 4)"),
                Permutation.parse("(3 4)")
        );

        MinkwitzChain chain = s5.createMinkwitzChain(30);
        System.out.println(chain);
        chain.save();
    }

    @Test
    void load() {
        MinkwitzChain chain = MinkwitzChain.load("Symmetric group S5");
        System.out.println(chain);
    }
}