package exception;

/**
 * Unchecked exception thrown when a cache configuration parameter
 * is invalid (e.g. non-power-of-2 sizes, negative values, or
 * malformed JSON structure in the config file).
 *
 * Being unchecked (RuntimeException) signals that configuration errors
 * are programming mistakes or environment issues — not something the
 * normal control flow should recover from at runtime.
 */
public class CacheConfigException extends RuntimeException {

    private final String paramName;
    private final Object paramValue;

    /**
     * Simple constructor with just an error message.
     *
     * @param message Description of the configuration error
     */
    public CacheConfigException(String message) {
        super(message);
        this.paramName = null;
        this.paramValue = null;
    }

    /**
     * Detailed constructor that records which configuration parameter
     * caused the error and what its invalid value was.
     *
     * @param message    Description of the configuration error
     * @param paramName  The name of the invalid config parameter (e.g. "cacheSize")
     * @param paramValue The invalid value that was provided
     */
    public CacheConfigException(String message, String paramName, Object paramValue) {
        super(message);
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    /**
     * @return The name of the invalid parameter, or null if not provided
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * @return The invalid value, or null if not provided
     */
    public Object getParamValue() {
        return paramValue;
    }

    /**
     * Provides a detailed error message including the parameter name
     * and value that caused the configuration error.
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (paramName != null) {
            sb.append(" | Parameter: '").append(paramName).append("'");
        }
        if (paramValue != null) {
            sb.append(" | Value: ").append(paramValue);
        }
        return sb.toString();
    }
}
