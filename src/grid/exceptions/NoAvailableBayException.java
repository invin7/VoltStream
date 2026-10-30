package grid.exceptions;

/** Checked: every charging bay is occupied, so a vehicle cannot be dispatched. */
public class NoAvailableBayException extends Exception {
    public NoAvailableBayException() {
        super("No charging bays are currently available.");
    }
}
