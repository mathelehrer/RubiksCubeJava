package com.numbercruncher.rubikscube.main;

import com.numbercruncher.rubikscube.math.Permutation;
import com.numbercruncher.rubikscube.math.PermutationGroup;
import com.numbercruncher.rubikscube.utils.StringUtils;

public class S5 {

    /*****************************
     **** Attribute **************
     *****************************/
    private PermutationGroup s5;

    /*****************************
     **** Konstruktor*************
     *****************************/
    public S5() {
        Permutation a = Permutation.parse("(0 1 2 3 4)");
        Permutation b = Permutation.parse("(3 4)");
       
        //the order of the generators matters for the stabilizer chain
        //this way, it is almost possible to first stabilize all corners and then all edges
        String[] labels = {"a","b"};
        s5 = new PermutationGroup("Symmetric group S5", labels, a,b);

        System.out.println(s5);

        //additional simplification rules for s5
        s5.addWordRule(s->{
            String previous;
            do{
                previous=s;
                s=s.replace("aaaa","A").replace("AAAA","a")
                        .replace("bb","").replace("BB","");
            }while(!s.equals(previous));
            return s;
        });

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
        S5 app = new S5();

        for (int i = 0; i < 10; i++) {
            System.out.println(app.s5.randomElement(26));
        }

        System.out.println("");
        System.out.println("");

        System.out.println(app.s5.getStabilizerChain());

    }


}
