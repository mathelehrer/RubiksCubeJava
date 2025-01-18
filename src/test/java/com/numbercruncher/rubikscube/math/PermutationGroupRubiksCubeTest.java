package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.utils.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermutationGroupRubiksCubeTest {
    private PermutationGroup rubiksGroup;

    @BeforeEach
    void setUp() {
        Permutation t = Permutation.parse("(1 3 7 5)(2 6 8 4)(9 33 25 17)(10 34 26 18)(11 35 27 19)(48)");
        Permutation l = Permutation.parse("(1 17 41 39)(4 20 44 38)(5 21 45 35)(9 11 15 13)(10 14 16 12)(48)");
        Permutation f = Permutation.parse("(5 25 43 15)(7 29 41 11)(8 28 42 14)(17 19 23 21)(18 22 24 20)(48)");
        Permutation r = Permutation.parse("(3 37 43 19)(6 36 46 22)(7 33 47 23)(25 27 31 29)(26 30 32 28)(48)");
        Permutation b = Permutation.parse("(1 13 47 27)(2 12 48 30)(3 9 45 31)(33 35 39 37)(34 38 40 36)");
        Permutation d = Permutation.parse("(13 21 29 37)(15 23 31 39)(16 24 32 40)(41 43 47 45)(42 46 48 44)");

        //the order of the generators matters for the stabilizer chain
        //this way, it is almost possible to first stabilize all corners and then all edges
        String[] labels = {"T", "D", "L","R",  "F", "B"};
        //A
        rubiksGroup = new PermutationGroup("Rubik's Cube Group", labels, t,d, l, r, f,  b );

        for (String label : labels) {
            rubiksGroup.addWordRule(s->{
                String previous;
                do{
                    previous=s;
                    String tL = StringUtils.toggleCase(label);
                    s=s.replaceAll(label+label+label,tL)
                            .replaceAll(tL+tL+tL,label);
                }while(!s.equals(previous));
                return s;
            });
        }

        //prefer odd orbit elements (corners) to be stabilized first, when building the stabilizer chain
        rubiksGroup.addBasisSelectionRule(value->{return value%2!=0;});

    }

    @Test
    void getBase(){
        byte[] base = rubiksGroup.getBaseAsByteArray();
        assertEquals("[1, 13, 5, 7, 23, 15, 12, 8, 14, 24, 16, 3, 6, 4, 2, 22, 30, 32]", Arrays.toString(base));
    }

    @Test
    void createMinkwitzChain(){
        long start = System.currentTimeMillis();
        int numberOfElements = 500000;
        MinkwitzChain chain = rubiksGroup.getMinkwitzChain(numberOfElements);
        long end = System.currentTimeMillis();
        rubiksGroup.visualizeMinkwitzChain(chain);
        System.out.println("Calculated in: " + (end - start) + " ms");
        System.out.println("Missing: "+chain.getNumberOfMissingElements());
        System.out.println("Average Word Length: "+chain.getAverageWordLength());
    }

    @Test
    void createExtendedMinkwitzChain(){
        long start = System.currentTimeMillis();
        int preTraining = 0;
        int numberOfElements = 8000001;
        int maxBranching = 1;
        int simplificationRules =2840;
        ExtendedMinkwitzChain chain = rubiksGroup.getExtendedMinkwitzChain(preTraining,numberOfElements,simplificationRules,maxBranching,true);

        long end = System.currentTimeMillis();

        rubiksGroup.visualizeExtendedMinkwitzChain(chain);
        System.out.println("Calculated in: " + (end - start) + " ms");
        System.out.println("Missing: "+chain.getNumberOfMissingElements());
        System.out.println("Average Word Length: "+chain.getAverageWordLength());

    }

    @Test
    void getDegree() {
        assertEquals(49,rubiksGroup.getDegree());
    }

    @Test
    void getStabilizerChain() {
        long start = System.currentTimeMillis();
        StabilizerChain chain = rubiksGroup.getStabilizerChain();
        long end = System.currentTimeMillis();
        System.out.println(chain);
        System.out.println("Calculated in: " + (end - start) + " ms");
    }

    @Test
    void contains() {
        for (int i = 0; i < 200; i++) {
           GroupElement element = rubiksGroup.randomElement(20);
           assertTrue(rubiksGroup.contains(element));
        }
    }


    @Test
    void testToString() {
        System.out.println(rubiksGroup);
    }

    @Test
    void getSize() {
        assertTrue(rubiksGroup.getSize().divide(new BigInteger("43252003274489856000")).equals(BigInteger.ONE));
    }

    @Test
    void visualizeStabChain() {
        rubiksGroup.visualizeStabilizerChain();
    }

    @Test
    void superflip(){
        //try to get the word of the super flip;
        int preTraining = 0;
        int numberOfElements = 8000000;
        int maxBranching = 1;
        int simplificationRules = 2840;
        PermutationGroup rubiksCube = PermutationGroup.RubiksGroup();
        Permutation superFlip = Permutation.parse("(2 34)(4 10)(6 26)(8 18)(12 38)(14 20)(16 44)(22 28)(24 42)(30 36)(32 46)(40 48)");
        System.out.println(superFlip);
        System.out.println("Element test: "+rubiksCube.contains(superFlip));
        String word = rubiksCube.elementToWord(superFlip,numberOfElements,simplificationRules,true);
        System.out.println(word+"\nMy word representation of the super flip with "+word.length()+" words: ");
        GroupElement reconstruction = rubiksCube.wordToElement(word);
        System.out.println(reconstruction.toFullString());
        System.out.println(reconstruction.getPermutation());
    }


    @Test
    void superflipExtended(){
        //try to get the word of the super flip;

        Permutation superFlip = Permutation.parse("(2 34)(4 10)(6 26)(8 18)(12 38)(14 20)(16 44)(22 28)(24 42)(30 36)(32 46)(40 48)");
        System.out.println(superFlip);
        System.out.println("Element test: "+rubiksGroup.contains(superFlip));
        int preTraining = 8000000;
        int numberOfElements = 8000000;
        int maxBranching = 1;
        int simplificationRules = 10982;
        List<GroupElement> elements = rubiksGroup.elementToWordExtended(superFlip,preTraining, numberOfElements,simplificationRules,maxBranching,20,true);
        System.out.println("My word representation of the super flip with "+elements.size()+" versions! ");
        for (GroupElement element : elements) {
            element.apply(rubiksGroup.getSimplifyingRules(simplificationRules));
        }
        List<String> subList = elements.stream().filter(v->v.toFullWordString().length()<60).map(GroupElement::toFullWordString).toList();
        for (String s : subList) {
            System.out.println(s);
        }
        System.out.println("Amount of simplification rules: "+rubiksGroup.getSimplifyingRules(simplificationRules).size());
        rubiksGroup.saveSimplificationRules();;
    }

    @Test
    void getFirst10000Elements  () {
        PermutationGroup rubiksCube = PermutationGroup.RubiksGroup();
        int count =0;
        for (GroupIterator it = rubiksCube.getIterator(8000000); it.hasNext(); ) {
            GroupElement element = it.next();
            count++;
            if (count%10000==0)
                System.out.println(count+": "+element.toFullWordString());
        }
    }

    @Test
    void applySimplifications(){
        int numberOfSimplificationRules = 6659;
        int preTraining = 0;
        int numberOfElements = 1000000;
        ExtendedMinkwitzChain chain = rubiksGroup.getExtendedMinkwitzChain(preTraining, numberOfElements, numberOfSimplificationRules,1);
        rubiksGroup.visualizeExtendedMinkwitzChain(chain);
        System.out.println("Average Word Length: "+chain.getAverageWordLength());
        chain.applyRules(rubiksGroup.getSimplifyingRules(numberOfSimplificationRules));
        System.out.println("Average Word Length: "+chain.getAverageWordLength());
        Permutation superFlip = Permutation.parse("(2 34)(4 10)(6 26)(8 18)(12 38)(14 20)(16 44)(22 28)(24 42)(30 36)(32 46)(40 48)");
        System.out.println(superFlip);
        System.out.println("Element test: "+rubiksGroup.contains(superFlip));
        List<GroupElement> elements = rubiksGroup.elementToWordExtended(superFlip, preTraining, numberOfElements, numberOfSimplificationRules,1, 10,true);
        System.out.println("My word representation of the super flip with "+elements.size()+" versions! ");
        System.out.println("Amount of simplification rules: "+rubiksGroup.getSimplifyingRules(numberOfSimplificationRules).size());
        for (GroupElement element : elements) {
            element.apply(rubiksGroup.getSimplifyingRules(numberOfSimplificationRules));
        }

        List<GroupElement> elements2 =elements.stream().filter(e->e.toFullWordString().length()<100).toList();

        for (GroupElement element : elements2) {
            System.out.println(element.toFullWordString().length()+": "+element.toFullString());
        }

        rubiksGroup.saveSimplificationRules();;
    }


    @Test
    void applySimplifications2(){
        ExtendedMinkwitzChain chain = rubiksGroup.getExtendedMinkwitzChain(7000001,7000001,3394,1);
        rubiksGroup.visualizeExtendedMinkwitzChain(chain);
        System.out.println("Average Word Length: "+chain.getAverageWordLength());
        chain.applyRules(rubiksGroup.getSimplifyingRules(3394));
        System.out.println("Average Word Length: "+chain.getAverageWordLength());
        Permutation superFlip = Permutation.parse("(2 34)(4 10)(6 26)(8 18)(12 38)(14 20)(16 44)(22 28)(24 42)(30 36)(32 46)(40 48)");
        System.out.println(superFlip);
        System.out.println("Element test: "+rubiksGroup.contains(superFlip));
        List<GroupElement> elements = rubiksGroup.elementToWordExtended(superFlip,7000001, 7000001,3394,1, true);
        System.out.println("My word representation of the super flip with "+elements.size()+" versions! ");
        System.out.println("Amount of simplification rules: "+rubiksGroup.getSimplifyingRules(3394).size());
        for (GroupElement element : elements) {
            System.out.println(element.toFullWordString().length()+": "+element.toFullString());
        }
        rubiksGroup.saveSimplificationRules();;
    }

    @Test
    void saveSimplificationRules(){
        System.out.println(rubiksGroup.getSimplifyingRules(4670).size());
        rubiksGroup.saveSimplificationRules();
    }

    @Test
    void simplifyWord(){
        rubiksGroup.simplifyWord("flBLrtRFBTTFbrrfRFbdfBdrbFDBfDbDtlldLFbtRfdrlbFdLrbT",17000000);
        rubiksGroup.simplifyWord("RBrBfTbbFFbdBfDFLRdldrffRFDfdFbLBdrFFRlttLDtbfRltBfl",17000000);
        rubiksGroup.simplifyWord("RBftFbFLrblRDFTBflfbfLrdBfrlTdfDtRRTdfRDtBRlDfBL",17000000);
        rubiksGroup.simplifyWord("DRFLBdblfbrBfTFrTlRbbLrTDrLBBRlDbtDLLTdbFRltLrBLrDFbLtDF",17000000);
        rubiksGroup.simplifyWord("rFbDBdfBRbrTRlbbLrTRTlRbbLrTfRRFbdtLLTdbFRltLrBLrDFbLtDF",17000000);
        rubiksGroup.simplifyWord("rBRDLTbtldRfTbFrBrBtDBTdrbDDFbLLBfLtLTdbDlFRltLrBLrDFbLtDF",17000000);
        rubiksGroup.simplifyWord("DLTBtldrbRRftFbRBrBtDBTdrbDDFbLLBfLtLTdbDlFRltLrBLrDFbLtDF",17000000);
        rubiksGroup.simplifyWord("rFbDBdfBRbTRtrtrtRTRftBfLbTBflfbfLrdBfrlTdFFRlTTLDtBRlDfBL",17000000);
        rubiksGroup.simplifyWord("BTrtDBdbRbRBftFrDtLblTdrbFDFLflBfDfDFblBFFRlTTLrdTbRltdbFl",17000000);
        rubiksGroup.simplifyWord("FFdFtRFrTfDFBLBFbbflfbffLLFLrbLRfLdRlTrdBFtbTfrDbRLDbFRlDt",17000000);
    }

    @Test
    void forVideo(){
        ExtendedMinkwitzChain chain = rubiksGroup.getExtendedMinkwitzChain(7000001,7000001,3394,1);
        rubiksGroup.visualizeExtendedMinkwitzChain(chain);
        System.out.println("Average Word Length: "+chain.getAverageWordLength());
        Permutation currentState = Permutation.parse("(1 27 37 21 5 7 9 3 47 41 17 25 35 33 31 15 11 19)(2 12 44)(4 46 26 14 36)(6 20 30 10 32)(8 24 22 40 18 42 28 48)(13 29 39 23 45 43)(16 34 38)");
        System.out.println(currentState);
        System.out.println("Element test: "+rubiksGroup.contains(currentState));
        List<GroupElement> elements = rubiksGroup.elementToWordExtended(currentState,7000001, 7000001,3394,1, true);
        System.out.println("My word representation of the super flip with "+elements.size()+" versions! ");
        for (GroupElement element : elements) {
            System.out.println(element.toFullWordString().length()+": "+element.toFullString());
        }
    }

    @Test
    void forVideo2(){
        rubiksGroup.visualizeStabilizerChain();
        System.out.println("");

        Permutation currentState = Permutation.parse("(1 27 37 21 5 7 9 3 47 41 17 25 35 33 31 15 11 19)(2 12 44)(4 46 26 14 36)(6 20 30 10 32)(8 24 22 40 18 42 28 48)(13 29 39 23 45 43)(16 34 38)");
        System.out.println("Element test: "+rubiksGroup.contains(currentState,true));
        currentState = Permutation.parse("(1 27 37 21 5 25 35 33 31 15 11 7 9 3 47 41 17 19)(2 12 44)(4 46 26 14 36)(6 20 30 10 32)(8 24 22 40 18 42 28 48)(13 29 39 23 45 43)(16 34 38)");
        System.out.println("Element test: "+rubiksGroup.contains(currentState,true));
        currentState= Permutation.parse("(1 27 37 21 5 19)(2 12 44)(3 47 41 17 7 9)(4 46 26 14 36)(6 20 30 10 32)(8 24 22 40 18 42 28 48)(11 25 35 33 31 15)(13 29 39 23 45 43)(16 34 38)");
        System.out.println("Element test: "+rubiksGroup.contains(currentState,true));
        currentState=Permutation.parse("(1 27 37 21 5 7 9 3 47 41 17 25 35 33 31 15 11 19)(2 12 44)(4 32 6 20 30 10 46 26 14 36)(8 24 22 40 18 42 28 48)(13 29 39 23 45 43)(16 34 38)");
        System.out.println("Element test: "+rubiksGroup.contains(currentState,true));
        currentState=Permutation.parse("(1 27 37 21 5 25 35 33 31 15 11 7 9 3 47 41 17 19)(2 12 44)(4 32 6 20 30 10 46 26 14 36)(8 24 22 40 18 42 28 48)(13 29 39 23 45 43)(16 34 38)");
        System.out.println("Element test: "+rubiksGroup.contains(currentState,true));
        currentState=Permutation.parse("(1 27 37 21 5 19)(2 12 44)(3 47 41 17 7 9)(4 32 6 20 30 10 46 26 14 36)(8 24 22 40 18 42 28 48)(11 25 35 33 31 15)(13 29 39 23 45 43)(16 34 38)");
        System.out.println("Element test: "+rubiksGroup.contains(currentState,true));
        currentState=Permutation.parse("(1 27 37 21 5 7 9 3 47 41 17 25 35 33 31 15 11 19)(2 12 44 18 42 28 48 34 38 16 8 24 22 40)(4 46 26 14 36)(6 20 30 10 32)(13 29 39 23 45 43)");
        System.out.println("Element test: "+rubiksGroup.contains(currentState,true));
        currentState=Permutation.parse("(1 27 37 21 5 25 35 33 31 15 11 7 9 3 47 41 17 19)(2 12 44 18 42 28 48 34 38 16 8 24 22 40)(4 46 26 14 36)(6 20 30 10 32)(13 29 39 23 45 43)");
        System.out.println("Element test: "+rubiksGroup.contains(currentState,true));
        currentState=Permutation.parse("(1 27 37 21 5 19)(2 12 44 18 42 28 48 34 38 16 8 24 22 40)(3 47 41 17 7 9)(4 46 26 14 36)(6 20 30 10 32)(11 25 35 33 31 15)(13 29 39 23 45 43)");
        System.out.println("Element test: "+rubiksGroup.contains(currentState,true));
        currentState=Permutation.parse("(1 27 37 21 5 19)(2 12 44 18 42 28 48 34 38 16 8 24 22 40)(3 47 41 17 7 9)(4 32 6 20 30 10 46 26 14 36)(11 25 35 33 31 15)(13 29 39 23 45 43)");
        System.out.println("Element test: "+rubiksGroup.contains(currentState,true));
        currentState=Permutation.parse("(1 27 37 21 5 25 35 33 31 15 11 7 9 3 47 41 17 19)(2 12 44 18 42 28 48 34 38 16 8 24 22 40)(4 32 6 20 30 10 46 26 14 36)(13 29 39 23 45 43)");
        System.out.println("Element test: "+rubiksGroup.contains(currentState,true));

        //        String word = rubiksGroup.elementToWord(currentState,500000,2840);
//        System.out.println(word);
//        GroupElement reconstruction = rubiksGroup.wordToElement(word);
//        System.out.println(currentState);
//        System.out.println(reconstruction.getPermutation());
    }

    @Test
    void forVideo3(){
        String[] perms = {
        "(1 27 37 21 5 7 9 3 47 41 17 25 35 33 31 15 11 19)(2 12 44 18 40 20)(4 24 22 26 46 30 10 42 28 6 32 36)(8 48 14 34 38 16)(13 29 39 23 45 43)",
                "(1 27 37 21 5 25 35 33 31 15 11 7 9 3 47 41 17 19)(2 12 44 18 40 20)(4 24 22 26 46 30 10 42 28 6 32 36)(8 48 14 34 38 16)(13 29 39 23 45 43)",
                "(1 27 37 21 5 19)(2 12 44 18 40 20)(3 47 41 17 7 9)(4 24 22 26 46 30 10 42 28 6 32 36)(8 48 14 34 38 16)(11 25 35 33 31 15)(13 29 39 23 45 43)",
                "(1 27 37 21 5 19)(2 12 44 18 40 20)(3 47 41 17 7 9)(4 42 28 6 32 36)(8 48 14 34 38 16)(10 24 22 26 46 30)(11 25 35 33 31 15)(13 29 39 23 45 43)",
               "(1 27 37 21 5 7 9 3 47 41 17 25 35 33 31 15 11 19)(2 12 44 18 40 20)(4 42 28 6 32 36)(8 48 14 34 38 16)(10 24 22 26 46 30)(13 29 39 23 45 43)"};

        for (String perm : perms) {
            Permutation currentState = Permutation.parse(perm);
            boolean result = rubiksGroup.contains(currentState, false);
            System.out.println("Element test: " + result);
            if (result){
                rubiksGroup.elementToWordExtended(currentState,8000000,8000000,2840,1,0,true);
            }
        }
    }

    @Test
    void randomElements(){
        for (int i = 0; i < 100; i++) {
            GroupElement element = rubiksGroup.randomElement(20);
            System.out.println(element.getPermutation());
            System.out.println("-------------------------------------------------------");
        }
    }

    @Test
    void simplifyExtendedMinkwitzChain(){
        rubiksGroup.simplifyExtendedMinkwitzChain(0,17000000,17000000);
    }

}