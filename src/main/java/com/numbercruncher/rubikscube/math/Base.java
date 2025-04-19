package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.utils.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The class Base is the natural structure that permutations are applied to
 * It is just a simple container of a byte array that has a string representation
 * The length of the base depends on the group.
 *
 * S_n always requires the full set \Omega as a base.
 * Subgroups usually require less elements.
 * No two different group elements have the same action on the base.
 * Only the unit element keeps the base unchanged.
 *
 * TODO: This can be made more general to store different entities than numbers
 * @author: NumberCruncher
 * Since: 30/12/2024
 * @version: 30/12/2024
 */
public class Base implements Comparable<Base>{

    /*************************************
     *********** Attributes ***************
     **************************************/
    private byte[] base;

    /*************************************
     *********** Constructor ***************
     **************************************/

    /**
     * Constructor for the Base class that initializes the instance with a given array of points.
     *
     * @param points the byte array representing the points to initialize the base
     */
    public Base(byte[] points){
        this.base = points;
    }

    public Base(List<Byte> points){
        this.base = new byte[points.size()];
        for (int i = 0; i < this.base.length; i++) {
            this.base[i]=points.get(i);
        }
    }

    public Base(int deg){
        this.base = new byte[deg];
        for (int i = 0; i < deg; i++) {
            this.base[i]=(byte) i;
        }
    }


    /*************************************
     *********** Getter     ***************
     **************************************/

    public byte[] getBase() {
        return base;
    }
    public int length() {
        return base.length;
    }

    /*************************************
     *********** Setter      ***************
     **************************************/

    /*************************************
     *********** Public Methods ***********
     **************************************/

    /**
     apply a cycle to this base. The action is copied from the mathematica function
     `PermutationReplace`

     The cycle is interpreted as follows:
     e.g.

     the cycle (1 3 7)
     changes the base b=[1,2,3,4,7,8] to [3,2,7,4,1,8]

     the action upon the base can be written as exponentiation $b^a$
     And it should be compatible with the multiplication operation of permutations

     i.e.
     two permutations a and b

     (b^a)^b = b^(a*b)

     The multiplication is defined as if the product a*b is applied to its set with arbitary elements (first b than a)
     The action on the base is defined as if the product a*b is applied to the base first a than b

     @ return: the image of the base under the action of the cycle is returned
     */
    public Base action(Cycle cycle){
        byte[] result = new byte[this.base.length];

        for (int i = 0; i < result.length; i++) {
            byte b = base[i];
            if (cycle.contains(b)) {
                int j = cycle.indexOf(b);
                result[i] = cycle.get((j+1)%cycle.size());
            } else {
                result[i] = b;
            }
        }
        return new Base(result);
    }

    /**
     * Applies the action defined by a permutation to the base.
     * The action is equivalent to applying all cycles of the given permutation in sequence.
     *  see documentation there
     *
     * @param permutation the permutation containing cycles to be applied to the base
     */
    public Base action(Permutation permutation){
            Base result = new Base(this.base);
            for (Cycle cycle : permutation.getCycles()) {
                result = result.action(cycle);
            }
            return result;
    }


    public static Base parse(String s){
        byte[] points = StringUtils.parseByteArray(s);
        return new Base(points);
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


    @Override
    public int hashCode() {
        return Arrays.hashCode(base); // Compute hash based on array contents
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Reference equality
        if (o == null || getClass() != o.getClass()) return false; // Null/different class
        Base oBase = (Base) o;
        return Arrays.equals(base, oBase.base); // Compare contents of the byte arrays
    }

    @Override
    public int compareTo(Base o) {
        if (this.base.length != o.base.length) return this.base.length-o.base.length;
        else{
            for (int i = 0; i < this.base.length; i++) {
                if (this.base[i]!=o.base[i]) return this.base[i]-o.base[i];
            }
            return 0;
        }
    }
}
