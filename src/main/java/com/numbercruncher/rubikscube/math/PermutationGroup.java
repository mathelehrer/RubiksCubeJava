package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.utils.StringUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private final Permutation one;
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

        this.invGens = IntStream.range(0,labels.length)
                .boxed()
                .collect(Collectors.toMap(i -> StringUtils.toggleCase(labels[i]),i -> generators.get(i).inverse()));
        this.generatorLabels=List.of(labels);

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

    public BigInteger getSize(){
        StabilizerChain chain = getStabilizerChain();
        return calcGroupSize(chain);
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



    /*****************************
     **** private methods  *******
     *****************************/

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
            extend(this.stabilizerChain,generator);
        }
    }

    private void extend(StabilizerChain chain,Permutation g){
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
                    extend(chain.getStabilizer(),s);
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
                        extend(chain.getStabilizer(),s);
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
                            extend(chain.getStabilizer(),s);
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
        byte first = orbit.get(0);
        List<Byte> orbitCopy = new ArrayList<>(orbit);
        Collections.sort(orbitCopy);

        String line="";
        byte pos = 0;
        for (byte b : orbitCopy) {
            for (int tab = pos; tab < b; tab++) {
                line+="\t";
            }
            if (b==first){
                line+=b+"*\t";
            }
            else{
                line+=b+"\t";
            }
            pos=(byte) (b+1);
        }
        return line;
    }

    private String visualizeStabilizerChain(StabilizerChain stabilizer) {
        if (stabilizer.isLast()){
            return "\n";
        }
        else{
            return generateOrbitLine(stabilizer.getOrbit())+"\n"+visualizeStabilizerChain(stabilizer.getStabilizer());
        }
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


}
