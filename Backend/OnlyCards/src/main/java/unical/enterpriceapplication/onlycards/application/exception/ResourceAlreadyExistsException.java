package unical.enterpriceapplication.onlycards.application.exception;


public class ResourceAlreadyExistsException extends Exception {
    public ResourceAlreadyExistsException( String name, String resource) {
        super(resource+" with name " + name + " already exists");
    }
}
