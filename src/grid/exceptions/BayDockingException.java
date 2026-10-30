package grid.exceptions;

/** Checked: a vehicle cannot be docked at a bay (null, already full, or bay occupied). */
public class BayDockingException extends Exception {
    public BayDockingException(String message) {
        super(message);
    }
}
