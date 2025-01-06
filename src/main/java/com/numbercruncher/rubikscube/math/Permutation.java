package com.numbercruncher.rubikscube.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The class Permutation can be represented differently
 *
 * 1.) as an array that shows the re-arrangement from the default case
 * 2.) as a List of Cycles
 * 3.) as an action on a base
 *
 * Author: NumberCruncher
 * Since: 30/12/2024
 * Version: 1.0
 */
public class Permutation implements Comparable<Permutation> {
    /*************************************
     *********** Attributes ***************
     **************************************/

    private final List<Cycle> cycles;
    private Set setImage;
    private final int degree;

    /*************************************
     *********** Constructor ***************
     **************************************/

    /**
     * create a permutation form the set image
     *
     *
     * @param seq: a set image that shows how the set is transformed
     */
    public Permutation(byte[] seq){
        this.setImage = new Set(seq);
        this.degree = seq.length;
        this.cycles=new ArrayList<>();
        createCycles();
    }

    public Permutation(Cycle... cycles){
        this.cycles= Arrays.asList(cycles);
        this.degree = this.cycles.stream().flatMap(List::stream).max(Byte::compareTo).orElse((byte)0)+1;
    }

    public Permutation(List<Cycle> cycles){
        this.cycles=cycles;
        this.degree = this.cycles.stream().flatMap(List::stream).max(Byte::compareTo).orElse((byte)0)+1;
    }

    /*************************************
     *********** Getter     ***************
     **************************************/

    /**
     * The set image is only calculated on demand to save storage
     *
     * @return
     */
    public Set getSetImage() {
        if (this.setImage == null)
        {
            Set set = new Set(this.degree);
            for (Cycle cycle : cycles) {
                set.applyCycle(cycle);
            }
            this.setImage = set;
        }
        return this.setImage;
    }

    public int getDegree() {
        return degree;
    }

    public List<Cycle> getCycles() {
        return cycles;
    }

    /**
     * Computes the sign of the permutation based on its cycle structure.
     * The sign is determined by the parity of the permutation:
     * - If the permutation can be expressed as an even number of transpositions, the sign is +1.
     * - If the permutation can be expressed as an odd number of transpositions, the sign is -1.
     *
     * This is calculated by summing (cycle size - 1) for each cycle in the permutation and checking the parity of the total.
     *
     * @return an integer representing the sign of the permutation. Returns 1 for even permutations and -1 for odd permutations.
     */
    public int sign(){
        int parity = 0;
        for (Cycle cycle:cycles) parity+=cycle.size()-1;
        if (parity%2==0) return 1;else return -1;
    }

    public boolean isIdentity(){
        return 1>=this.cycles.stream().mapToInt(Cycle::getCycleLength).max().orElse(0);
    }

    /*************************************
     *********** Setter      ***************
     **************************************/

    /*************************************
     *********** Public Methods ***********
     **************************************/

    /**
     * Multiplies the current permutation by another permutation.
     * The multiplication combines the transformations of both permutations
     * and results in a new permutation representing their composition.
     *
     * @param factor the permutation to multiply with the current permutation. Must be of the same degree as the current permutation.
     * @return a new Permutation object that represents the product of the current permutation and the given permutation.
     * @throws IllegalArgumentException if the degree of the given permutation does not match the degree of the current permutation.
     */
    public Permutation multiply(Permutation factor) {
        if (factor != null) {

            if (this.degree == factor.degree) {

                Set setLeft = this.getSetImage();
                Set setRight = factor.getSetImage();

                byte[] productSeq = new byte[degree];
                for (int i = 0; i < degree; i++)
                    productSeq[i] = setRight.get((byte) setLeft.get(i));
                return new Permutation(productSeq);
            }
            else{
                throw new IllegalArgumentException("Dimensions of the permutations are not equal");
            }
        }

        return null;
    }


    /**
     * Computes the inverse of the current permutation.
     * The inverse of a permutation is obtained by inverting
     * each of its cycles.
     *
     * @return a new Permutation object representing the inverse of the current permutation.
     */
    public Permutation inverse() {

        List<Cycle> cycles = this.cycles;
        List<Cycle> newCycles = new ArrayList<>();
        cycles.forEach(x->newCycles.add(x.inverse()));

        return new Permutation(newCycles);
    }

    /**
     * Creates and returns a deep copy of the current permutation.
     *
     * The copied permutation has an independent set of cycles, ensuring that
     * modifications to the original permutation do not affect the copy, and vice versa.
     *
     * @return a new Permutation object that is a deep copy of the current permutation.
     */
    public Permutation copy() {
        return new Permutation(this.cycles.stream().map(Cycle::copy).collect(java.util.stream.Collectors.toList()));
    }

    public byte action(byte point) {
        byte image=point;
        for (Cycle cycle : cycles) {
            if (cycle.contains(image)) {
                int j = cycle.indexOf(image);
                image = cycle.get((j+1)%cycle.size());
            }
        }
        return image;
    }

