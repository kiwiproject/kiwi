package org.kiwiproject.mongo;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;

import com.google.common.annotations.Beta;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;

import java.io.Closeable;

/**
 * A wrapper around a {@link MongoClient} that also provides {@link MongoDatabase} and {@link DB} instances
 * for a specific database, which is specified in the constructor.
 */
@Beta
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

    private final DB db;

    /**
     * Create a new instance with the given Mongo connection URI. Uses {@code databaseName} to get both
     * {@link MongoDatabase} and {@link DB} instances, which are available from this instance via their
     * getter methods.
     *
     * @param mongoUri     the Mongo connection URI
     * @param databaseName the database associated with this instance
     */
    @SuppressWarnings("deprecation")
    public MongoClientWrapper(String mongoUri, String databaseName) {
        checkArgumentNotBlank(mongoUri, "mongoUri cannot be blank");
        checkArgumentNotBlank(databaseName, "databaseName cannot be blank");

        mongoClient = new MongoClient(new MongoClientURI(mongoUri));
        mongoDatabase = mongoClient.getDatabase(databaseName);
        db = mongoClient.getDB(databaseName);
    }

    /**
     * Return the (deprecated) {@link DB} instance for clients that have not yet migrated to {@link MongoDatabase}.
     *
     * @return the DB instance associated with this instance (as specified in the constructor)
     */
    public DB getDB() {
        return db;
    }

    /**
     * Closes the wrapper {@link MongoClient}.
     */
    @Override
    public void close() {
        mongoClient.close();
    }
}
