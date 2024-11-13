package unical.enterpriceapplication.onlycards.application.exception;

public class ResourceNotFoundException extends Exception{
    public ResourceNotFoundException(String id, String resource) {
        super(resource+" "+ id + " not found");
    }
}
