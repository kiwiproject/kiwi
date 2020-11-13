package org.kiwiproject.mongo;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;

import java.net.URI;

/**
 * Static utilities relating to Mongo databases.
 */
@UtilityClass
@Beta
public class KiwiMongoDbs {

    /**
     * Extract the database name component of the given Mongo connection URI.
     * <p>
     * For example, {@code order_processing} is the database name for the URI
     * {@code mongodb://database-1.test/order_processing}.
     *
     * @param mongoUri the Mongo connection URI, assumed to contain the database name
     * @return the database name
     * @throws IllegalArgumentException if the given Mongo URI does not contain a database
     */
    public static String extractDbName(String mongoUri) {
        checkArgumentNotBlank(mongoUri, "mongoUri cannot be blank");

        return extractDbName(URI.create(mongoUri));
    }

    /**
     * Extract the database name component of the given Mongo connection URI.
     * <p>
     * For example, {@code order_processing} is the database name for the URI
     * {@code mongodb://database-1.test/order_processing}.
     *
     * @param uri the Mongo connection URI, assumed to contain the database name
     * @return the database name
     * @throws IllegalArgumentException if the given Mongo URI does not contain a database
     */
    public static String extractDbName(URI uri) {
        checkArgumentNotNull(uri, "uri cannot be null");

        var pathOrNull = uri.getPath();
        if (isBlank(pathOrNull) || "/".equals(pathOrNull)) {
            throw new IllegalArgumentException("Mongo connection URI does not contain a database name");
        }

        return pathOrNull.substring(1);
    }

    /**
     * Extract the scheme and host information of the given Mongo connection URI.
     * <p>
     * For example, {@code mongodb://mongo-db-1.test:27019/} is the host component for the URI
     * {@code mongodb://mongo-db-1.test:27019/test_database}.
     *
     * @param mongoUri the Mongo connection URI
     * @return the host information in the format {@code mongodb://extracted-host-info/}
     */
    public static String extractHostInformation(String mongoUri) {
        checkArgumentNotBlank(mongoUri, "mongoUri cannot be blank");

        return extractHostInformation(URI.create(mongoUri));
    }

    /**
     * Extract the scheme and host information of the given Mongo connection URI.
     *
     * @param uri the Mongo connection URI
     * @return the host information in the format {@code mongodb://extracted-host-info/}
     */
    public static String extractHostInformation(URI uri) {
        checkArgumentNotNull(uri, "uri cannot be null");

        return f("{}://{}/", uri.getScheme(), uri.getAuthority());
    }
}
