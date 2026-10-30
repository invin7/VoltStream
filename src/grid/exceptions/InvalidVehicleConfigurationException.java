package grid.exceptions;

/** Checked: user-supplied vehicle data (type, plate, charge level) is invalid. */
public class InvalidVehicleConfigurationException extends Exception {
    public InvalidVehicleConfigurationException(String message) {
        super(message);
    }
}
