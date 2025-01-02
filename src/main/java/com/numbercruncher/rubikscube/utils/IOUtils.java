package com.numbercruncher.rubikscube.utils;

import java.net.URL;

/**
 * The class IOUtils
 *
 * @author NumberCruncher
 * Since 1/2/25
 * @version 1/2/25
 */

public class IOUtils {


    /*****************************
     **** static methods **********
     *****************************/

    public static URL getResourcePath(String subDir){
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader.getResource(subDir);
    }
}
