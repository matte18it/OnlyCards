package unical.enterpriceapplication.onlycards.application.exception;

public class MissingParametersException extends Exception {
    public MissingParametersException(String cardId) {
        super("Missing parameters: " + cardId);

    }
}
