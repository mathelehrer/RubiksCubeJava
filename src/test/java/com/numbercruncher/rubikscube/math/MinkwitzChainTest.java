package com.numbercruncher.rubikscube.math;

import org.junit.jupiter.api.Test;

import java.util.TreeMap;

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

        MinkwitzChain chain = s5.getMinkwitzChain(30);
        System.out.println(chain);
        chain.save();
    }

    @Test
    void load() {
        MinkwitzChain chain = MinkwitzChain.load("Rubik's Cube Group",400000);
        System.out.println("Average word length before simplification: "+chain.getAverageWordLength());
        PermutationGroup rubikscube = PermutationGroup.RubiksGroup();
        TreeMap<String,String> rules =  rubikscube.getSimplifyingRules(850,true);
        chain.applyRules(rules);
        System.out.println("Average word length after simplification: "+chain.getAverageWordLength());
        chain = rubikscube.trainMinkwitzChain(chain,400001,500000);
        System.out.println("Average word length before simplification: "+chain.getAverageWordLength());
        chain.applyRules(rules);
        System.out.println("Average word length after simplification: "+chain.getAverageWordLength());

    }
}