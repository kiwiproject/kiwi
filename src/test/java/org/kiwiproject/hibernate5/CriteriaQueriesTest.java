package org.kiwiproject.hibernate5;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.nth;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.kiwiproject.collect.KiwiLists.third;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;

@DisplayName("CriteriaQueries")
@SuppressWarnings({"java:S1874", "deprecation"})
class CriteriaQueriesTest {

    private Session session;
    private SessionImplementor sessionImplementor;

    @BeforeEach
    void setUp() {
        session = mock(Session.class);
        sessionImplementor = mock(SessionImplementor.class);
        when(session.createCriteria(TheEntity.class))
                .thenReturn(new CriteriaImpl(TheEntity.class.getName(), sessionImplementor));
    }

    @AfterEach
    void tearDown() {
        verifyNoInteractions(sessionImplementor);
    }

    @Nested
    class DistinctCriteriaForClass {

        @Test
        void shouldCreateDistinctCriteria() {
            var criteria = toCriteriaImpl(CriteriaQueries.distinctCriteria(session, TheEntity.class));

            assertHasDistinctRootEntity(criteria);
        }
    }

    @Nested
    class DistinctCriteriaWithOrder {

        @Test
        void shouldCreateCriteriaWithOrder() {
            var orderClause = " foo desc, bar, baz ";
            var criteria = toCriteriaImpl(
                    CriteriaQueries.distinctCriteriaWithOrder(session, TheEntity.class, orderClause));

            assertHasDistinctRootEntity(criteria);

            List<CriteriaImpl.OrderEntry> orderEntries = newArrayList(criteria.iterateOrderings());
            assertThat(orderEntries).hasSize(3);

            assertOrderEntry(first(orderEntries), "foo", false);
            assertOrderEntry(second(orderEntries), "bar", true);
            assertOrderEntry(third(orderEntries), "baz", true);
        }
    }

    @Nested
    class DistinctCriteriaWithFetchAssociations {
        @Test
        void testDistinctCriteria_WithPersistentClass_AndFetchAssociations() {
            String[] fetchAssociations = {"foos", "bars"};
            var criteria = toCriteriaImpl(
                    CriteriaQueries.distinctCriteriaWithFetchAssociations(session, TheEntity.class, fetchAssociations));

            assertHasDistinctRootEntity(criteria);

            assertThat(criteria.getFetchMode("this.foos")).isEqualTo(FetchMode.JOIN);
            assertThat(criteria.getFetchMode("this.bars")).isEqualTo(FetchMode.JOIN);
            assertThat(criteria.getFetchMode("this.bazes")).isNull();
        }
    }

    @Nested
    class DistinctCriteriaWithOrderAndFetchAssociations {

        @Test
        void shouldCreateDistinctCriteriaWithOrderAndOneFetchAssociation() {
            var orderClause = " foo, bar desc ";
            String[] fetchAssociations = {"foos"};
            var criteria = toCriteriaImpl(
                    CriteriaQueries.distinctCriteria(session, TheEntity.class, orderClause, fetchAssociations));

            assertHasDistinctRootEntity(criteria);

            List<CriteriaImpl.OrderEntry> orderEntries = newArrayList(criteria.iterateOrderings());
            assertThat(orderEntries).hasSize(2);
            assertOrderEntry(first(orderEntries), "foo", true);
            assertOrderEntry(second(orderEntries), "bar", false);
            assertThat(criteria.getFetchMode("this.foos")).isEqualTo(FetchMode.JOIN);
        }

        @Test
        void testDistinctCriteriaWithOrderAndMultipleFetchAssociations() {
            var orderClause = " foo, bar ";
            String[] fetchAssociations = {"foos", "bars"};
            CriteriaImpl criteria = toCriteriaImpl(
                    CriteriaQueries.distinctCriteria(session, TheEntity.class, orderClause, fetchAssociations));

            assertHasDistinctRootEntity(criteria);

            List<CriteriaImpl.OrderEntry> orderEntries = newArrayList(criteria.iterateOrderings());
            assertThat(orderEntries).hasSize(2);

            assertOrderEntry(first(orderEntries), "foo", true);
            assertOrderEntry(second(orderEntries), "bar", true);

            assertThat(criteria.getFetchMode("this.foos")).isEqualTo(FetchMode.JOIN);
            assertThat(criteria.getFetchMode("this.bars")).isEqualTo(FetchMode.JOIN);
        }
    }

    @Nested
    class AddOrder {

