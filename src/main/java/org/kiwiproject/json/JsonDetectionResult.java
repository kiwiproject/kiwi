package org.kiwiproject.json;

import static java.util.Objects.nonNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 * Represents the result of attempting to detect if content is JSON. Provides enough information to determine whether
 * format detection
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class JsonDetectionResult {

    private final Boolean isJson;
    private final Exception error;

    /**
     * Whether format detection succeeded in determining a positive or negative result.
     *
     * @return true if the format detection produced a result, otherwise false
     */
    public boolean hasDetectionResult() {
        return nonNull(isJson);
    }

    /**
     * Whether format detection caught an exception during the detection process.
     *
     * @return true if there was an exception thrown during the detection process, otherwise false
     */
    public boolean hasError() {
        return nonNull(error);
    }

    /**
     * Is the content JSON? This should only be called if {@link #hasDetectionResult()} returns true (or if you
     * don't care whether an error occurred during detection).
     *
     * @return true only if detection succeeded and the format was detected as JSON, otherwise false
     */
    public boolean isJson() {
        if (hasDetectionResult()) {
            return isJson;
        }

        return false;  // not our fault caller didn't check; but don't blow up
    }

    /**
     * If an error occurred during the detection process, return it, otherwise return null
     *
     * @return the exception thrown during the detection process, or null if there was no error
     */
    @Nullable
    public Exception getErrorOrNull() {
        return error;
    }
}
