package com.numbercruncher.rubikscube.logger;



public class Logger {

    public static enum Level {info,warning,debug,error}
    private static boolean isDebugEnabled = false;


    private Class callingClass;
    public Logger(Class callingClass){
        this.callingClass = callingClass;
    }

    public Logger(Object object){
        this.callingClass = object.getClass();
    }

    public static void logging(Level level,String message){
        switch(level){
            case info:
                System.out.println("info: " + message);
                break;
            case warning:
                System.out.println("warning: " + message);
                break;
            case debug:
                if (isDebugEnabled) System.out.println("debug: " + message);
                break;
            case error:
                System.err.println("error: "+message);
                System.exit(0);
                break;
        }

    }

    public static void logging(Level level,String message,Class caller){
        String name = caller.getName();
        switch(level){
            case info:
                System.out.println("info: " + message+" in "+name);
                break;
            case warning:
                System.out.println("warning: " + message+" in "+name);
                break;
            case debug:
                System.out.println("debug: " + message+" in "+name);
                break;
            case error:
                System.err.println("error: "+message+" in "+name);
                System.exit(0);
                break;
        }

    }

    public static void logging(Level level,String message, Object caller) {
        logging(level, message, caller.getClass());
    }

    /**
     * for compatability
     * @param info
     */
    public static void info(String info){
        System.out.println(info);
    }

    public void log(Level level,String message){
        this.logging(level,message);
    }

    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

}

