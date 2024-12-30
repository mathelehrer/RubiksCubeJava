package com.numbercruncher.rubikscube.math;

import java.util.Arrays;
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
public class Base {

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
     apply a cycle to a this base. The action is copied from the mathematica function
     `PermutationReplace`

     The cycle is interpreted as follows:
     eg.

     the cycle (1 3 7)
     changes the base b=[1,2,3,4,7,8] to [3,2,7,4,1,8]

     the action upon the base can be written as exponentiation $b^a$
     And it should be compatible with the multiplication operation of permutations

     i.e.
     two permutations a and b

     (b^a)^b = b^(a*b)

     The multiplication is defined as if the product a*b is applied to its set with arbitary elements (first b than a)
     The action on the base is defined as if the product a*b is applied to the base first a than b


     */
    public void action(Cycle cycle){
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
        this.base=result;
    }

    /**
     * Applies the action defined by a permutation to the base.
     * The action is equivalent to applying all cycles of the given permutation in sequence.
     *  see documentation there
     *
     * @param permutation the permutation containing cycles to be applied to the base
     */
    public void action(Permutation permutation){
            for (Cycle cycle : permutation.getCycles()) {
                this.action(cycle);
            }
    }


    public static Base parse(String s){
        s=s.strip();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("String is null or empty");
        }

        if (s.charAt(0) != '[' || s.charAt(s.length() - 1) != ']') {
            throw new IllegalArgumentException("String does not start with '[' and end with ']'");
        }
        StringTokenizer tokens = new StringTokenizer(s.substring(1,s.length()-1),",");

        byte[] points = new byte[tokens.countTokens()];
        for (int i = 0; i < points.length; i++) {
            points[i] = Byte.parseByte(tokens.nextToken());
        }
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

}
