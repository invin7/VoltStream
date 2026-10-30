package grid.exceptions;

/** Unchecked: a programming error, requesting a bay type the factory does not know. */
public class UnknownBayTypeException extends RuntimeException {
    public UnknownBayTypeException(String type) {
        super("Unknown bay type: " + type);
    }
}
