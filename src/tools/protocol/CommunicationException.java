package tools.protocol;

import tools.ExceptionHandler;

public class CommunicationException extends Exception{

    public int level;
    public String errorMsg;
    public String expectedValue;
    public ExceptionHandler.OccClass occurrence;
    private Exception exception;

    public CommunicationException(String s){
        super(s);
        errorMsg = s;
    }

    public CommunicationException(String errorMsg, String expectedValue){
        super(errorMsg);
        this.errorMsg = errorMsg;
        this.expectedValue = expectedValue;
    }

    public CommunicationException(String errorMsg, String expectedValue, ExceptionHandler.OccClass occClass){
        super(errorMsg);
        this.errorMsg = errorMsg;
        this.expectedValue = expectedValue;
        this.occurrence = occClass;
    }

    public CommunicationException(Exception e){
        this.exception = e;
    }
}
