package nl.thedutchruben.playtime.core.storage.exceptions;

/**
 * Thrown when a storage type is not found
 */
public class StorageTypeNotFoundException extends RuntimeException {

    /**
     * Create a new StorageTypeNotFoundException
     * @param message The message to display
     */
    public StorageTypeNotFoundException(String message) {
        super(message);
    }
}