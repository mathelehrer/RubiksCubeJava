package com.numbercruncher.rubikscube.math;

import java.util.Arrays;

/**
 * The class Base is the natural structure that permutations are applied to
 * It is just a simple container of a byte array that has a string representation
 *
 * TODO: This can be made more general to store different entities than numbers
 * @author: NumberCruncher
 * Since: 30/12/2024
 * @version: 30/12/2024
 */
public class Base {

    /*************************************
     *********** Attributes ***************
     **************************************/
    private byte[] base;

    /*************************************
     *********** Constructor ***************
     **************************************/
    public Base(int size){
        if (size>255){
            throw new IllegalArgumentException("Size must be less than or equal to 255");
        }
        else{
            base = new byte[size];
            for (int i = 0; i <size; i++) {
                base[i]=(byte) i;
            }
        }
    }

    /*************************************
     *********** Getter     ***************
     **************************************/

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
        byte[] result = base.clone();

        int cycleLength = cycle.getCycleLength();
        for (int i = 0; i < cycleLength; i++) {
            int src = cycle.get(i);
            int target = cycle.get((i+1)%cycleLength);
            if (src<0 || target<0||src>=base.length||target>=base.length){
                throw new IllegalArgumentException("Cycle values "+this+" doesn't match with base "+base);
            }
            else
                result[src] = base[target];
        }
        this.base=result;
    }

    /*************************************
     *********** Private Methods **********
     **************************************/

    /*************************************
     *********** Overrides      ***********
     **************************************/

    @Override
    public String toString() {
        return Arrays.toString(base);
    }

}
