package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.utils.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermutationGroupCoxB3 {
    private PermutationGroup coxB3;

    @BeforeEach
    void setUp() {
        Permutation a = Permutation.parse("(1 5)(2 6)(3 7)(4 8)");
        Permutation b = Permutation.parse("(1 6)(3 8)");
        Permutation c = Permutation.parse("(1 4)(5 8)");

        //the order of the generators matters for the stabilizer chain
        //this way, it is almost possible to first stabilize all corners and then all edges
        String[] labels = {"a", "b", "c"};

        coxB3 = new PermutationGroup("Coxeter B3", labels, a,b,c );

//        for (String label : labels) {
//            coxB3.addWordRule(s->{
//                String previous;
//                do{
//                    previous=s;
//                    s=s.replaceAll(label+label,"");
//                }while(!s.equals(previous));
//                return s;
//            });
//        }
    }

    @Test
    void getBase(){
        byte[] base = coxB3.getBaseAsByteArray();
        assertEquals("[1, 2, 3]", Arrays.toString(base));
    }

    @Test
    void createMinkwitzChain(){
        long start = System.currentTimeMillis();
        int numberOfElements = 20000;
        MinkwitzChain chain = coxB3.getMinkwitzChain(numberOfElements);
        long end = System.currentTimeMillis();
        coxB3.visualizeMinkwitzChain(chain);
        System.out.println("Calculated in: " + (end - start) + " ms");
        System.out.println("Missing: "+chain.getNumberOfMissingElements());
        System.out.println("Average Word Length: "+chain.getAverageWordLength());
        System.out.println("Group Size: " + coxB3.getSize());
    }

    @Test
    void createExtendedMinkwitzChain(){
        long start = System.currentTimeMillis();
        int preTraining = 0;
        int numberOfElements = 8000000;
        int maxBranching = 1;
        int simplificationRules =1000;
        ExtendedMinkwitzChain chain = coxB3.getExtendedMinkwitzChain(preTraining,numberOfElements,simplificationRules,maxBranching,true);

        long end = System.currentTimeMillis();

        coxB3.visualizeExtendedMinkwitzChain(chain);
        System.out.println("Calculated in: " + (end - start) + " ms");
        System.out.println("Missing: "+chain.getNumberOfMissingElements());
        System.out.println("Average Word Length: "+chain.getAverageWordLength());

    }

    @Test
    void getDegree() {
        assertEquals(9,coxB3.getDegree());
    }

    @Test
    void getStabilizerChain() {
        long start = System.currentTimeMillis();
        StabilizerChain chain = coxB3.getStabilizerChain();
        long end = System.currentTimeMillis();
        System.out.println(chain);
        System.out.println("Calculated in: " + (end - start) + " ms");
    }

    @Test
    void contains() {
        for (int i = 0; i < 200; i++) {
           GroupElement element = coxB3.randomElement(20);
           assertTrue(coxB3.contains(element));
        }
    }


    @Test
    void testToString() {
        System.out.println(coxB3);
    }

    @Test
    void getSize() {
        assertTrue(coxB3.getSize().divide(new BigInteger("48")).equals(BigInteger.ONE));
    }

    @Test
    void visualizeStabChain() {
        coxB3.visualizeStabilizerChain();
    }

    @Test
    void getFirst48Elements  () {
        int count =0;
        for (GroupIterator it = coxB3.getIterator(48); it.hasNext(); ) {
            GroupElement element = it.next();
            count++;
            System.out.println(count+": "+element.toTabString(3));
        }
    }


}