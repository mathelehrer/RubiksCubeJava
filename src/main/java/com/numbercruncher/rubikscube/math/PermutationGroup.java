package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.logger.Logger;
import com.numbercruncher.rubikscube.utils.IOUtils;
import com.numbercruncher.rubikscube.utils.StringUtils;

import javax.swing.*;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.numbercruncher.rubikscube.utils.StringUtils.subWords;

public class PermutationGroup {

    /*****************************
     **** Attribute **************
     *****************************/
    private final List<Permutation> generators;
    private final String name;
    private final Map<String,Permutation> gens;
    private final Map<String,Permutation> invGens;
    private final List<String> generatorLabels;
    private int degree;
    private final List<Function<String,String>> rules;
    private final List<Function<Byte,Boolean>> basisSelectionRules;
    private StabilizerChain stabilizerChain;
    private MinkwitzChain minkwitzChain;
    private ExtendedMinkwitzChain extendedMinkwitzChain;
    private GroupIterator iterator;
    private Base base;

    private final List<GroupElement> groupElementGenerators;
    private TreeMap<String,GroupElement> wordGeneratorMap;

    private final Permutation one;
    private TreeMap<String, String> simplifyingRules;

    //load the data of shortest words from a file
    //each list of bytes encodes
    private List<String> shortestWords;

    /*****************************
     **** Konstruktor*************
     *****************************/

    public PermutationGroup(String name, List<Permutation> generators,String... generatorNames) {
        String[] labels = new String[generators.size()];
        //generate default labels
        for (int i = 0; i < labels.length; i++) {
            labels[i]=""+(char)(97+i);
        }
        //replace with custom labels
        for (int i = 0; i < generatorNames.length; i++) {
            labels[i]=generatorNames[i];
        }
        this.name = name;
        this.generators = generators;

        this.gens = IntStream.range(0, labels.length)
                .boxed() // Converts the int stream to an Integer stream
                .collect(Collectors.toMap(i -> labels[i], i -> generators.get(i)));

        this.groupElementGenerators = new ArrayList<>();
        for (Map.Entry<String, Permutation> entry: gens.entrySet()) {
            this.groupElementGenerators.add(new GroupElement(entry.getValue(),entry.getKey()));
        }

        this.invGens = IntStream.range(0,labels.length)
                .boxed()
                .collect(Collectors.toMap(i -> StringUtils.toggleCase(labels[i]),i -> generators.get(i).inverse()));
        this.generatorLabels=List.of(labels);

        for (Map.Entry<String, Permutation> entry : invGens.entrySet()) {
            this.groupElementGenerators.add(new GroupElement(entry.getValue(),entry.getKey()));
        }

        this.rules=new ArrayList<>();
        this.basisSelectionRules=new ArrayList<>();

        for (String label : generatorLabels) {
            rules.add(s -> {
                String previous;
                do{

                    previous=s;
                    s=s.replaceAll(label+StringUtils.toggleCase(label),"")
                            .replaceAll(StringUtils.toggleCase(label)+label,"");

                }while(!s.equals(previous));
                return s;
            });
        }

        this.one=Permutation.parse("("+(this.getDegree()-1)+")");
    }

    public PermutationGroup(List<Permutation> generators) {
        this("Group",generators);
    }

    public PermutationGroup(String name, String[] generatorNames, Permutation... generators) {
        this(name,List.of(generators),generatorNames);
    }

    public void generateWordRules(){

    }

    /*****************************
     **** Getter    **************
     *****************************/

    public int getDegree(){
        if (this.degree==0){
            this.degree=generators.get(0).getDegree();
        }
        return this.degree;
    }

    public StabilizerChain getStabilizerChain(){
        if (this.stabilizerChain==null){
            schreierSims();
        }
        return this.stabilizerChain;
    }

    public String getName(){
        return this.name;
    }

    public BigInteger getSize(){
        StabilizerChain chain = getStabilizerChain();
        return calcGroupSize(chain);
    }

    public Base getBase(){
        if (this.base==null){
            computeBase();
        }
        return this.base;
    }

    public byte[] getBaseAsByteArray(){
        return this.getBase().getBase();
    }

    public List<GroupElement> getGroupElementGenerators(){
        return this.groupElementGenerators;
    }

    public TreeMap<String,String> getSimplifyingRules(int numberOfRules){
        return getSimplifyingRules(numberOfRules,false);
    }

    public TreeMap<String,String> getSimplifyingRules(int numberOfRules, boolean verbose) {
        if (this.simplifyingRules==null) {
            this.generateSimplificationRules(numberOfRules, verbose);
        }
        return this.simplifyingRules;
    }

    public MinkwitzChain getMinkwitzChain(int numberOfElements, boolean verbose){
        if (this.minkwitzChain==null){
            createMinkwitzChain(numberOfElements,verbose);
        }
        return this.minkwitzChain;
    }

    public MinkwitzChain getMinkwitzChain(int numberOfElements){
        return this.getMinkwitzChain(numberOfElements,false);
    }

    public ExtendedMinkwitzChain getExtendedMinkwitzChain(int preTraining, int numberOfElements,int numberOfSimplificationRules,int maxBranching, boolean verbose){
        //initialize simplification rules
        if (numberOfSimplificationRules>0){
            this.getSimplifyingRules(numberOfSimplificationRules,verbose);
        }
        if (this.extendedMinkwitzChain==null){
            createExtendedMinkwitzChain(preTraining,numberOfElements,maxBranching,verbose);
        }
        return this.extendedMinkwitzChain;
    }

