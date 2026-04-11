package exception;

/**
 * Checked exception thrown when a trace file contains a malformed
 * or out-of-range memory address.
 *
 * Being a checked exception forces callers (e.g. TraceLoader) to
 * explicitly handle or propagate it — appropriate because bad trace
 * data is an expected, recoverable error in normal operation.
 */
public class InvalidAddressException extends Exception {

    private final String badAddress;
    private final int lineNumber;

    /**
     * Simple constructor with just an error message.
     *
     * @param message Description of the error
     */
    public InvalidAddressException(String message) {
        super(message);
        this.badAddress = null;
        this.lineNumber = -1;
    }

    /**
     * Detailed constructor that records the offending address string
     * and the line number in the trace file where it was found.
     *
     * @param message    Description of the error
     * @param badAddress The malformed address string from the trace file
     * @param lineNumber The 1-based line number in the trace file
     */
    public InvalidAddressException(String message, String badAddress, int lineNumber) {
        super(message);
        this.badAddress = badAddress;
        this.lineNumber = lineNumber;
    }

    /**
     * @return The malformed address string, or null if not provided
     */
    public String getBadAddress() {
        return badAddress;
    }

    /**
     * @return The line number in the trace file (1-based), or -1 if not provided
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Provides a detailed error message including context about where
     * the problem occurred in the trace file.
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (badAddress != null) {
            sb.append(" | Address: '").append(badAddress).append("'");
        }
        if (lineNumber > 0) {
            sb.append(" | Line: ").append(lineNumber);
        }
        return sb.toString();
    }
}
