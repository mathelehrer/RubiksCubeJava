package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.utils.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermutationGroupRubiksCubeNiceStabChain {
    private PermutationGroup rubiksGroup;

    @BeforeEach
    void setUp() {
        Permutation t = Permutation.parse("(1 3 7 5)(2 6 8 4)(9 33 25 17)(10 34 26 18)(11 35 27 19)(48)");
        Permutation l = Permutation.parse("(1 17 41 39)(4 20 44 38)(5 21 45 35)(9 11 15 13)(10 14 16 12)(48)");
        Permutation f = Permutation.parse("(5 25 43 15)(7 29 41 11)(8 28 42 14)(17 19 23 21)(18 22 24 20)(48)");
        Permutation r = Permutation.parse("(3 37 43 19)(6 36 46 22)(7 33 47 23)(25 27 31 29)(26 30 32 28)(48)");
        Permutation b = Permutation.parse("(1 13 47 27)(2 12 48 30)(3 9 45 31)(33 35 39 37)(34 38 40 36)");
        Permutation d = Permutation.parse("(13 21 29 37)(15 23 31 39)(16 24 32 40)(41 43 47 45)(42 46 48 44)");

        //With the following choice of generator sequence the white face is stabilized completely first
        String[] labels = {"B","F","R","L","T","D"};

        Permutation[] generators = {b,f,r,l,t,d};

        rubiksGroup = new PermutationGroup("RubikNice",labels,generators);

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
        assertEquals("[1, 5, 3, 7, 14, 6, 8, 15, 4, 2, 13, 22, 12, 16, 24, 30, 23, 32]", Arrays.toString(base));
    }

    @Test
    void createMinkwitzChain(){
        long start = System.currentTimeMillis();
        int numberOfElements = 20000;
        MinkwitzChain chain = rubiksGroup.getMinkwitzChain(numberOfElements);
        long end = System.currentTimeMillis();
        rubiksGroup.visualizeMinkwitzChain(chain);
        System.out.println("Calculated in: " + (end - start) + " ms");
        System.out.println("Missing: "+chain.getNumberOfMissingElements());
        System.out.println("Average Word Length: "+chain.getAverageWordLength());
        System.out.println("Group Size: " + rubiksGroup.getSize());
    }

    @Test
    void createExtendedMinkwitzChain(){
        long start = System.currentTimeMillis();
        int preTraining = 0;
        int numberOfElements = 800000;
        int maxBranching = 1;
        int simplificationRules =800;
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
    void getNiceStabilizerChain(){
        StabilizerChain chain = rubiksGroup.getStabilizerChain();
        while(chain.getOrbit().size()>0){
            Map<Byte,Permutation> coset_res = chain.getCosetRepresentatives();
            for (int i = 0; i < 48; i++) {
                if (chain.getOrbit().contains(Byte.parseByte(String.valueOf(i+1)) )){
                    if (!coset_res.get(Byte.parseByte(String.valueOf(i+1))).isIdentity())
                        System.out.print((i+1)+"\t");
                    else
                        System.out.print("\033[0;1m" +(i+1)+"\t"+ "\033[0m");
                }
                else{
                    System.out.print("\t");
                }
            }
            System.out.println();
            chain = chain.getStabilizer();
        }
    }

    @Test
    void getNiceStabChainData(){
        long start = System.currentTimeMillis();
        int preTraining = 0;
        int numberOfElements = 800000;
        int maxBranching = 1;
        int simplificationRules =800;
        ExtendedMinkwitzChain chain = rubiksGroup.getExtendedMinkwitzChain(preTraining,numberOfElements,simplificationRules,maxBranching,true);

        int count = 0;
        while(chain.getOrbit().size()>0){
            Map<Byte,TreeSet<GroupElement>> coset_res = chain.getCosetRepresentativesMap();
            for (Map.Entry<Byte, TreeSet<GroupElement>> entry : coset_res.entrySet()) {
                System.out.println("("+entry.getKey()+",0,-"+1.5*count+"):"+entry.getValue().first().getWord());
            }
            count++;
            chain = chain.getStabilizerChain();
        }
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
        int preTraining = 0;
        int numberOfElements = 8000000;
        int maxBranching = 1;
        int simplificationRules = 30487;
        List<GroupElement> elements = rubiksGroup.elementToWordExtended(superFlip,preTraining, numberOfElements,simplificationRules,maxBranching,10,true);
        System.out.println("My word representation of the super flip with "+elements.size()+" versions! ");
        for (GroupElement element : elements) {
            element.apply(rubiksGroup.getSimplifyingRules(simplificationRules));
        }
        List<String> subList = elements.stream().map(GroupElement::toFullWordString).filter(s -> s.length()<60).toList();
        for (String s : subList) {
            System.out.println(s.length()+": "+s);
        }
        System.out.println("Amount of simplification rules: "+rubiksGroup.getSimplifyingRules(simplificationRules).size());
        rubiksGroup.saveSimplificationRules();;
    }

    @Test
    void getFirst10000Elements  () {

        int count =0;
        for (GroupIterator it = this.rubiksGroup.getIterator(8000000); it.hasNext(); ) {
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
        GroupElement elem = rubiksGroup.wordToElement("FFdFtRFrTfDFBLBFbbflfbffLLFLrbLRfLdRlTrdBFtbTfrDbRLDbFRlDt");
        System.out.println(elem.getPermutation());
        elem.apply(rubiksGroup.getSimplifyingRules(30741));
        System.out.println(elem.toFullWordString().length()+": "+elem.toFullWordString());
        rubiksGroup.simplifyWord("FFdFtRFrTfDFBLBFbbflfbffLLFLrbLRfLdRlTrdBFtbTfrDbRLDbFRlDt",17000000);

    }

    @Test
    void saveSimplificationRules(){
        System.out.println(rubiksGroup.getSimplifyingRules(800).size());
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
    }
    
}