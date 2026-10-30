package grid.exceptions;

/** Unchecked: the controller was configured with illegal values (capacity, time step). */
public class InvalidGridConfigurationException extends RuntimeException {
    public InvalidGridConfigurationException(String message) {
        super(message);
    }
}
