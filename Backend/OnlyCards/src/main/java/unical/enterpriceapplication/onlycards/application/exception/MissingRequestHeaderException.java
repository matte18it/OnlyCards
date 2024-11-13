package unical.enterpriceapplication.onlycards.application.exception;


public class MissingRequestHeaderException extends Exception {
    public MissingRequestHeaderException(String header) {
        super("Missing request header: " + header);
    }
}