    /*************************************
     *********** Private Methods **********
     **************************************/

    /**
     * Constructs the cycles representation of the permutation.
     *
     * This method identifies and creates all cycles (both trivial and non-trivial)
     * from the permutation's set image. A trivial cycle is a cycle of length 1 where
     * an element maps to itself. Non-trivial cycles involve elements that are cyclically
     * permuted. The identified cycles are added to the internal `cycles` list of the
     * `Permutation` object.
     *
     * Trivial cycles:
     * - For all elements in the domain of the permutation, if an element maps to itself,
     *   it is considered a trivial cycle.
     * - The last trivial cycle (if any) is explicitly added as a single-element cycle,
     *  such that the degree of the permutation can be concluded from the cycle structure.
     *
     * Non-trivial cycles:
     * - Iteratively identifies chains of transformations where elements do not trivially
     *   map to themselves.
     * - Cycles are terminated when an element maps back to the starting index of the chain.
     */
    private void createCycles(){
        boolean[] idx = new boolean[degree];

        //trivial cycles
        for (int i = 0; i < this.degree; i++) {
            if(this.setImage.get(i)==i){
                idx[i] =true;
                if (i==this.degree-1){
                    //add largest trivial cycle
                    Cycle cycle = new Cycle();
                    cycle.add((byte) i);
                    this.cycles.add(cycle);
                }
            }
        }

        //non-trivial cycles
        for (int i = 0; i < this.degree; i++) {
            if(!idx[i]){
                Cycle cycle = new Cycle();

                //insert first position into cycle
                cycle.add((byte)i);
                idx[i]=true;

                int j = i;
                while (this.setImage.get(j)!=i){
                    cycle.add((byte) setImage.get(j));
                    j= setImage.get(j);
                    idx[j]=true;
                }

                this.cycles.add(cycle);
            }
        }

    }
    /*************************************
     *********** Overrides      ***********
     **************************************/

    @Override
    public String toString() {
        this.cycles.sort(Cycle::compareTo);
        return this.cycles.stream().
                map(Object::toString).
                collect(java.util.stream.Collectors.joining(""));
    }


    /*************************************
     *********** Static Methods  ***********
     **************************************/

    /**
     * Parses a string representation of a permutation into a Permutation object.
     * The input string should consist of cycles, where each cycle is enclosed between parentheses
     * and contains numbers separated by spaces. Cycles that are adjacent should be separated by ")(".
     *
     * @param s the string representation of the permutation to parse
     * @return a Permutation object constructed from the given String
     * @throws IllegalArgumentException if the String is null, empty, or improperly formatted
     */
    public static Permutation parse(String s){
        s=s.strip();
        s=s.replace(")(",")#(");
        String[] parts = s.split("#");
        List<Cycle> cycles = new ArrayList<>();
        for (String part : parts) {
            cycles.add(Cycle.parse(part));
        }
        return new Permutation(cycles);
    }

    /**
     * Generates a random permutation of a given degree.
     *
     * The method creates a random permutation by forming random cycles
     * of elements from the set {0, 1, ..., degree-1}. It ensures at least one cycle
     * includes the highest element in the set. Cycles of length 1 are discarded.
     *
     * @param degree the size of the set to generate a permutation for. Must be at least 1.
     *               If a value less than 1 is provided, it defaults to 1.
     *
     * @return a Permutation object representing a random permutation of the given degree.
     */
    public static Permutation randomPermutation(int degree){
        if (degree<1) degree =1;
        List<Cycle> cycles = new ArrayList<>();
        Byte[] allValues = new Byte[degree];
        for (int i = 0; i < degree; i++) {
            allValues[i]=(byte) i;
        }
        List<Byte> values = new ArrayList<>();

        for (Byte allValue : allValues) {
            if (Math.random() > 0.3) values.add(allValue);
        }

        while(!values.isEmpty()){
            int n = (int) (Math.random()*values.size());
            Cycle cycle  = new Cycle();

            for (int i = 0; i < n+1; i++) {
                cycle.add(values.get(i));
            }
            values=values.subList(n+1,values.size());
            cycles.add(cycle);
        }

        //remove all cycles of length 1
        cycles.removeIf(c->c.size()==1);

        //check whether the highest position shows up in one of the cycles
        boolean found = false;
        final int max = degree-1;
        for (Cycle cycle : cycles) {
            if (cycle.stream().anyMatch(v->v==max)) {found=true;break;}
        }
        if (!found) {
            Cycle cycle = new Cycle();
            cycle.add((byte) (degree-1));
            cycles.add(cycle);
        }

        return new Permutation(cycles);
    }

    @Override
    public int compareTo(Permutation o) {
        if (this.cycles.size() != o.cycles.size())
            return this.cycles.size() - o.cycles.size();
        for (int i = 0; i < this.cycles.size(); i++) {
            int diff = this.cycles.get(i).compareTo(o.cycles.get(i));
            if (diff != 0) return diff;
        }
        return 0;
    }
}