    public ExtendedMinkwitzChain getExtendedMinkwitzChain(int preTraining, int numberOfElements,int numberOfSimplificationRules,int maxBranching){
        return this.getExtendedMinkwitzChain(preTraining, numberOfElements,numberOfSimplificationRules, maxBranching,false);
    }

    public ExtendedMinkwitzChain getExtendedMinkwitzChain(int preTraining, int numberOfElements){
        return this.getExtendedMinkwitzChain(preTraining, numberOfElements,0,1,false);
    }

    public TreeMap<String, GroupElement> getWordGeneratorMap() {
        if (this.wordGeneratorMap==null){
            this.wordGeneratorMap = new TreeMap<>();
            for (GroupElement generator : this.groupElementGenerators) {
                this.wordGeneratorMap.put(generator.getWord(),generator);
            }
        }
        return wordGeneratorMap;
    }

    /*****************************
     **** Setter    **************
     *****************************/

    public void addWordRule(Function<String,String> rule){
        this.rules.add(rule);
    }

    public void addBasisSelectionRule(Function<Byte,Boolean> rule){
        this.basisSelectionRules.add(rule);
    }



    /*****************************
     **** public methods *********
     *****************************/

    public GroupIterator getIterator(){
        return this.getIterator(-1);
    }

    public GroupIterator getIterator(int numberOfElements){
        return new GroupIterator(this, numberOfElements);
    }

    public boolean contains(GroupElement element){
        return this.contains(element.getPermutation());
    }

    public boolean contains(Permutation perm){
        return checkElement(perm, getStabilizerChain());
    }

    public GroupElement randomElement(int n){
        GroupElement out = new GroupElement(one,"");
        for (int i = 0; i < n; i++) {
            Optional<Map.Entry<String, Permutation>> optional;
            String label = generatorLabels.get((int)(Math.random()*generatorLabels.size()));
            if (Math.random() < 0.5)
            //pick random map.entry from gens
            {
                Permutation perm = gens.get(label);
                out=out.multiply(new GroupElement(perm,label));
            } else {
                label = StringUtils.toggleCase(label);
                Permutation perm = invGens.get(label);
                out=out.multiply(new GroupElement(perm,label));
            }

        }
        out.wordSimplify(rules);
        return out;
    }

    public void visualizeStabilizerChain(){
        StabilizerChain chain = getStabilizerChain();
        String out = "Stabilizer chain orbit structure:\n";
        out+= "=================================\n";

        out+=generateOrbitLine(IntStream.range(0,this.degree).boxed().map(v->Byte.parseByte(v+"")).collect(Collectors.toList()))+"\n";
        for (int i = 0; i < this.degree; i++) {
            out+="====";
        }
        out+="\n";

        List<Byte> orbit = chain.getOrbit();
        out+=generateOrbitLine(orbit)+"\n";

        out+=visualizeStabilizerChain(chain.getStabilizer());
        System.out.println(out);
    }


    public MinkwitzChain trainMinkwitzChain(MinkwitzChain minkwitzChain, int start, int end) {

        int max = 0;
        for (GroupIterator it = this.getIterator(end); it.hasNext() && max<=end; ) {
            GroupElement permutation = it.next();

            if (max>=start)
                trainSubChain(minkwitzChain,permutation);
            if (max%20000==0)
                minkwitzChain.save("_"+max);

            max++;
        }

        return minkwitzChain;
    }

    /**
     * low level training, the elements are only added to the lowest level stabilizer group
     * fast and a large number of elements can be processed
     * @param extendedMinkwitzChain
     * @param end
     * @return
     */
    public ExtendedMinkwitzChain stabilizerTrainingExtended(ExtendedMinkwitzChain extendedMinkwitzChain, int end) {
        int max = 1;
        for (GroupIterator it = this.getIterator(end); it.hasNext() && max<=end; ) {
            GroupElement permutation = it.next();

            trainStabilizerExtendedSubChain(extendedMinkwitzChain,permutation,0);

            max++;
        }
        extendedMinkwitzChain.save("_"+max+"_0");
        return extendedMinkwitzChain;
    }


    public ExtendedMinkwitzChain trainExtendedMinkwitzChain(ExtendedMinkwitzChain extendedMinkwitzChain,int preTraining, int start, int end,int maxBranching) {
        System.out.println("Start training: "+start+" - "+end);
        int max = 1;
        for (GroupIterator it = this.getIterator(end); it.hasNext() && max<=end; ) {
            GroupElement permutation = it.next();

            if (max%10000==0)
                System.out.println(max+": "+"Train with: "+permutation);
            if (max>=start)
                trainExtendedSubChain(extendedMinkwitzChain,permutation,maxBranching,0);

            max++;
        }

        if (maxBranching==0)
            extendedMinkwitzChain.save("_"+preTraining+"_"+max);
        else
            extendedMinkwitzChain.save("_"+preTraining+"_"+max+"_"+maxBranching);

        return extendedMinkwitzChain;
    }

    public void visualizeMinkwitzChain(MinkwitzChain chain){
        String out = "Minkwitz chain orbit structure:\n";
        out+= "====================+++++======\n";

        out+=generateOrbitLine(
                IntStream.range(0,this.degree)
                        .boxed()
                        .map(v->Byte.parseByte(v+""))
                        .collect(Collectors.toList()), 2)+"\n";
        for (int i = 0; i < this.degree; i++) {
            out+="========";
        }
        out+="\n";

        out+= visualizeMinkwitzChainRecursive(chain);
        System.out.println(out);
    }

