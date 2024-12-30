package com.numbercruncher.rubikscube.utils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The class StringUtils
 *
 * @author NumberCruncher
 * Since 12/30/24
 * @version 12/30/24
 */

public class StringUtils {

    /*****************************
     **** static methods **********
     *****************************/

    public static String toggleCase(String s){
        List<Character> chars = s.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        chars.replaceAll(c -> Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c));
        return chars.stream().map(Object::toString).collect(Collectors.joining());
    }
}
