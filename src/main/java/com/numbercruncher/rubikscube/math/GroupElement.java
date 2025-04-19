package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The class GroupElement
 *
 * @author NumberCruncher
 * Since 12/30/24
 * @version 12/30/24
 */

public class GroupElement implements Comparable<GroupElement> {

    /*****************************
     **** Attributes **************
     *****************************/
    private final Permutation permutation;
    private String word;
    private List<String> factors = new ArrayList<>();


    /*****************************
     **** Constructor *************
     *****************************/

    public GroupElement(Permutation permutation, String word){
        this.permutation=permutation;
//        if (this.permutation.isIdentity())
//            this.word="";
//        else
            this.word=word;
    }

    /*****************************
     **** Getter    **************
     *****************************/

    public Permutation getPermutation() {
        return permutation;
    }

    public String getWord() {
        return word;
    }

    public List<String> getFactors(){
        return factors;
    }

    /*****************************
     **** Setter    **************
     *****************************/

    public void addFactor(String factor){
        this.factors.add(factor);
    }

    public void setFactors(List<String> factors){
        this.factors=factors;
    }

    /*****************************
     **** public methods *********
     *****************************/

    public GroupElement multiply(GroupElement factor){
        return new GroupElement(permutation.multiply(factor.permutation),word+factor.word);
    }

    public GroupElement multiply(GroupElement factor,TreeMap<String,String> simplifyingRules){
        GroupElement product =  new GroupElement(permutation.multiply(factor.permutation),word+factor.word);
        product.apply(simplifyingRules);
        return product;
    }

    public GroupElement inverse(){
        return new GroupElement(permutation.inverse(), StringUtils.toggleCase(new StringBuilder(word).reverse().toString()));
    }

    public void wordSimplify(List<Function<String,String>> rules){
        int oldLength;
        int newLength;
        do {
            oldLength = word.length();
            for (Function<String, String> rule : rules) {
                word = rule.apply(word);
            }
            newLength = word.length();
        } while (newLength < oldLength);
    }


    public void apply(TreeMap<String,String> rules){
        this.apply(rules,false,1);
    }

    public void apply(TreeMap<String,String> rules,boolean verbose){
        this.apply(rules,verbose,1);
    }

    public void apply(TreeMap<String, String> rules,boolean verbose,int depth) {
        String finalWord = this.getWord();
        final Predicate<String> selector = s->s.length()<= finalWord.length();
        TreeMap<String, String> subRules = rules.entrySet().stream().filter(v->selector.test(v.getKey())).collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,(v1, v2)->v1,TreeMap::new));

        int oldLength = word.length();
        for (Map.Entry<String,String> entry : subRules.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (verbose && this.word.contains(key)) {
                System.out.print(this.word+"->");
                this.word = this.word.replace(key, value);
                System.out.println(this.word);

            }
            else this.word = this.word.replace(key, value);
        }
        int newLength = word.length();

        if (newLength < oldLength) {
            if (depth%100==0){
                //System.out.println(finalWord+"->"+word+" at level "+depth);
                System.out.println("simplification at level "+depth);
            }
            apply(rules, verbose, depth+1);
        }
    }
    /*****************************
     **** private methods  *******
     *****************************/

    /*****************************
     **** Overrides     **********
     *****************************/

    /**
     * Provides a string representation of the object. If the length of the word exceeds 5 characters,
     * the result will be a shortened form using the first and last characters of the word, separated by "...".
     * Otherwise, the full word is returned.
     *
     * @return a string representation of the object, either shortened or the full word.
     */
    public String toString(){
        if (word.length()>5)
            return word.charAt(0)+"..."+word.substring(word.length()-1);
        return word;
    }

    /**
     * Returns the full word representation of the group element.
     *
     * @return the full word as a string.
     */
    public String toFullWordString(){
        return word;
    }

    /**
     * Provides a detailed string representation of the group element,
     * including both the word and its associated permutation formatted as "word->permutation".
     *
     * @return a string combining the word and permutation in the format "word->permutation".
     */
    public String toFullString(){
        return word+"->"+permutation.toString();
    }

    public String toTabString(int tabs){
        int wordtabs = word.length()/4;
        return word+StringUtils.tabs(tabs-wordtabs)+"->"+permutation.toString();
    }

    @Override
    public int compareTo(GroupElement o) {
         return this.permutation.compareTo(o.permutation);
    }


    /*****************************
     **** static methods **********
     *****************************/
}
