package org.kiwiproject.io;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Resources;
import lombok.experimental.UtilityClass;
import org.kiwiproject.net.UncheckedURISyntaxException;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Static utilities related to {@link Path} instances.
 */
@UtilityClass
public class KiwiPaths {

    public static Path pathFromResourceName(String resourceName) {
        try {
            return Paths.get(Resources.getResource(resourceName).toURI());
        } catch (URISyntaxException e) {
            throw newUncheckedException(resourceName, e);
        }
    }

    @VisibleForTesting
    static UncheckedURISyntaxException newUncheckedException(String resourceName, URISyntaxException e) {
        var message = "Cannot convert to URI: " + resourceName;
        return new UncheckedURISyntaxException(message, e);
    }
}
