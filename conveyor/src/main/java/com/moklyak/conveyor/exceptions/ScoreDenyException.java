package com.moklyak.conveyor.exceptions;

public class ScoreDenyException extends Exception{
    public ScoreDenyException(String message) {
        super(message);
    }

    public ScoreDenyException() {
    }

    public ScoreDenyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScoreDenyException(Throwable cause) {
        super(cause);
    }
}
