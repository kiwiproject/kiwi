package org.kiwiproject.mongo;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;

import com.google.common.annotations.Beta;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.kiwiproject.base.KiwiDeprecated;

import java.io.Closeable;

/**
 * A wrapper around a {@link MongoClient} that also provides {@link MongoDatabase} and {@link DB} instances
 * for a specific database, which is specified in the constructor.
 *
 * @deprecated with no replacement since we no longer support the older Mongo 3.x driver versions and our code must
 * therefore use only the new Mongo APIs (i.e. {@code com.mongodb.DB} doesn't exist in the Mongo 4.x drivers).
 */
@Beta
@Deprecated(forRemoval = true, since = "1.2.1")
@KiwiDeprecated(since = "1.2.1", removeAt = "2.0.0", reference = "https://github.com/kiwiproject/kiwi/issues/658")
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
