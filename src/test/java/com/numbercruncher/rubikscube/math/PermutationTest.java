package com.numbercruncher.rubikscube.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermutationTest {

@Test
void action(){
    Permutation a = Permutation.parse("(1 3 7 8)(9)");
    Permutation b = Permutation.parse("(9 2 4)(1 5 3)");

    Base base = Base.parse("[1,2,3,4,7,8]");
    base.action(a);
    assertEquals("[3, 2, 7, 4, 8, 1]", base.toString());
    base.action(b);
    assertEquals("[1, 4, 7, 9, 8, 5]", base.toString());
    base.action(a.multiply(b).inverse());
    assertEquals("[1, 2, 3, 4, 7, 8]", base.toString());
}
    @Test
    void inverse() {
        for (int i = 0; i < 20; i++) {
            Permutation p = Permutation.randomPermutation((int) (Math.random() * i));
            Permutation inverse = p.inverse();
            assertEquals(p.toString(), p.multiply(p.inverse().multiply(p)).toString());
        }
    }

    @Test
    void multiply() {

        Permutation p1 = Permutation.parse("(1 2 3 5)");
        Permutation p2 = Permutation.parse("(0 1 3 5)");
        Permutation p3 = Permutation.parse("(0 1 2 5 3)");

        assertEquals(p3.toString(), p1.multiply(p2).toString());

        Permutation q1 = Permutation.parse("(5)(0 4)(1 3)");
        Permutation q2 = Permutation.parse("(0 1)(2 5)(3 4)");
        Permutation q3 = Permutation.parse("(0 3)(1 4)(2 5)");

        assertEquals(q3.toString(), q1.multiply(q2).toString());

        Permutation r1 = Permutation.parse("(0 4)(1 2)(3 5)");
        Permutation r2 = Permutation.parse("(0 5 2 1 3 4)");
        Permutation r3 = Permutation.parse("(2 3)(4 5)");

        assertEquals(r3.toString(), r1.multiply(r2).toString());

        Permutation s1 = Permutation.parse("(1 5 2 3 4)");
        Permutation s2 = Permutation.parse("(0 5 3 2 4)");
        Permutation s3 = Permutation.parse("(0 5 4 1 3)");

        assertEquals(s3.toString(), s1.multiply(s2).toString());

        Permutation t1 = Permutation.parse("(0 3)(1 5)");
        Permutation t2 = Permutation.parse("(0 1 3 2 5 4)");
        Permutation t3 = Permutation.parse("(0 2 5 3 1 4)");

        assertEquals(t3.toString(), t1.multiply(t2).toString());

        Permutation u1 = Permutation.parse("(0 2 1 5 4)");
        Permutation u2 = Permutation.parse("(1 5 3 2)");
        Permutation u3 = Permutation.parse("(0 1 3 2 5 4)");

        assertEquals(u3.toString(), u1.multiply(u2).toString());

        Permutation v1 = Permutation.parse("(0 5 2 3)");
        Permutation v2 = Permutation.parse("(5)(0 4 2 1)");
        Permutation v3 = Permutation.parse("(0 5 1)(2 3 4)");

        assertEquals(v3.toString(), v1.multiply(v2).toString());

        Permutation w1 = Permutation.parse("(0 2 4 1 5 3)");
        Permutation w2 = Permutation.parse("(0 5 4)(1 2 3)");
        Permutation w3 = Permutation.parse("(0 3 5 1 4 2)");

        assertEquals(w3.toString(), w1.multiply(w2).toString());

        Permutation x1 = Permutation.parse("(5)(0 3)(2 4)");
        Permutation x2 = Permutation.parse("(0 5 3 2 4)");
        Permutation x3 = Permutation.parse("(0 2)(3 5)");

        assertEquals(x3.toString(), x1.multiply(x2).toString());

    }


    @Test
    void getSetImage() {
        for (int i = 0; i <20; i++) {
            Permutation p = Permutation.randomPermutation((int) (Math.random() * i));
            Set imageSet = p.getSetImage();
            Set set = new Set(p.getDegree());
            set.applyPermutation(p);
            assertEquals(imageSet.toString(), set.toString());
        }

    }


    @Test
    void testToString() {
        for (int i = 0; i < 20; i++) {
            Permutation p = Permutation.randomPermutation((int) (Math.random() * i));
            assertEquals(p.toString(),Permutation.parse(p.toString()).toString());
        }

    }

    @Test
    void isIdentity() {
        for (int i = 0; i < 20; i++) {
            Permutation p = Permutation.randomPermutation((int) (Math.random() * i));
            assertTrue(p.multiply(p.inverse()).isIdentity());
        }
    }
}