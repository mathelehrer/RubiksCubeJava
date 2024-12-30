package com.numbercruncher.rubikscube.math;

import static org.junit.jupiter.api.Assertions.*;

class CycleTest {

    private Cycle cycle;
    private Cycle empty;
    private Cycle one;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        cycle=Cycle.randomCycle();
        empty=Cycle.randomCycle(0);
        one=new Cycle(new byte[]{0});
    }

    @org.junit.jupiter.api.Test
    void isIdentity() {
        assertTrue(one.isIdentity());
        assertTrue(empty.isIdentity());
        if (cycle.getCycleLength()<2){
            assertTrue(cycle.isIdentity());
        }
        else{
            assertFalse(cycle.isIdentity());
        }
    }

    @org.junit.jupiter.api.Test
    void copy() {
        Cycle copy = cycle.copy();
        assertEquals(cycle, copy);
    }

    @org.junit.jupiter.api.Test
    void inverse() {
        Cycle inverse = cycle.inverse();
        assertEquals(cycle, inverse.inverse());
    }

    @org.junit.jupiter.api.Test
    void parse() {
        Cycle parsedCycle = Cycle.parse(cycle.toString());
        assertEquals(cycle, parsedCycle);
    }

    @org.junit.jupiter.api.Test
    void testToString() {
        assertEquals(cycle.toString(), Cycle.parse(cycle.toString()).toString()
        );
    }
}