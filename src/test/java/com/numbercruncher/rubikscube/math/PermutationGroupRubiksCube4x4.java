package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.utils.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermutationGroupRubiksCube4x4 {
    private PermutationGroup rubiksGroup;

    @BeforeEach
    void setUp() {
        //arabic letters turn the outer plane
        //greek letters turn the second row planes
        Permutation t = Permutation.parse("(1 13 9 5)(2 14 10 6)(3 15 11 7)(4 16 12 8)(17 45 57 77)(29 41 53 73)(30 42 54 74)(31 43 55 75)(96)");
        Permutation τ = Permutation.parse("(18 46 58 78)(20 48 60 80)(27 39 51 71)(32 44 56 76)(96)");
        Permutation r = Permutation.parse("(1 33 89 73)(13 45 85 69)(14 46 86 70)(15 47 87 71)(49 61 57 53)(50 62 58 54)(51 63 59 55)(52 64 60 56)(96)");
        Permutation ρ = Permutation.parse("(2 34 90 74)(4 36 92 76)(11 43 83 67)(16 48 88 72)(96)");
        Permutation f = Permutation.parse("(1 17 81 49)(2 18 82 50)(3 19 83 51)(5 21 85 53)(33 45 41 37)(34 46 42 38)(35 47 43 39)(36 48 44 40)(96)");
        Permutation φ = Permutation.parse("(4 20 84 52)(6 22 86 54)(8 24 88 56)(15 31 95 63)(96)");
        Permutation l = Permutation.parse("(5 77 93 37)(6 78 94 38)(7 79 95 39)(9 65 81 41)(17 29 25 21)(18 30 26 22)(19 31 27 23)(20 32 28 24)(96)");
        Permutation λ = Permutation.parse("(3 75 91 35)(8 80 96 40)(10 66 82 42)(12 68 84 44)");
        Permutation b = Permutation.parse("(9 57 89 25)(10 58 90 26)(11 59 91 27)(13 61 93 29)(65 77 73 69)(66 78 74 70)( 67 79 75 71)(68 80 76 72)(96)");
        Permutation β = Permutation.parse("(7 55 87 23)(12 60 92 28)(14 62 94 30)(16 64 96 20)");
        Permutation d = Permutation.parse("(21 65 61 33)(22 66 62 34)(23 67 63 35)(25 69 49 37)(81 93 89 85)(82 94 90 86)(83 95 91 87)(84 92 96 88)");
        Permutation δ = Permutation.parse("(19 79 59 47)(24 68 64 36)(26 70 50 38)(28 72 52 40)(96)");


        int[] indices = new int[]{0,1,2,3,4,5,6,7,8,9,10,11};
        List<Integer> indexList = new ArrayList<>();
        for (int i : indices) indexList.add(i);
        Collections.shuffle(indexList);

        String[] randomLabels = new String[12];
        Permutation[] randomGenerators = new Permutation[12];

        String[] labels = {"T","Τ","L","Λ","F","Φ","R","Ρ","B","Β","D","Δ"};
        Permutation[] generators = {t,τ,l,λ,f,φ,r,ρ,b,β,d,δ};

        int count = 0;
        for (Integer i : indexList) {
            randomLabels[count]=labels[i];
            randomGenerators[count++]=generators[i];
        }

        System.out.println("Generator sequence: " + Arrays.asList(randomLabels) + ": ");
        rubiksGroup = new PermutationGroup("Rubik's Cube Group",randomLabels,randomGenerators);

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
    void consistencyCheck(){
        List<GroupElement>generators= rubiksGroup.getGroupElementGenerators();
        for (GroupElement generator : generators) {
            System.out.println(generator.multiply(generator).multiply(generator).multiply(generator).toFullString());
        }
        StabilizerChain chain = rubiksGroup.getStabilizerChain();
        List<Byte> orbit0 = chain.getOrbit();
        List<Byte> orbit = chain.getStabilizer().getOrbit();
        System.out.println(orbit0.size());
        System.out.println(orbit.size());
    }

    @Test
    void multiplyPermutations() {
        List<GroupElement> generators = rubiksGroup.getGroupElementGenerators();
        for (GroupElement generator : generators) {
            for (GroupElement groupElement : generators) {
                System.out.println(generator.multiply(groupElement));
            }
        }
    }

    @Test
    void getBase(){
        byte[] base = rubiksGroup.getBaseAsByteArray();
        assertEquals("[1, 27, 5, 31, 19, 24, 39, 28, 51, 44, 71, 7, 23, 79, 20, 48, 56, 60, 32, 11, 9, 75, 13, 21, 3, 4, 43, 55, 15, 35, 8, 91, 12, 40, 16, 68, 76, 80, 25, 83, 36, 47, 63, 52, 84, 59, 64, 87, 33, 67, 72, 88, 73, 22, 62, 70, 50, 34, 66, 82, 54, 14, 2, 42, 57, 89, 49, 61, 69, 85, 86, 65, 93, 81, 37, 10, 77, 29, 74, 26, 90, 30, 58, 46, 18, 38, 6, 41, 17, 78, 53, 45]", Arrays.toString(base));
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
        assertEquals(97,rubiksGroup.getDegree());
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
            for (int i = 0; i < rubiksGroup.getDegree(); i++) {
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
        BigInteger size = rubiksGroup.getSize();
        System.out.println(size);
        assertTrue(size.divide(new BigInteger("16972688908618238933770849245964147960401887232000000000")).equals(BigInteger.ONE));
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
    void getFirst1000Elements  () {

        int count =0;
        for (GroupIterator it = this.rubiksGroup.getIterator(1000); it.hasNext(); ) {
            GroupElement element = it.next();
            count++;
            if (count%100==0)
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