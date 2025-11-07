package org.kiwiproject.spring.util;

import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiStrings.f;

import com.mongodb.client.MongoClients;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.mongodb.MongoDBContainer;

@UtilityClass
@Slf4j
public class MongoTestContainerHelpers {

    public static final int STANDARD_MONGODB_PORT = 27_017;

    public static final String ENV_MONGO_IMAGE_NAME = "MONGO_IMAGE_NAME";

    public static final String MONGO_LATEST_IMAGE_NAME = "mongo:latest";

    public static MongoDBContainer newMongoDBContainer() {
        var imageName = envMongoImageNameOrLatest();
        return newMongoDBContainer(imageName);
    }

    private static String envMongoImageNameOrLatest() {
        var envImageName = System.getenv(ENV_MONGO_IMAGE_NAME);
        return isNull(envImageName) ? MONGO_LATEST_IMAGE_NAME : envImageName;
    }

    @SuppressWarnings("resource")  // because Testcontainers closes it for us
    public static MongoDBContainer newMongoDBContainer(String dockerImageName) {
        LOG.info("Create MongoDBContainer for Docker image name: {}", dockerImageName);
        return new MongoDBContainer(DockerImageName.parse(dockerImageName))
                .waitingFor(new HostPortWaitStrategy());
    }

    public static MongoTemplate newMongoTemplate(MongoDBContainer container) {
        var mongoClient = MongoClients.create(connectionStringFor(container));
        return new MongoTemplate(mongoClient, "test");
    }

    public static String connectionStringFor(MongoDBContainer container) {
        var host = container.getHost();
        var port = container.getMappedPort(STANDARD_MONGODB_PORT);
        var connectionString = f("mongodb://{}:{}", host, port);
        logConnectionString(connectionString);
        return connectionString;
    }

    public static String connectionStringFor(MongoDBContainer container, String databaseName) {
        var host = container.getHost();
        var port = container.getMappedPort(STANDARD_MONGODB_PORT);
        var connectionString = f("mongodb://{}:{}/{}", host, port, databaseName);
        logConnectionString(connectionString);
        return connectionString;
    }

    private static void logConnectionString(String connectionString) {
        LOG.info("Mongo connection string: {}", connectionString);
    }
}
