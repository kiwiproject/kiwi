package org.kiwiproject.junit.jupiter;

import static org.kiwiproject.spring.util.MongoTestHelpers.mongoConnectionString;
import static org.kiwiproject.spring.util.MongoTestHelpers.startInMemoryMongoServer;

import de.bwaldvogel.mongo.MongoServer;
import lombok.Getter;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MongoServerExtension implements BeforeAllCallback, AfterAllCallback {

    @Getter
    private MongoServer mongoServer;

    @Override
    public void beforeAll(ExtensionContext context) {
        mongoServer = startInMemoryMongoServer();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        mongoServer.shutdownNow();
    }

    public String getConnectionString() {
        return mongoConnectionString(mongoServer);
    }
}
