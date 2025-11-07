package org.kiwiproject.spring.context;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.kiwiproject.spring.util.MongoTestContainerHelpers.connectionStringFor;
import static org.kiwiproject.spring.util.MongoTestContainerHelpers.newMongoDBContainer;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.LoggingEventListener;
import org.springframework.data.mongodb.core.mapping.event.MongoMappingEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@DisplayName("MongoRepositoryContext")
@Testcontainers(disabledWithoutDocker = true)
class MongoRepositoryContextTest {

    @Container
    static final MongoDBContainer MONGODB = newMongoDBContainer();

    private MongoRepositoryContext mongoRepositoryContext;

    @BeforeEach
    void setUp() {
        var connectionString = connectionStringFor(MONGODB, "test");
        mongoRepositoryContext = new MongoRepositoryContext(connectionString);
    }

    @Nested
    class Construction {

        @Test
        void shouldInitializeMongoTemplate() {
            assertThat(mongoRepositoryContext.getMongoTemplate()).isNotNull();
        }

        @Test
        void shouldInitializeMongoRepositoryFactory() {
            assertThat(mongoRepositoryContext.getFactory()).isNotNull();
        }

        @Test
        void shouldInitializeSpringContext() {
            assertThat(mongoRepositoryContext.getSpringContext()).isNotNull();
        }
    }

    @Nested
    class GetRepository {

        @ParameterizedTest
        @ValueSource(classes = {SampleQuoteDocRepository.class, SamplePersonDocRepository.class})
        void shouldGetRepositories(Class<MongoRepository<?, ?>> repositoryClass) {
            var repository = mongoRepositoryContext.getRepository(repositoryClass);

            assertThat(repository).isNotNull();

            assertThatCode(repository::findAll).doesNotThrowAnyException();
        }

        @Test
        void shouldReturnSameInstanceOnceInitialized() {
            var repository1 = mongoRepositoryContext.getRepository(SampleQuoteDocRepository.class);
            var repository2 = mongoRepositoryContext.getRepository(SampleQuoteDocRepository.class);

            assertThat(repository1).isSameAs(repository2);
        }

        @Test
        void shouldBeAbleToUseRepository() {
            var quoteDocRepository = mongoRepositoryContext.getRepository(SampleQuoteDocRepository.class);

            quoteDocRepository.insert(new SampleQuoteDoc(
                    "Blaise Pascal",
                    "Justice without force is powerless; force without justice is tyrannical"));

            quoteDocRepository.insert(new SampleQuoteDoc(
                    "Adam Smith",
                    "Science is the great antidote to the poison of enthusiasm and superstition."));

            var people = quoteDocRepository.findAll()
                    .stream()
                    .map(SampleQuoteDoc::getName)
                    .collect(toUnmodifiableSet());

            assertThat(people).containsExactlyInAnyOrder("Adam Smith", "Blaise Pascal");
        }
    }

    @Nested
    class AttachListeners {

        @Test
        void shouldAttachListeners() {
            var afterSaveListener = new MyAfterSaveEventListener();
            var afterLoadListener = new MyAfterLoadEventListener();

            mongoRepositoryContext.attachListeners(new LoggingEventListener(), afterSaveListener, afterLoadListener);

            var personDocRepository = mongoRepositoryContext.getRepository(SamplePersonDocRepository.class);

            var alice = personDocRepository.insert(new SamplePersonDoc("Alice", 42));
            var bob = personDocRepository.insert(new SamplePersonDoc("Bob", 49));

            var aliceId = alice.getId();
            var bobId = bob.getId();

            personDocRepository.findById(bobId).orElseThrow();
            personDocRepository.findById(aliceId).orElseThrow();
            personDocRepository.findById(bobId).orElseThrow();

            assertThat(afterSaveListener.savedIds).containsExactly(aliceId, bobId);
            assertThat(afterLoadListener.loadedIds).containsExactly(bobId, aliceId, bobId);
        }

        @Test
        void shouldAllowMultipleListenersOfSameType() {
            var afterLoadListener1 = new MyAfterLoadEventListener();
            var afterLoadListener2 = new MyAfterLoadEventListener();

            mongoRepositoryContext.attachListeners(afterLoadListener1);

            // This second call triggers the warning (must inspect logs to see the WARN message)
            mongoRepositoryContext.attachListeners(afterLoadListener2);

            var personDocRepository = mongoRepositoryContext.getRepository(SamplePersonDocRepository.class);

            var alice = personDocRepository.insert(new SamplePersonDoc("Carlos", 45));

            var aliceId = alice.getId();
            personDocRepository.findById(aliceId).orElseThrow();

            // Both listeners should be triggered
            assertThat(afterLoadListener1.loadedIds).containsExactly(aliceId);
            assertThat(afterLoadListener2.loadedIds).containsExactly(aliceId);
        }
    }

    @SuppressWarnings("NullableProblems")
    private static class MyAfterLoadEventListener extends AbstractMongoEventListener<Object> {

        final List<Object> loadedIds = new ArrayList<>();

        @Override
        public void onAfterLoad(AfterLoadEvent<Object> event) {
            getId(event).ifPresent(loadedIds::add);
        }
    }

    @SuppressWarnings("NullableProblems")
    private static class MyAfterSaveEventListener extends AbstractMongoEventListener<Object> {

        final List<Object> savedIds = new ArrayList<>();

        @Override
        public void onAfterSave(AfterSaveEvent<Object> event) {
            getId(event).ifPresent(savedIds::add);
        }
    }

    private static Optional<String> getId(MongoMappingEvent<?> event) {
        return getId(event.getDocument());
    }

    private static Optional<String> getId(Document document) {
        return Optional.ofNullable(document)
                .map(doc -> doc.get("_id"))
                .map(Object::toString);
    }

    @Data
    @NoArgsConstructor
    @RequiredArgsConstructor
    private static class SampleQuoteDoc {

        @Id
        private String id;

        @NonNull
        private String name;

        @NonNull
        private String text;
    }

    private interface SampleQuoteDocRepository extends MongoRepository<SampleQuoteDoc, String> {
    }

    @Data
    @NoArgsConstructor
    @RequiredArgsConstructor
    private static class SamplePersonDoc {
        @Id
        private String id;

        @NonNull
        private String name;

        @NonNull
        private Integer age;
    }

    private interface SamplePersonDocRepository extends MongoRepository<SamplePersonDoc, String> {
    }
}
