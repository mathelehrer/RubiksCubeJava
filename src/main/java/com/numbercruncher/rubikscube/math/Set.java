package com.numbercruncher.rubikscube.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The class Set is the natural structure that permutations are applied to
 * It is just a simple container of a byte array that has a string representation
 *
 * TODO: This can be made more general to store different entities than numbers
 * @author: NumberCruncher
 * Since: 30/12/2024
 * @version: 30/12/2024
 */
public class Set {

    /*************************************
     *********** Attributes ***************
     **************************************/
    private Byte[] set;

    /*************************************
     *********** Constructor ***************
     **************************************/
    public Set(int size){
        if (size>255){
            throw new IllegalArgumentException("Size must be less than or equal to 255");
        }
        else{
            set= new Byte[size];
            for (int i = 0; i <size; i++) {
                set[i]=(byte) i;
            }
        }
    }

    public Set(Byte[] set){
        this.set=set;
    }

    public Set(byte[] set){
        //check set for completeness
        java.util.List<Byte> indices = new ArrayList<>();
        for (byte i=0;i<set.length;i++){
            indices.add(i);
        }
        for (int i=0;i<set.length;i++){
            if (indices.contains(set[i])){
                indices.remove((Byte) set[i]);
            }
        }
        if (!indices.isEmpty()){
            throw new IllegalArgumentException("Set is missing the following elements: "+indices);
        }
        this.set=new Byte[set.length];
        for (int i = 0; i < set.length; i++) {
            this.set[i]=set[i];
        }

    }

    /*************************************
     *********** Getter     ***************
     **************************************/

    public byte get(int i){
        return set[i];
    }

    /*************************************
     *********** Setter      ***************
     **************************************/


    /*************************************
     *********** Public Methods ***********
     **************************************/

    /**
     apply a cycle to a this base
     The cycle is interpreted as follows:
     the first number in the cycle states the position that is
     written and the following number in the cycle states the position that is inserted
     */
    public void applyCycle(Cycle cycle){
        Byte[] result = set.clone();

        int cycleLength = cycle.getCycleLength();
        for (int i = 0; i < cycleLength; i++) {
            int src = cycle.get(i);
            int target = cycle.get((i+1)%cycleLength);
            if (src<0 || target<0||src>=set.length||target>=set.length){
                throw new IllegalArgumentException("Cycle values "+this+" doesn't match with set "+set);
            }
            else
                result[src] = set[target];
        }
        this.set=result;
    }

    public void applyPermutation(Permutation permutation) {
        for (Cycle cycle : permutation.getCycles()) {
            this.applyCycle(cycle);
        }
    }
    /*************************************
     *********** Private Methods **********
     **************************************/

    /*************************************
     *********** Overrides      ***********
     **************************************/

    @Override
    public String toString() {
        return "["+ Arrays.stream(this.set).map(aByte -> Byte.toString(aByte)).collect(Collectors.joining(", "))+"]";
    }


}
