package com.numbercruncher.rubikscube.math;

import com.numbercruncher.rubikscube.utils.StringUtils;

import java.util.List;
import java.util.function.Function;

/**
 * The class GroupElement
 *
 * @author NumberCruncher
 * Since 12/30/24
 * @version 12/30/24
 */

public class GroupElement {

    /*****************************
     **** Attributes **************
     *****************************/
    private Permutation permutation;
    private String word;

    /*****************************
     **** Constructor *************
     *****************************/

    public GroupElement(Permutation permutation, String word){
        this.permutation=permutation;
        if (this.permutation.isIdentity())
            this.word="";
        else
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

    /*****************************
     **** Setter    **************
     *****************************/

    /*****************************
     **** public methods *********
     *****************************/

    public GroupElement multiply(GroupElement factor){
        return new GroupElement(permutation.multiply(factor.permutation),word+factor.word);
    }

    public GroupElement inverse(){
        return new GroupElement(permutation.inverse(), StringUtils.toggleCase(new StringBuilder(word).reverse().toString()));
    }

    public void wordSimplify(List<Function<String,String>> rules){
        for (Function<String,String> rule : rules) {
            word = rule.apply(word);
        }
    }
    /*****************************
     **** private methods  *******
     *****************************/

    /*****************************
     **** Overrides     **********
     *****************************/

    public String toString(){
        return word+": "+permutation;
    }

    /*****************************
     **** static methods **********
     *****************************/
}
