package com.alexco.simplevertxservice;

public class TestUtils {
    public static Throwable createThrowable(String msg) {
        Throwable throwable;
        try {
            throw new RuntimeException(msg);
        } catch (RuntimeException e) {
            throwable = e;
        }
        return throwable;
    }
}