        @Test
        void shouldAddOrderEntries() {
            var criteria1 = toCriteriaImpl(session.createCriteria(TheEntity.class));

            var orderClause = "   foo  , bar      desc, baz ASC, corge ";
            var criteria2 = CriteriaQueries.addOrder(criteria1, orderClause);
            assertThat(criteria2).isSameAs(criteria1);

            List<CriteriaImpl.OrderEntry> orderEntries = newArrayList(criteria1.iterateOrderings());
            assertThat(orderEntries).hasSize(4);

            assertOrderEntry(first(orderEntries), "foo", true);
            assertOrderEntry(second(orderEntries), "bar", false);
            assertOrderEntry(third(orderEntries), "baz", true);
            assertOrderEntry(nth(orderEntries, 4), "corge", true);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldIgnore_EmptyOrderClause(String orderClause) {
            var criteria1 = toCriteriaImpl(session.createCriteria(TheEntity.class));

            var criteria2 = CriteriaQueries.addOrder(criteria1, orderClause);
            assertThat(criteria2).isSameAs(criteria1);

            List<CriteriaImpl.OrderEntry> orderEntries = newArrayList(criteria1.iterateOrderings());
            assertThat(orderEntries).isEmpty();
        }

        @Nested
        class ShouldThrowIllegalArgumentException {

            @Test
            void whenBadOrderInOrderClause() {
                var criteria = toCriteriaImpl(session.createCriteria(TheEntity.class));

                assertThatThrownBy(() -> CriteriaQueries.addOrder(criteria, "foo, bar desc, baz corge"))
                        .isExactlyInstanceOf(IllegalArgumentException.class)
                        .hasMessageEndingWith("Property order must be either asc or desc (case-insensitive)");
            }

            @Test
            void whenTooManyTokensInOrderClause() {
                CriteriaImpl criteria = toCriteriaImpl(session.createCriteria(TheEntity.class));
                assertThatThrownBy(() -> CriteriaQueries.addOrder(criteria, "foo, bar desc, baz asc klunk"))
                        .isExactlyInstanceOf(IllegalArgumentException.class)
                        .hasMessageStartingWith("'baz asc klunk' is not a valid order specification");
            }
        }

        /**
         * This is testing an internal (package-private) method.
         */
        @Nested
        class ToOrderFromPropertyOrderClause {

            @ParameterizedTest
            @CsvSource({
                    "name, name",
                    "age, age",
                    "name asc, name",
                    "name ASC, name",
                    "age ASC, age",
                    "age asc, age",
            })
            void shouldParseValidAscendingOrderClauses(String propertyOrdering, String expectedPropertyName) {
                var order = CriteriaQueries.toOrderFromPropertyOrderClause(propertyOrdering);

                assertThat(order.getPropertyName()).isEqualTo(expectedPropertyName);
                assertThat(order.isAscending()).isTrue();
            }

            @ParameterizedTest
            @CsvSource({
                    "name desc, name",
                    "name DESC, name",
                    "age desc, age",
                    "age DESC, age",
            })
            void shouldParseValidDescendingOrderClauses(String propertyOrdering, String expectedPropertyName) {
                var order = CriteriaQueries.toOrderFromPropertyOrderClause(propertyOrdering);

                assertThat(order.getPropertyName()).isEqualTo(expectedPropertyName);
                assertThat(order.isAscending()).isFalse();
            }

            @ParameterizedTest
            @CsvSource({
                    "name asc age desc",
                    "name asc desc",
                    "name asc bar",
                    "name foo bar",
            })
            void shouldThrowIllegalArgumentException_GivenPropertyOrdering_WithTooManySegments(String propertyOrdering) {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> CriteriaQueries.toOrderFromPropertyOrderClause(propertyOrdering))
                        .withMessage("'%s' is not a valid order specification. Must contain a property name optionally followed by (case-insensitive) asc or desc", propertyOrdering);
            }

            @ParameterizedTest
            @CsvSource({
                    "name a",
                    "name as",
                    "name ascending",
                    "name d",
                    "name des",
                    "name descending",
                    "name foo",
                    "name bar",
            })
            void shouldThrowIllegalArgumentException_GivenInvalidOrderDirection(String propertyOrdering) {
                var orderDirection = propertyOrdering.split(" ")[1];

                assertThatIllegalArgumentException()
                        .isThrownBy(() -> CriteriaQueries.toOrderFromPropertyOrderClause(propertyOrdering))
                        .withMessage("'%s' is not a valid order. Property order must be either asc or desc (case-insensitive)", orderDirection);
            }
        }
    }

    private static void assertOrderEntry(CriteriaImpl.OrderEntry orderEntry, String propertyName, boolean ascending) {
        assertThat(orderEntry.getOrder().getPropertyName()).isEqualTo(propertyName);
        assertThat(orderEntry.getOrder().isAscending()).isEqualTo(ascending);
    }

    private static void assertHasDistinctRootEntity(CriteriaImpl criteria) {
        assertThat(criteria.getResultTransformer()).isEqualTo(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    private static class TheEntity {
    }

    private static CriteriaImpl toCriteriaImpl(Criteria criteria) {
        assertThat(criteria)
                .describedAs("Expecting %s to be exactly instance of CriteriaImpl", criteria.getClass())
                .isExactlyInstanceOf(CriteriaImpl.class);

        return (CriteriaImpl) criteria;
    }
}
