package fn10.psptools.psp.reader;

public class ReaderException extends RuntimeException {
    public ReaderException(String message, Exception cause) {
        super(message, cause);
    }

    public ReaderException(String message) {
        super(message);
    }
}
