package cz.tmarton.dbeaver;

public class ResourceNotFoundException extends IllegalStateException {

    public ResourceNotFoundException(String s) {
        super(s);
    }
}
