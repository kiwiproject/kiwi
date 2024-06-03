package org.kiwiproject.spring.context;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.kiwiproject.spring.data.KiwiMongoConverters.addCustomConverters;
import static org.kiwiproject.spring.data.KiwiMongoConverters.newBsonUndefinedToNullObjectConverter;

import com.mongodb.WriteConcern;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * This class generates the context and factory necessary to programmatically initialize Spring Data
 * {@link org.springframework.data.mongodb.repository.MongoRepository} interfaces.
 * <p>
 * This also provides a default map of repositories. For simple basic usages, simply call {@link #getRepository} with
 * the appropriate type, and it will return (or create-and-return) the repository instance.
 */
@Slf4j
public class MongoRepositoryContext {

    /**
     * The {@link MongoTemplate} initialized by this context.
     */
    @Getter
    private final MongoTemplate mongoTemplate;

    /**
     * Direct access to the {@link MongoRepositoryFactory} in case it is needed for whatever reason.
     */
    @Getter
    private final MongoRepositoryFactory factory;

    /**
     * The Spring application context used by this instance, in case it is needed for whatever reason.
     */
    @Getter
    private final GenericApplicationContext springContext;

    private final ConcurrentMap<Class<?>, MongoRepository<?, ?>> repoMap = new ConcurrentHashMap<>();

    /**
     * Create a new instance using the given MongoDB URI.
     *
     * @param mongoUri the MongoDB connection string, e.g. {@code mongodb://my-mongo-server.test:27017}
     */
    public MongoRepositoryContext(String mongoUri) {
        this(initializeMongoTemplate(mongoUri));
    }

    /**
     * Create a new instance that will use the given {@link MongoTemplate}.
     *
     * @param mongoTemplate the MongoTemplate to use
     */
    public MongoRepositoryContext(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.factory = new MongoRepositoryFactory(mongoTemplate);
        this.springContext = new GenericApplicationContext();
        this.springContext.refresh();
        this.mongoTemplate.setApplicationContext(springContext);
    }

    /**
     * Get a MongoRepository having the given class.
     *
     * @param repositoryInterfaceClass the repository class
     * @param <T>                      the repository class
     * @return the singleton repository instance
     */
    @SuppressWarnings("unchecked")
    public final <T extends MongoRepository<?, ?>> T getRepository(Class<T> repositoryInterfaceClass) {
        return (T) repoMap.computeIfAbsent(
                repositoryInterfaceClass,
                missingClass -> (T) factory.getRepository(missingClass));
    }

    /**
     * Attach one or more listeners to the context.
     *
     * @param listeners the listeners to attach
     * @see org.springframework.context.support.AbstractApplicationContext#addApplicationListener(ApplicationListener)
     */
    public void attachListeners(ApplicationListener<?>... listeners) {
        Set<String> registeredListenerClassNames = springContext.getApplicationListeners()
                .stream()
                .map(listener -> listener.getClass().getName())
                .collect(toUnmodifiableSet());

        Stream.of(listeners).forEach(listener -> {
            var listenerClassName = listener.getClass().getName();
            LOG.debug("Adding application listener: {}", listenerClassName);

            if (registeredListenerClassNames.contains(listenerClassName)) {
                LOG.warn("There is already listener of type of {}; adding another one may cause unintended consequences",
                        listenerClassName);
            }

            springContext.addApplicationListener(listener);
        });
    }

    /**
     * Convenience method to initialize a new {@link MongoTemplate} from the given MongoDB connection string.
     * <p>
     * The returned instance is configured with write concern set to {@link WriteConcern#ACKNOWLEDGED}.
     * <p>
     * This method also registers
     * {@link org.kiwiproject.spring.data.KiwiMongoConverters.BsonUndefinedToNullStringConverter BsonUndefinedToNullStringConverter}
     * as a custom converter.
     *
     * @param mongoUri the MongoDB connection string, e.g. {@code mongodb://my-mongo-server.test:27017}
     * @return a new MongoTemplate instance
     */
    public static MongoTemplate initializeMongoTemplate(String mongoUri) {
        var mongoDbFactory = new SimpleMongoClientDatabaseFactory(mongoUri);

        var mongoTemplate = new MongoTemplate(mongoDbFactory);
        mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED);

        addCustomConverters(mongoTemplate, newBsonUndefinedToNullObjectConverter());

        return mongoTemplate;
    }
}
