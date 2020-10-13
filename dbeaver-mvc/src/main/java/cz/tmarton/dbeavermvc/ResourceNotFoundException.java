package cz.tmarton.dbeavermvc;

public class ResourceNotFoundException extends IllegalStateException {

    public ResourceNotFoundException(String s) {
        super(s);
    }
}