    public void visualizeExtendedMinkwitzChain(ExtendedMinkwitzChain chain){
        String out = "Extended Minkwitz chain orbit structure:\n";
        out+= "=========++++++++===========+++++======\n";

        out+=generateOrbitLine(
                IntStream.range(0,this.degree)
                        .boxed()
                        .map(v->Byte.parseByte(v+""))
                        .collect(Collectors.toList()), 2)+"\n";
        for (int i = 0; i < this.degree; i++) {
            out+="========";
        }
        out+="\n";

        out+= visualizeExtendedMinkwitzChainRecursive(chain);
        System.out.println(out);
    }


    /**
     * This method computes the word representation for an arbitrary permutation of the group.
     * It uses the Minkwitz chain to compute a list of stabilizer representatives, whose product is equal to the permutation
     * @param permutation
     * @return
     */
    public String elementToWord(Permutation permutation, int numberOfElements,int numberOfRules,boolean verbose){
        MinkwitzChain chain = this.getMinkwitzChain(numberOfElements,verbose);
        GroupElement element  = elementToWordRecursive(permutation,chain);
        element.apply(this.getSimplifyingRules(numberOfRules,verbose),verbose);
        return element.getWord();
    }

    public List<GroupElement> elementToWordExtended(Permutation permutation,int preTraining, int numberOfElements,int numberOfRules, int maxBranching,boolean verbose){
        ExtendedMinkwitzChain chain = this.getExtendedMinkwitzChain(preTraining,numberOfElements,numberOfRules,maxBranching,verbose);
        return elementToWordRecursiveExtended(permutation,chain,0,2);
    }

    public List<GroupElement> elementToWordExtended(Permutation permutation,int preTraining, int numberOfElements,int numberOfRules, int maxBranching,int maxDepth,boolean verbose){
        ExtendedMinkwitzChain chain = this.getExtendedMinkwitzChain(preTraining,numberOfElements,numberOfRules,maxBranching,verbose);
        return elementToWordRecursiveExtended(permutation,chain,0,maxDepth);
    }

    public String elementToWord(Permutation permutation, int numberOfElements,int numberOfRules){
        return elementToWord(permutation,numberOfElements,numberOfRules,false);
    }


    /** Convert a given String of letters into a group element
     *
     * @param word
     * @return
     */
    public GroupElement wordToElement(String word){
        GroupElement out = new GroupElement(one,"");
        for(int i = 0; i < word.length(); i++)
            out =out.multiply(this.getWordGeneratorMap().get(word.substring(i,i+1)));
        return out;
    }

    public void generateSimplificationRules(int numberOfRules,boolean verbose){
        if (loadSimplificationRules(verbose,numberOfRules)){
            if (verbose) System.out.println("Simplification rules loaded!");
            return ;
        }

        if (verbose) System.out.println("Try to create " + numberOfRules + " simplifying rules");
        //Construct a trivial group iterator
        Deque<GroupElement> queue=new ArrayDeque<>();
        TreeMap<Base, String> elements = new TreeMap<>();
        //Store rules in reverse order that the longer rules are applied first
        this.simplifyingRules = new TreeMap<>(new SimplifyingRuleComparator());

        boolean limitReached = false;
        Base base = this.getBase();
        int counter = 0;

        elements.put(base,"");
        queue.offer(new GroupElement(one,""));

//        List<GroupElement> reversedGenerators = new ArrayList<>();
//        for (GroupElement groupElementGenerator : groupElementGenerators) {
//            reversedGenerators.add(0,groupElementGenerator);
//        }
        int old =0;
        while (!queue.isEmpty()) {
            GroupElement element = queue.poll();

            //if the queue once exceeds the maximum number of elements we can stop generating new elements
            if (this.simplifyingRules.size() > numberOfRules) limitReached = true;


            if (old!=this.simplifyingRules.size()  && this.simplifyingRules.size()%100==0){
                System.out.println(this.simplifyingRules.size());
                old = this.simplifyingRules.size() ;
            }

            if (numberOfRules == -1 || !limitReached) {
                //make sure that the queue is extended with every possible child of the element that is extracted from the queue
                for (GroupElement generator : this.groupElementGenerators) {
                    GroupElement next = element.multiply(generator);
                    Base nextBase = base.action(next.getPermutation());


                    if (!elements.containsKey(nextBase)) {
                        queue.offer(next);
                        elements.put(nextBase, next.getWord());
                    } else {
                        //state simplification rule
                        next.apply(this.simplifyingRules);
                        String src = next.getWord();
                        String target = elements.get(nextBase);
                        if (src.length() > target.length()) {
                            counter++;
                            if (verbose ) System.out.println(counter+": "+src + "->" + target);
                            this.simplifyingRules.put(src, target);
                        }

                    }
                }
            }
        }

        saveSimplificationRules(verbose,numberOfRules);
        if (verbose) System.out.println("Simplification rules generated!");
    }

    public void saveSimplificationRules(){
        saveSimplificationRules(true,this.simplifyingRules.size());
    }

