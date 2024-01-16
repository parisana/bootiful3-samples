package bootiful.observability.mediaTest.exceptions;

/**
 * @author pari on 15/01/24
 */
public class StorageException extends RuntimeException{
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
