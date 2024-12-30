package com.numbercruncher.rubikscube.math;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.*;

/**
 *
 * The class Cycle
 *
 * We store the cycle data as byte values to keep the
 * storage amount small. This limits the maximal size to 127
 *
 * @author: NumberCruncher
 * Since: 30/12/2024
 * @version: 30/12/2024
 */
public class Cycle extends ArrayList<Byte> implements Comparable<Cycle>{
    /*************************************
     *********** Attributes ***************
     **************************************/

    /*************************************
     *********** Constructor ***************
     **************************************/
    public Cycle(){
        super();
    }

    public Cycle(Byte[] elements){
        super();
        this.addAll(asList(elements));
    }

    public Cycle(byte[] elements){
        super();
        this.addAll(
                IntStream.range(0,elements.length)
                        .mapToObj(i->elements[i])
                        .collect(Collectors.toList())
        );
    }

    /*************************************
     *********** Getter         ***********
     **************************************/

    public boolean isIdentity(){
        return this.size() < 2 ;
    }
    public int getCycleLength() {
        return this.size();
    }

    public Cycle copy(){
        Cycle copy = new Cycle();
        copy.addAll(this);
        return copy;
    }



    /*************************************
     *********** Public Methods ***********
     **************************************/

    public Cycle inverse(){
        if (this.size()<3) return this.copy();
        else{
            Cycle inv = new Cycle();
            inv.add(this.get(0));
            for (int i = this.size()-1; i >0; i--)
                inv.add(this.get(i));
            return inv;
        }
    }



    /*************************************
     *********** Static  Methods **********
     **************************************/
    public static Cycle parse(String s){
        s=s.strip();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("String is null or empty");
        }

        if (s.charAt(0) != '(' || s.charAt(s.length() - 1) != ')') {
            throw new IllegalArgumentException("String does not start with '(' and end with ')'");
        }
        StringTokenizer tokens = new StringTokenizer(s.substring(1,s.length()-1)," ");
        Cycle cycle =new Cycle();
        while(tokens.hasMoreTokens()){
            cycle.add( Byte.parseByte(tokens.nextToken()));
        }
        return cycle;
    }

    public static Cycle randomCycle(){
        return randomCycle(10,10);
    }

    public static Cycle randomCycle(int length){
        return randomCycle(length,length);
    }

    public static Cycle randomCycle(int range,int length){
        int n = (int) (Math.random()*length);

        byte[] values = new byte[n];

        for (int i = 0; i < values.length; i++) {
            values[i] = (byte) (Math.random()*range);
        }

        return new Cycle(values);

    }
    /*************************************
     *********** Overrides      ***********
     **************************************/

    @Override
    public String toString() {
        return "("+this.stream().map(Object::toString).collect(Collectors.joining(" "))+")";
    }


    @Override
    public int compareTo(Cycle o) {
        int one = this.stream().min(Byte::compareTo).orElse((byte)0);
        int two  =o.stream().min(Byte::compareTo).orElse((byte)0);
        return one-two;
    }
}