    /**
     * This method tries to simplify the parameter word.
     * It creates all possible partitions of various length and tries to find replacements
     * elements of the first shortest words that generate the same basis image
     * @param word
     * @return
     */
    public String simplifyWord(String word, int nShortestWords){
        System.out.println(word+" is replaced by: ");
        String simplerWord="";

        Base base = this.getBase();
        int baseLength = base.length();
        TreeMap<Base,String> baseImageMap =  new TreeMap<>();

        int count = 0;
        for (GroupIterator it = this.getIterator(nShortestWords); it.hasNext(); ) {
            GroupElement g = it.next();
            baseImageMap.put(base.action(g.getPermutation()),g.toFullWordString());

        }

        String oldWord = word;
        String newWord = "";
        while (oldWord.length()>newWord.length()) {
            boolean replaced = false;
            for (int i = word.length(); i > 1; i--) {
                List<String> parts = subWords(word, i);
                for (String part : parts) {
                    GroupElement element = wordToElement(part);
                    Base image = base.action(element.getPermutation());
                    if (baseImageMap.containsKey(image)) {
                        String replacement = baseImageMap.get(image);
                        if (part.length() > replacement.length()) {
                            newWord = oldWord.replace(part, replacement);
                            replaced = true;
                            System.out.println(oldWord.length()+"->"+newWord.length());
                            oldWord = newWord;
                            break;
                        }
                    }
                    if (replaced) break;
                }
            }
        }

        System.out.println(newWord);
        return simplerWord;
    }

    /*****************************
     **** private methods  *******
     *****************************/

    private void computeBase(){
        List<Byte> basePoints = new ArrayList<>(getBasePoints(this.getStabilizerChain()));
        this.base = new Base(basePoints);
    }

    private List<Byte> getBasePoints(StabilizerChain chain){
        if (chain.isLast()){
            return new ArrayList<Byte>();
        }
        else{
            List<Byte> subBase = getBasePoints(chain.getStabilizer());
            subBase.add(0,chain.getOrbit().get(0));
            return subBase;
        }
    }


    private BigInteger calcGroupSize(StabilizerChain chain) {
        if (chain.isLast()){
            return BigInteger.ONE;
        }
        else{
            return new BigInteger(String.valueOf(chain.getOrbit().size())).multiply(calcGroupSize(chain.getStabilizer()));
        }
    }

    private void schreierSims(){
        this.stabilizerChain=new StabilizerChain();
        for (Permutation generator : generators) {
            schreierSimsRecursive(this.stabilizerChain,generator);
        }
    }

    private void schreierSimsRecursive(StabilizerChain chain, Permutation g){
        if (!this.contains(g)){
            if (chain.getGenerators().isEmpty()){
                //empty stabilizer chain
                chain.setStabilizer(new StabilizerChain());
                chain.addGenerator(g);
                byte beta = getBasePoint(g);
                chain.addOrbitPoint(beta);
                chain.addCosetRepresentative(beta,one);
                byte delta = g.action(beta);
                Permutation s = g.copy();
                while (delta!=beta){
                    chain.addOrbitPoint(delta);
                    chain.addCosetRepresentative(delta,s);
                    s=s.multiply(g);
                    delta=s.action(beta);
                }
                if (!s.isIdentity())
                    schreierSimsRecursive(chain.getStabilizer(),s);
            }
            else{
                // already existing stabilzer chain
                List<Byte> orbit = chain.getOrbit();
                int oldOrbitSize = orbit.size();

                //deal with the old orbit elements first
                for (int i=0;i<oldOrbitSize;i++){
                    byte delta = orbit.get(i);
                    byte gamma = g.action(orbit.get(i));
                    if (!orbit.contains(gamma)) {
                        //new orbit element
                        chain.addCosetRepresentative(gamma, chain.getCosetRepresentative(delta).multiply(g));
                        orbit.add(gamma);
                    }
                    else{
                        Permutation repGamma= chain.getCosetRepresentative(gamma);
                        Permutation repDelta= chain.getCosetRepresentative(delta);

                        //construct a new stabilizer element
                        Permutation s = repDelta.multiply(g.multiply(repGamma.inverse()));
                        schreierSimsRecursive(chain.getStabilizer(),s);
                    }
                }

                chain.addGenerator(g);
                //now act with all chain generators onto the new orbit elements
                for (int i = oldOrbitSize; i <orbit.size(); i++) {
                    for (Permutation generator : chain.getGenerators()) {
                        byte delta = orbit.get(i);
                        byte gamma = generator.action(delta);
                        if (!orbit.contains(gamma)) {
                            orbit.add(gamma);//this should make the for loop longer
                            chain.addCosetRepresentative(gamma,chain.getCosetRepresentative(delta).multiply(generator));
                        }
                        else{
                            Permutation repGamma= chain.getCosetRepresentative(gamma);
                            Permutation repDelta = chain.getCosetRepresentative(delta);
                            Permutation s = repDelta.multiply(generator.multiply(repGamma.inverse()));
                            schreierSimsRecursive(chain.getStabilizer(),s);
                        }
                    }
                }
            }
        }
    }

    private byte getBasePoint(Permutation g){
        List<Byte> omega = IntStream.range(0,this.degree)
                .mapToObj(v->Byte.parseByte(v+""))
                .collect(Collectors.toList());

        List<Byte> prefered =  IntStream.range(0,this.degree)
                .mapToObj(v->Byte.parseByte(v+""))
                .collect(Collectors.toList());

        for (Function<Byte,Boolean> rule : basisSelectionRules)
            prefered = prefered.stream().filter(rule::apply).collect(Collectors.toList());

        for (Byte b : prefered) {
            byte beta = g.action(b);
            if (beta!=b) return b;
        }

        //fall back to full omega, when the rules cannot be satisfied

        for (Byte b : omega) {
            byte beta = g.action(b);
            if (beta!=b) return b;
        }

        return -1;//this case should not occur :-)
    }
    /**
     * Checks whether a given permutation belongs to the group represented by the stabilizer chain.
     *
     * @param perm the permutation to be checked
     * @param chain the stabilizer chain representing the group
     * @return true if the permutation belongs to the group, false otherwise
     */
    private boolean checkElement(Permutation perm, StabilizerChain chain){
        if (chain.isLast()){
            if (perm.isIdentity())
                return true;
            else
                return false;
        }
        else{
            List<Byte> orbit = chain.getOrbit();
            byte omega = chain.getOrbit().get(0);
            byte delta = perm.action(omega);

            if (!orbit.contains(delta))
                return false;
            else{
                Permutation rep = chain.getCosetRepresentative(delta);
                perm=perm.multiply(rep.inverse());
                if (perm.isIdentity())
                    return true;
                else
                    return checkElement(perm,chain.getStabilizer());
            }

        }
    }

