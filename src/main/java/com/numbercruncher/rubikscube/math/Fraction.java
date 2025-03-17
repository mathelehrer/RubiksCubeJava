package com.numbercruncher.rubikscube.math;

/**
 * The Fraction class represents a mathematical fraction, composed of a numerator and a denominator.
 * It provides functionality for fraction manipulation, such as arithmetic operations,
 * reduction to lowest terms, and comparison between fractions.
 * This class allows the representation of rational numbers in fraction form.
 */
public class Fraction {
    private int numerator;
    private int denominator;

    public Fraction(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public Fraction add(Fraction f) {
        return new Fraction(numerator * f.denominator + f.numerator * denominator,
                denominator * f.denominator);
    }

    public Fraction multiply(Fraction f) {
        return new Fraction(numerator * f.numerator,
                denominator * f.denominator);
    }
}
