package unical.enterpriceapplication.onlycards.application.exception;

public class LimitExceedException extends Exception{
    public LimitExceedException(String resource, int limit) {
        super("Limit exceeded for " + resource + ". Maximum allowed: " + limit);
    }
}