    private String generateOrbitLine(List<Byte> orbit) {
        return generateOrbitLine(orbit, 1);
    }


    private String generateOrbitLine(List<Byte> orbit,int nTabs) {
        byte first = orbit.get(0);
        List<Byte> orbitCopy = new ArrayList<>(orbit);
        Collections.sort(orbitCopy);

        StringBuilder tabs = new StringBuilder();
        for (int i = 0; i < nTabs; i++) tabs.append("\t");

        StringBuilder line= new StringBuilder();
        byte pos = 0;
        for (byte b : orbitCopy) {
            for (int tab = pos; tab < b; tab++) {
                line.append(tabs);
            }
            if (b==first){
                line.append(b).append("*").append(tabs);
            }
            else{
                line.append(b).append(tabs);
            }
            pos=(byte) (b+1);
        }
        return line.toString();
    }

    private void createMinkwitzChain(int numberOfElements, boolean verbose){
        //Try to load existing MinkwitzChain
        MinkwitzChain minkwitzChain = MinkwitzChain.load(this.name,numberOfElements);
        if (verbose) System.out.println("Minkwitz chain trained with "+numberOfElements+" is loaded!");
        if (minkwitzChain==null) {
            //Create new Minkwitz chain from the stabilizer chain data
            if (verbose) System.out.println("Creating a new Minkwitz chain ...");
            minkwitzChain = new MinkwitzChain(this.getStabilizerChain(),this.groupElementGenerators,this.name);
            trainMinkwitzChain(minkwitzChain, numberOfElements);
            if (verbose) System.out.println("...done!");
        }
        this.minkwitzChain = minkwitzChain;
    }


    private void createExtendedMinkwitzChain(int preTraining, int numberOfElements, int maxBranching, boolean verbose){
        //Try to load existing MinkwitzChain
        ExtendedMinkwitzChain extendedMinkwitzChain = ExtendedMinkwitzChain.load(this.name,preTraining,numberOfElements,maxBranching);
        if (verbose) System.out.println("Extended Minkwitz chain trained with "+numberOfElements+" is loaded!");
        if (extendedMinkwitzChain==null) {
            //Create new Minkwitz chain from the stabilizer chain data
            if (verbose) System.out.println("Creating a new Minkwitz chain ...");
            extendedMinkwitzChain = new ExtendedMinkwitzChain(this.getStabilizerChain(),this.groupElementGenerators,this.name);
            if (preTraining>0)
                extendedMinkwitzChain = stabilizerTrainingExtended(extendedMinkwitzChain,preTraining);

            trainExtendedMinkwitzChain(extendedMinkwitzChain, preTraining, 0, numberOfElements,maxBranching);
            if (verbose) System.out.println("...done!");
        }
        this.extendedMinkwitzChain = extendedMinkwitzChain;
    }

    private String generateMinkwitzLine(List<Byte> orbit, Map<Byte,GroupElement> cosetRepresentatives) {
        byte first =orbit.get(0);

        StringBuilder line= new StringBuilder();
        byte pos = 0;
        String tabs = "\t\t";
        TreeMap<Byte,GroupElement> sortedCosetRepresentatives = new TreeMap<>(cosetRepresentatives);

        for (Map.Entry<Byte,GroupElement> entry : sortedCosetRepresentatives.entrySet()) {
            Byte b = entry.getKey();
            for (int tab = pos; tab < b; tab++) {
                line.append(tabs);
            }
            if (b==first){
                StringBuilder out = new StringBuilder(b + "*");
                if (out.length()<4)
                    out.append("\t\t");
                else if (out.length()<8)
                    out.append("\t");
                line.append(out);
            }
            else{
                GroupElement rep = entry.getValue();
                StringBuilder out = null;
                if (rep!=null)
                    out = new StringBuilder(rep.toString());
                else
                    out = new StringBuilder(b+"");

                if (out.length()<4)
                    out.append("\t\t");
                else if (out.length()<8)
                    out.append("\t");
                line.append(out);
            }
            pos=(byte) (b+1);
        }
        return line.toString();
    }

    private String generateExtendedMinkwitzLine(List<Byte> orbit, Map<Byte,TreeSet<GroupElement>> cosetRepresentatives) {
        byte first =orbit.get(0);

        StringBuilder line= new StringBuilder();
        byte pos = 0;
        String tabs = "\t\t";
        TreeMap<Byte,TreeSet<GroupElement>> sortedCosetRepresentatives = new TreeMap<>(cosetRepresentatives);

        for (Map.Entry<Byte,TreeSet<GroupElement>> entry : sortedCosetRepresentatives.entrySet()) {
            Byte b = entry.getKey();
            for (int tab = pos; tab < b; tab++) {
                line.append(tabs);
            }
            if (b==first){
                StringBuilder out = new StringBuilder(b + "*");
                if (out.length()<4)
                    out.append("\t\t");
                else if (out.length()<8)
                    out.append("\t");
                line.append(out);
            }
            else{
                GroupElement rep = entry.getValue().first();
                StringBuilder out = null;
                if (rep!=null)
                    out = new StringBuilder(rep.toString());
                else
                    out = new StringBuilder(b+"");

                if (out.length()<4)
                    out.append("\t\t");
                else if (out.length()<8)
                    out.append("\t");
                line.append(out);
            }
            pos=(byte) (b+1);
        }
        return line.toString();
    }

