package unical.enterpriceapplication.onlycards.application.exception;

public class UnsupportedMediaTypeException extends Exception {
    public UnsupportedMediaTypeException(String resource, String extensionAllowed) {
        super("Unsupported media type for " + resource + ". Only " + extensionAllowed + " are allowed");
    }
}
