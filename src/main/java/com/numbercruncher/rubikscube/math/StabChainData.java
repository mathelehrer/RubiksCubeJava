package com.numbercruncher.rubikscube.math;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StabChainData implements Comparable<StabChainData> {


    /*****************************
     **** Attribute **************
     *****************************/
    private List<Byte> stabs;
    /*****************************
     **** Constructor *************
     *****************************/

    public StabChainData(StabilizerChain stabilizerChain) {
        stabs = new ArrayList<>();
        while(!stabilizerChain.isLast()){
            stabs.add(stabilizerChain.getOrbit().get(0));
            stabilizerChain = stabilizerChain.getStabilizer();
        }
    }

/*****************************
 **** Getter    **************
 *****************************/

/*****************************
 **** Setter    **************
 *****************************/

/*****************************
 **** public methods *********
 *****************************/


/*****************************
 **** private methods  *******
 *****************************/

    /*****************************
     **** overrides     **********
     *****************************/
    @Override
    public int compareTo(StabChainData o) {
        for (int i = 0; i < stabs.size(); i++) {
            if (stabs.get(i).compareTo(o.stabs.get(i)) != 0) {
                return stabs.get(i).compareTo(o.stabs.get(i));}
        }
        return 0;
    }

    @Override
    public String toString() {
        return stabs.stream().map(x->x.toString()).collect(Collectors.joining("->"));
    }

}