    private String visualizeStabilizerChain(StabilizerChain chain) {
        if (chain.isLast()){
            return "\n";
        }
        else{
            return generateOrbitLine(chain.getOrbit())+"\n"+visualizeStabilizerChain(chain.getStabilizer());
        }
    }

    private String visualizeMinkwitzChainRecursive(MinkwitzChain chain) {
        if (chain.isLast()){
            return "\n";
        }
        else{
            return generateMinkwitzLine(chain.getOrbit(),chain.getCosetRepresentatives())+"\n"+ visualizeMinkwitzChainRecursive(chain.getStabilizerChain());
        }
    }

    private String visualizeExtendedMinkwitzChainRecursive(ExtendedMinkwitzChain chain) {
        if (chain.isLast()){
            return "\n";
        }
        else{
            return generateExtendedMinkwitzLine(chain.getOrbit(),chain.getCosetRepresentativesMap())+"\n"+ visualizeExtendedMinkwitzChainRecursive(chain.getStabilizerChain());
        }
    }


    private void trainMinkwitzChain(MinkwitzChain minkwitzChain, int numberOfElements) {
        this.trainMinkwitzChain(minkwitzChain,0,numberOfElements);
    }

    private void trainExtendedMinkwitzChain(ExtendedMinkwitzChain extendedMinkwitzChain,int preTrainging, int numberOfElements,int maxBranching) {
        this.trainExtendedMinkwitzChain(extendedMinkwitzChain,preTrainging,0,numberOfElements,maxBranching);
    }

    private void trainStabilizerExtendedSubChain(ExtendedMinkwitzChain extendedMinkwitzChain, GroupElement g,int depth) {
        if (!extendedMinkwitzChain.isLast()){
            List<Byte> orbit = extendedMinkwitzChain.getOrbit();
            byte omega = orbit.get(0);
            byte gamma = g.getPermutation().action(omega);
            if(omega==gamma){
                //group element is element of the stabilizer group
                trainStabilizerExtendedSubChain(extendedMinkwitzChain.getStabilizerChain(),g,depth+1);
            }
            else{
                extendedMinkwitzChain.addCosetRepresentative(gamma, g,depth);//all old elements of the list will be erased
            }
        }
    }

    private int trainSubChain(MinkwitzChain minkwitzChain, GroupElement g) {
        if (!minkwitzChain.isLast()){
            List<Byte> orbit = minkwitzChain.getOrbit();
            byte omega = orbit.get(0);
            byte gamma = g.getPermutation().action(omega);
            if(omega==gamma){
                //group element is element of the stabilizer group
                return trainSubChain(minkwitzChain.getStabilizerChain(),g);
            }
            else{
                //check whether it can be used as an orbit representative
                GroupElement rep = minkwitzChain.getCosetRepresentative(gamma);
                if (rep==null) {
                    minkwitzChain.addCosetRepresentative(gamma, g);
                    return gamma;
                }
                else{
                    //check whether the g is a better representative
                    if (rep.getWord().length()>g.getWord().length()) {
                        System.out.println(rep.getWord() + "->" + g.getWord());
                        minkwitzChain.addCosetRepresentative(gamma, g);
                        return gamma;
                    }
                    else{
                        //here we have two options to generate a stabilizer element
                        //1. g * rep^{-1}
                        int result1 =  trainSubChain(minkwitzChain.getStabilizerChain(),g.multiply(rep.inverse()));
                        //2. rep * g^{-1}
                        int result2 =  trainSubChain(minkwitzChain.getStabilizerChain(),rep.multiply(g.inverse()));
                        return Math.max(result1,result2);
                    }
                }
            }
        }
        return -1;
    }

