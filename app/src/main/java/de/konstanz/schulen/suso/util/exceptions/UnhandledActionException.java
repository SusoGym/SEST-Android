package de.konstanz.schulen.suso.util.exceptions;

public class UnhandledActionException extends RuntimeException {


    public UnhandledActionException(String msg) {
        super("Unhandled Action: " + msg);
    }
}
