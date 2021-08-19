package org.kiwiproject.mongo;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;

import com.google.common.annotations.Beta;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;

import java.io.Closeable;

/**
 * A wrapper around a {@link MongoClient} that also provides {@link MongoDatabase} instance
 * for a specific database, which is specified in the constructor.
 *
 * @deprecated with no replacement since we no longer support the older Mongo 3.x versions and our code must
 * therefore use only the new Mongo APIs (i.e. {@code com.mongodb.DB} doesn't exist in the Mongo 4.x drivers).
 */
@Beta
@Deprecated(forRemoval = true, since = "0.26.0")
public class MongoClientWrapper implements Closeable {

    /**
     * The wrapped {@link MongoClient}.
     */
    @Getter
    private final MongoClient mongoClient;

    /**
     * A {@link MongoDatabase} associated with this instance (as specified in the constructor).
     */
    @Getter
    private final MongoDatabase mongoDatabase;

    /**
     * Create a new instance with the given Mongo connection URI. Uses {@code databaseName} to get
     * a {@link MongoDatabase} instance, which is available from this instance via the getter method.
     *
     * @param mongoUri     the Mongo connection URI
     * @param databaseName the database associated with this instance
     */
    public MongoClientWrapper(String mongoUri, String databaseName) {
        checkArgumentNotBlank(mongoUri, "mongoUri cannot be blank");
        checkArgumentNotBlank(databaseName, "databaseName cannot be blank");

        this.mongoClient = MongoClients.create(mongoUri);
        mongoDatabase = this.mongoClient.getDatabase(databaseName);
    }

    /**
     * Closes the wrapper {@link MongoClient}.
     */
    @Override
    public void close() {
        mongoClient.close();
    }
}
