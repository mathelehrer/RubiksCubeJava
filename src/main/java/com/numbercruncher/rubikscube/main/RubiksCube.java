package com.numbercruncher.rubikscube.main;

import com.numbercruncher.rubikscube.math.Permutation;
import com.numbercruncher.rubikscube.math.PermutationGroup;
import com.numbercruncher.rubikscube.utils.StringUtils;

public class RubiksCube {

    /*****************************
     **** Attribute **************
     *****************************/
    private PermutationGroup rubiksGroup;

    /*****************************
     **** Konstruktor*************
     *****************************/
    public RubiksCube() {
        Permutation t = Permutation.parse("(1 3 7 5)(2 6 8 4)(9 33 25 17)(10 34 26 18)(11 35 27 19)(48)");
        Permutation l = Permutation.parse("(1 17 41 39)(4 20 44 38)(5 21 45 35)(9 11 15 13)(10 14 16 12)(48)");
        Permutation f = Permutation.parse("(5 25 43 15)(7 29 41 11)(8 28 42 14)(17 19 23 21)(18 22 24 20)(48)");
        Permutation r = Permutation.parse("(3 37 43 19)(6 36 46 22)(7 33 47 23)(25 27 31 29)(26 30 32 28)(48)");
        Permutation b = Permutation.parse("(1 13 47 27)(2 12 48 30)(3 9 45 31)(33 35 39 37)(34 38 40 36)");
        Permutation d = Permutation.parse("(13 21 29 37)(15 23 31 39)(16 24 32 40)(41 43 47 45)(42 46 48 44)");

        //the order of the generators matters for the stabilizer chain
        //this way, it is almost possible to first stabilize all corners and then all edges
        String[] labels = {"T", "D", "L","R",  "F", "B"};
        rubiksGroup = new PermutationGroup("Rubik's Cube Group", labels, t,d, l, r, f,  b );

        System.out.println(rubiksGroup);

        for (String label : labels) {
            rubiksGroup.addWordRule(s->{
                String previous;
                do{
                    previous=s;
                    String tL = StringUtils.toggleCase(label);
                    s=s.replaceAll(label+label+label,tL)
                            .replaceAll(tL+tL+tL,label);
                }while(!s.equals(previous));
                return s;
            });
        }

        //prefer odd orbit elements (corners) to be stabilized first, when building the stabilizer chain
        rubiksGroup.addBasisSelectionRule(value->{return value%2!=0;});

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

    public static void main(String[] args) {
        RubiksCube group = new  RubiksCube();

        for (int i = 0; i < 10; i++) {
            System.out.println(group.rubiksGroup.randomElement(26));
        }
    }
}
