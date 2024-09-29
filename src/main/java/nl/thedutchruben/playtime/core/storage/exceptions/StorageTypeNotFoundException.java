package nl.thedutchruben.playtime.core.storage.exceptions;

public class StorageTypeNotFoundException extends RuntimeException {
    public StorageTypeNotFoundException(String message) {
        super(message);
    }
}