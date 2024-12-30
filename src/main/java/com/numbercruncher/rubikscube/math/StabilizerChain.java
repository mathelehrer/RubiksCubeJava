package com.numbercruncher.rubikscube.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The class StabilizerChain
 *
 * @author NumberCruncher
 * @since 2024-12-30
 * @version 2024-12-30
 */
public class StabilizerChain {

    /*****************************
     **** Attribute **************
     *****************************/
    private List<Permutation> generators;
    private List<Byte> orbit;
    private Map<Byte,Permutation> cosetRepresentative;
    private StabilizerChain stabilizer;
    /*****************************
     **** Konstruktor*************
     *****************************/

    public StabilizerChain() {
        this.generators=new ArrayList<>();
        this.orbit=new ArrayList<>();
        this.cosetRepresentative=new HashMap<>();
        this.stabilizer=null;
    }

    /*****************************
     **** Getter    **************
     *****************************/
    public List<Permutation> getGenerators() {
        return generators;
    }

    public List<Byte> getOrbit() {
        return orbit;
    }

    public Permutation getCosetRepresentative(Byte point){
        return cosetRepresentative.get(point);
    }

    public StabilizerChain getStabilizer(){
        return stabilizer;
    }

    public Map<Byte,Permutation> getCosetRepresentatives(){
        return cosetRepresentative;
    }
    /*****************************
     **** Setter    **************
     *****************************/


    public void setStabilizer(StabilizerChain stabilizer){
        this.stabilizer=stabilizer;
    }


    public void addGenerator(Permutation generator){
        this.generators.add(generator);
    }

    public void addOrbitPoint(Byte point){
        this.orbit.add(point);
    }

    public void addCosetRepresentative(Byte point,Permutation permutation){
        this.cosetRepresentative.put(point,permutation);
    }

/*****************************
 **** public methods *********
 *****************************/

    /**
     * Checks whether the current stabilizer chain is the last one in the sequence.
     *
     * @return true if the stabilizer of this chain is null, indicating it is the last one; otherwise false.
     */
    public boolean isLast(){
        return this.stabilizer==null;
    }

    /**
     * Checks the consistency of the stabilizer chain by comparing the size of the coset representative
     * map and the size of the orbit list.
     *
     * @return true if the size of the coset representative map is equal to the size of the orbit list,
     * indicating consistency; false otherwise.
     */
    public boolean isConsistent(){
        return this.cosetRepresentative.size()==this.orbit.size();
    }


    public Permutation randomElement(){
        return null;
    }
    /*****************************
     **** private methods  *******
     *****************************/
    private String buildToString(StabilizerChain chain,int depth){
        String out="";
        String indent="";
        for (int i=0;i<depth;i++)
            indent+="\t";


        out+=indent;
        out+="orbit: ["+chain.getOrbit().stream().map(Object::toString).collect(Collectors.joining(","))+"]\n";
        out+=indent;
        out+="coset representative: ["+chain.getCosetRepresentatives().entrySet().stream().map(Object::toString).collect(Collectors.joining(","))+"].\n";

        if (!chain.isLast()){
            out+=indent;
            out+="stabilizer: \n"+buildToString(chain.getStabilizer(),depth+1);
        }
        return out;
    }

    /*****************************
     **** overrides     **********
     *****************************/

    public String toString(){
        String out="Stabilizer chain: \n";
        out+=buildToString(this,0);
        return out;

    }

}