    private int trainExtendedSubChain(ExtendedMinkwitzChain extendedMinkwitzChain, GroupElement g,int maxBranching,int depth) {
        if (maxBranching<1)
            maxBranching=1;

        if (!extendedMinkwitzChain.isLast()){
            List<Byte> orbit = extendedMinkwitzChain.getOrbit();
            byte omega = orbit.get(0);
            byte gamma = g.getPermutation().action(omega);
            if(omega==gamma){
                //group element is element of the stabilizer group
                return trainExtendedSubChain(extendedMinkwitzChain.getStabilizerChain(),g,maxBranching-1,depth+1);
            }
            else{
                //check whether it can be used as an orbit representative
                TreeSet<GroupElement> repList= extendedMinkwitzChain.getCosetRepresentativesMap().get(gamma);
                if (repList==null|| repList.isEmpty()) {
                    extendedMinkwitzChain.addCosetRepresentative(gamma, g,depth);
                    // System.out.println(gamma+"->"+g.getWord());
                    return gamma;
                }
                else{
                    //check whether the g is a better representative
                    GroupElement first = repList.first();
                    if (first.getWord().length()>g.getWord().length()) {
                        //System.out.println(first.getWord() + "->" + g.getWord());
                        //generate all possible stabilizer representatives first
                        int size = repList.size();
                        List<GroupElement> newRepList = new ArrayList<>(size);
                        if (size>maxBranching) {
                            newRepList.addAll(selectRandomSample(repList, maxBranching));
                        }
                        else{
                            newRepList.addAll(repList);
                        }
                        for (GroupElement rep  : newRepList) {
                            if (Math.random()<0.5)
                                trainExtendedSubChain(extendedMinkwitzChain.getStabilizerChain(),g.multiply(rep.inverse(),this.simplifyingRules),maxBranching-1,depth+1);
                            else
                                trainExtendedSubChain(extendedMinkwitzChain.getStabilizerChain(),rep.multiply(g.inverse(),this.simplifyingRules),maxBranching-1,depth+1);
                        }

                        extendedMinkwitzChain.addCosetRepresentative(gamma, g,depth);//all old elements of the list will be erased
                        return gamma;
                    }
                    else{
                        int result1 = -1,result2=-1;
                        int size = repList.size();
                        List<GroupElement> newRepList = new ArrayList<>(size);
                        if (size>maxBranching) {
                            newRepList.addAll(selectRandomSample(repList, maxBranching));
                        }
                        else{
                            newRepList.addAll(repList);
                        }
                        for (GroupElement rep : newRepList) {
                            //here we have two options to generate a stabilizer element
                            //1. g * rep^{-1}
                            if (Math.random()<0.5)
                                result1 = trainExtendedSubChain(extendedMinkwitzChain.getStabilizerChain(), g.multiply(rep.inverse(),this.simplifyingRules),maxBranching,depth+1);
                                //2. rep * g^{-1}
                            else
                                result2 = trainExtendedSubChain(extendedMinkwitzChain.getStabilizerChain(), rep.multiply(g.inverse(),this.simplifyingRules),maxBranching,depth+1);
                        }
                        extendedMinkwitzChain.addCosetRepresentative(gamma, g,depth+1);
                        return Math.max(result1,result2);

                    }
                }
            }
        }
        return -1;
    }


    /**
     * compute the Minkwitz chain representation of a given permutation.
     * @param permutation
     * @param chain
     * @return
     */
    private GroupElement elementToWordRecursive(Permutation permutation, MinkwitzChain chain) {
        List<Byte> orbit = chain.getOrbit();
        GroupElement identity = new GroupElement(one,"");
        GroupElement rep = null;

        if (orbit.isEmpty()) {
            rep = identity;
        }
        else {
            byte omega = orbit.get(0);
            byte gamma = permutation.action(omega);

            if (omega != gamma) {
                rep = chain.getCosetRepresentative(gamma);
            } else {
                rep = identity;
            }
        }
        if (chain.isLast()){
            return rep;
        }
        else {
            if (rep != null) {
                return elementToWordRecursive(permutation.multiply(rep.getPermutation().inverse()), chain.getStabilizerChain()).multiply(rep);
            } else {
                Logger.logging(Logger.Level.error, "Missing representative for in Minkwitz chain " + chain);
            }
        }
        return null;
    }



    private List<GroupElement> elementToWordRecursiveExtended(Permutation permutation, ExtendedMinkwitzChain chain) {
        return this.elementToWordRecursiveExtended(permutation, chain,0,2);
    }

    private TreeSet<GroupElement> elementToWordRecursiveExtended2(Permutation permutation, ExtendedMinkwitzChain chain,int depth) {
        List<Byte> orbit = chain.getOrbit();
        GroupElement identity = new GroupElement(one,"");
        TreeSet<GroupElement> rep = new TreeSet<>();

        if (orbit.isEmpty()) {
            rep.add(identity);
        }
        else {
            byte omega = orbit.get(0);
            byte gamma = permutation.action(omega);

            if (omega != gamma) {
                rep = chain.getCosetRepresentatives(gamma);
            } else {
                rep = new TreeSet<>();
                rep.add(identity);
            }
        }

        if (chain.isLast()){
            return rep;
        }
        else {
            //for each representative of the coset representatives we get the corresponding coset representatives from the stabilizer and right multiply each of them with the representative from the right
            TreeSet<GroupElement> allElements = new TreeSet<>();
            for (GroupElement element : rep) {
                TreeSet<GroupElement> levelLower = elementToWordRecursiveExtended2(permutation.multiply(element.getPermutation().inverse()), chain.getStabilizerChain(),depth+1);
                for (GroupElement part : levelLower) {
                    GroupElement next = part.multiply(element, simplifyingRules);
                    //System.out.println(tabs+"yielding: "+next.toFullString());
                    if (!allElements.contains(next)) {
                        allElements.add(next);
                    } else {
                        //Possible replace, when identical element with shorter word has been found
                        for (GroupElement groupElement : allElements) {
                            if (groupElement.compareTo(next) == 0) {
                                if (groupElement.getWord().length() > next.getWord().length()) {
                                    this.simplifyingRules.put(groupElement.toFullWordString(),next.toFullWordString());
                                    //System.out.println(groupElement.toFullWordString()+"->"+next.toFullWordString());
                                    allElements.remove(groupElement);
                                    allElements.add(next);
                                }
                                else if (groupElement.getWord().length() > next.getWord().length()){
                                    this.simplifyingRules.put(next.toFullWordString(),groupElement.toFullString());
                                    //System.out.println(next.toFullWordString()+"->"+groupElement.toFullWordString());
                                }
                                break;
                            }
                        }
                    }
                }
            }
            return allElements;
        }
    }

