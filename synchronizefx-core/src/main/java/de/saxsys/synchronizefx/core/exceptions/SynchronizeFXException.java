package de.saxsys.synchronizefx.core.exceptions;

/**
 * A exception for any kind of failures that appear in this framework.
 * 
 * @author raik.bieniek
 * 
 */
public class SynchronizeFXException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param e The exception that caused this failure.
     */
    public SynchronizeFXException(final Throwable e) {
        super(e);
    }

    /**
     * 
     * @param message A user readable message that describes the problem and optionally some advice for the user how the
     *            problem can be fixed.
     * @param e The exception that caused this failure.
     */
    public SynchronizeFXException(final String message, final Throwable e) {
        super(message, e);
    }

    /**
     * 
     * @param message A user readable message that describes the problem and optionally some advice for the user how the
     *            problem can be fixed.
     */
    public SynchronizeFXException(final String message) {
        super(message);
    }
}
