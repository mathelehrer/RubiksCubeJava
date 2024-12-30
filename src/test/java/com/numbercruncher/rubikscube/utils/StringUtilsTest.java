package com.numbercruncher.rubikscube.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void toggleCase() {
        assertEquals("ABC", StringUtils.toggleCase("abc"));
        assertEquals("hELLO wORLD", StringUtils.toggleCase("Hello World"));
    }
}