    private List<GroupElement> elementToWordRecursiveExtended(Permutation permutation, ExtendedMinkwitzChain chain,int depth, int maxDepth) {
        List<Byte> orbit = chain.getOrbit();
        GroupElement identity = new GroupElement(one,"");
        List<GroupElement> rep = new ArrayList<>();

        if (orbit.isEmpty()) {
            rep.add(identity);
        }
        else {
            byte omega = orbit.get(0);
            byte gamma = permutation.action(omega);

            if (omega != gamma) {
                rep = new ArrayList<>(chain.getCosetRepresentatives(gamma));
            } else {
                rep = new ArrayList<>();
                rep.add(identity);
            }
        }

        if (chain.isLast()){
            return rep;
        }
        else {
            List<GroupElement> allElements = new ArrayList<>();
            for (GroupElement element : rep) {
                List<GroupElement> levelLower;
                if (depth<maxDepth)
                    levelLower = elementToWordRecursiveExtended(permutation.multiply(element.getPermutation().inverse()), chain.getStabilizerChain(),depth+1,maxDepth);
                else
                    levelLower = new ArrayList(elementToWordRecursiveExtended2(permutation.multiply(element.getPermutation().inverse()), chain.getStabilizerChain(),depth+1));
                for (GroupElement part : levelLower) {
                    GroupElement next = part.multiply(element, simplifyingRules);
                    allElements.add(next);
                }
            }
            return allElements;
        }
    }

    private boolean loadSimplificationRules(boolean verbose,int numberOfRulesToLoad) {
        URL dirURL = IOUtils.getResourcePath("rules");
        String fileName = dirURL.getFile()+"/"+name+"_"+numberOfRulesToLoad+".txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))
        {
            String line;
            this.simplifyingRules = new TreeMap<>(new SimplifyingRuleComparator());

            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] parts = line.split("->");
                    String src = parts[0];
                    String target;
                    if (parts.length>1)
                        target = parts[1];
                    else
                        target = "";

                    this.simplifyingRules.put(src, target);
                }
            }
            return true;
        }
        catch(IOException ex){
            ex.getStackTrace();
            if (verbose) Logger.logging(Logger.Level.warning,ex.getMessage()+"\n");
        }
        return false;
    }

    private void saveSimplificationRules(boolean verbose, int numberOfRulesToSave) {
        URL dirURL = IOUtils.getResourcePath("rules");
        String fileName = dirURL.getFile() + "/" + name+"_"+numberOfRulesToSave + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Map.Entry<String, String> entry : this.simplifyingRules.entrySet()) {
                String line = entry.getKey() + "->" + entry.getValue();
                writer.write(line);
                writer.newLine(); // Add a new line after each rule
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            if (verbose) Logger.logging(Logger.Level.warning, ex.getMessage() + "\n");
        }
    }

    private List<GroupElement> selectRandomSample(TreeSet<GroupElement> elements, int numberOfElements) {


        List<GroupElement> newRepList = new ArrayList<>();
//        newRepList.add(elements.first());
//
//        return newRepList;
        TreeSet<Integer> selected = new TreeSet<>();
        int size = elements.size();

        for (int i = 0; i < numberOfElements; i++) {
            int select = (int) (Math.random()*size);
            while (selected.contains(select)) {
                select = (int) (Math.random()*size);
            }
            selected.add(select);
        }
        if (selected.size()==3)
            System.out.println((selected.stream().map(i->Integer.toString(i)).collect(Collectors.joining(","))));

        int count=0;
        for (GroupElement rep : elements) {
            if (selected.contains(count)) {
                newRepList.add(rep);
            }
            count++;
        }
        return newRepList;


    }
    /*****************************
     **** overrides     **********
     *****************************/
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(this.name+" generated by "+"\n");
        for (Map.Entry<String, Permutation> generator : gens.entrySet()) {
            out.append("\t\t").append(generator.getKey()).append(" = ").append(generator.getValue()).append("\n");
        }
        for (Map.Entry<String, Permutation> generator : invGens.entrySet()) {
            out.append("\t\t").append(generator.getKey()).append(" = ").append(generator.getValue()).append("\n");
        }
        return out.toString();
    }

    /***************************/
    /****** Statics ************/
    /***************************/

    public static PermutationGroup RubiksGroup(){
        Permutation t = Permutation.parse("(1 3 7 5)(2 6 8 4)(9 33 25 17)(10 34 26 18)(11 35 27 19)(48)");
        Permutation l = Permutation.parse("(1 17 41 39)(4 20 44 38)(5 21 45 35)(9 11 15 13)(10 14 16 12)(48)");
        Permutation f = Permutation.parse("(5 25 43 15)(7 29 41 11)(8 28 42 14)(17 19 23 21)(18 22 24 20)(48)");
        Permutation r = Permutation.parse("(3 37 43 19)(6 36 46 22)(7 33 47 23)(25 27 31 29)(26 30 32 28)(48)");
        Permutation b = Permutation.parse("(1 13 47 27)(2 12 48 30)(3 9 45 31)(33 35 39 37)(34 38 40 36)");
        Permutation d = Permutation.parse("(13 21 29 37)(15 23 31 39)(16 24 32 40)(41 43 47 45)(42 46 48 44)");

        //the order of the generators matters for the stabilizer chain
        //this way, it is almost possible to first stabilize all corners and then all edges
        String[] labels = {"T", "D", "L","R",  "F", "B"};
        PermutationGroup rubiksGroup = new PermutationGroup("Rubik's Cube Group", labels, t,d, l, r, f,  b );

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
        return rubiksGroup;
    }



}
