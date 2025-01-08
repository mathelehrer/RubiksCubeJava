package com.numbercruncher.rubikscube.utils;

import com.numbercruncher.rubikscube.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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

    public static String startTag(String tag){
        return "<"+tag+">\n";
    }

    public static String startTag(String tag, String attr){
        return "<"+tag+" "+attr+">\n";
    }

    public static String endTag(String tag){
        return "</"+tag+">\n";
    }

    public static String tabs(int n){
        StringBuilder out = new StringBuilder();
        for (int i=0;i<n;i++)
            out.append("\t");
        return out.toString();
    }

    public static byte[] parseByteArray(String data){
        data=data.strip();
        if (data.isEmpty()) {
            Logger.logging(Logger.Level.error,"String is null or empty");
            throw new IllegalArgumentException("String is null or empty");
        }

        if (data.charAt(0) != '[' || data.charAt(data.length() - 1) != ']') {
            Logger.logging(Logger.Level.error,"String does not start with '[' and end with ']'");
            throw new IllegalArgumentException("String does not start with '[' and end with ']'");
        }

        StringTokenizer tokens = new StringTokenizer(data.substring(1,data.length()-1),",");

        byte[] points = new byte[tokens.countTokens()];
        for (int i = 0; i < points.length; i++) {
            points[i] = Byte.parseByte(tokens.nextToken());
        }

        return points;
    }

    public static List<String> subWords(String word, int length){
        List<String> subWords = new ArrayList<>();
        for (int i = 0; i <= word.length() - length; i++) {
            subWords.add(word.substring(i, i + length));
        }
        return subWords;
    }
}
