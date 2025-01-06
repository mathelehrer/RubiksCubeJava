package com.numbercruncher.rubikscube.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PermutationGroupS5Test {
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
    void visualizeStabChain() {
        s5.visualizeStabilizerChain();
    }

    @Test
    void createMinkwitzChain(){
        MinkwitzChain chain = s5.getMinkwitzChain(120);
        s5.visualizeMinkwitzChain(chain);
        System.out.println("Average word length: "+chain.getAverageWordLength());

        System.out.println("Improved chain:");
        MinkwitzChain chain2 = s5.getMinkwitzChain(120);
        s5.visualizeMinkwitzChain(chain2);
        System.out.println("Average word length: "+chain2.getAverageWordLength());

        TreeMap<String,String> rules  = s5.getSimplifyingRules(120);
        chain2.applyRules(rules);
        s5.visualizeMinkwitzChain(chain2);
        System.out.println("Average word length: "+chain2.getAverageWordLength());
    }

    @Test
    void createExtendedMinkwitzChain(){
        ExtendedMinkwitzChain chain = s5.getExtendedMinkwitzChain(120,120);
        s5.visualizeExtendedMinkwitzChain(chain);
        System.out.println("Average word length: "+chain.getAverageWordLength());

        System.out.println(chain);

    }



    @Test
    void getBase(){
        Base base = s5.getBase();
        System.out.println(base);
        byte[] data = base.getBase();
        assertEquals("[0, 3, 2, 1]", Arrays.toString(data));

        //check whether all group elements have a different action on the base
        HashSet<Base> actions = new HashSet<>();
        for (GroupIterator it = s5.getIterator(); it.hasNext(); ) {
            GroupElement element = it.next();
            Base action = base.action(element.getPermutation());
            actions.add(action);
        }

        assertEquals(120,actions.size());
    }
    @Test
    void getSize() {
        assertEquals(new BigInteger("120"),s5.getSize());
    }

    @Test
    void generateSimplificationRules(){
        s5.generateSimplificationRules(120,true);
    }

    @Test
    void elementToWord(){
        int count = 0;
        for (GroupIterator it = s5.getIterator(); it.hasNext(); ) {
            GroupElement element = it.next();
            System.out.println((count++)+" "+element.toFullWordString()+" ["+s5.elementToWordExtended(element.getPermutation(),0,120,35,2,false).stream().map(GroupElement::toFullWordString).collect(Collectors.joining(","))+"]");
        }
    }
}