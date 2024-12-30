package com.numbercruncher.rubikscube.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SetTest {
    private Set omega;
    private Cycle cycle;
    @BeforeEach
    void setUp() {
        omega=new Set(10);
        cycle = new Cycle(new byte[]{2,3,4,5});

    }

    @Test
    void applyCycle() {
        omega.applyCycle(cycle);
        assertEquals("[0, 1, 3, 4, 5, 2, 6, 7, 8, 9]",omega.toString());
    }
}