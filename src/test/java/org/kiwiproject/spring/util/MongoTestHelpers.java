package org.kiwiproject.spring.util;

import static org.kiwiproject.base.KiwiStrings.f;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import lombok.experimental.UtilityClass;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@UtilityClass
public class MongoTestHelpers {

    public static MongoServer startInMemoryMongoServer() {
        var mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind();
        return mongoServer;
    }

    public static String mongoConnectionString(MongoServer mongoServer) {
        var addr = mongoServer.getLocalAddress();
        return f("mongodb://{}:{}/test_db_{}", addr.getHostName(), addr.getPort(), System.currentTimeMillis());
    }

    public static MongoTemplate newMongoTemplate(MongoServer mongoServer) {
        var connectionString = MongoTestHelpers.mongoConnectionString(mongoServer);
        var factory = new SimpleMongoClientDatabaseFactory(connectionString);
        return new MongoTemplate(factory);
    }
}
