package bootiful.observability.mediaTest.exceptions;

/**
 * @author pari on 15/01/24
 */
public class StorageFileNotFoundException extends RuntimeException {
    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
