package org.kiwiproject.spring.data;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;

import com.google.common.collect.Streams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.search.KiwiSearching;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@DisplayName("KiwiPaging")
class KiwiPagingTest {

    @Nested
    class CreatePageableUsingPagingParamsArgument {

        private PagingRequest request;

        @BeforeEach
        void setUp() {
            request = new PagingRequest();
        }

        @Test
        void shouldCreatePageableFromValidPagingRequest() {
            request.setPage(3);
            request.setLimit(10);
            request.setPrimarySort("primary-field");
            request.setPrimaryDirection(Sort.Direction.ASC);
            request.setSecondarySort("secondary-field");
            request.setSecondaryDirection(Sort.Direction.DESC);

            var pageable = KiwiPaging.createPageable(request);

            assertThat(pageable.getPageNumber()).isEqualTo(3);
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getSort()).hasToString("primary-field: ASC,secondary-field: DESC");
        }

        @Test
        void shouldCreatePageable_WhenPagingRequestSortPropertiesAreNotPresent() {
            request.setPage(5);
            request.setLimit(25);

            // These should be null by default, but be explicit anyway
            request.setPrimarySort(null);
            request.setSecondarySort(null);

            var pageable = KiwiPaging.createPageable(request);

            assertThat(pageable.getPageNumber()).isEqualTo(5);
            assertThat(pageable.getPageSize()).isEqualTo(25);
        }

        @Test
        void shouldNotAllowNullArgument() {
            assertThatThrownBy(() -> KiwiPaging.createPageable(null))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("pagedRequest cannot be null");
        }

        @Test
        void shouldNotAllowNullPage() {
            request.setPage(null);
            request.setLimit(25);

            assertThatThrownBy(() -> KiwiPaging.createPageable(request))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("page cannot be null");
        }

        @Test
        void shouldNotAllowNullLimit() {
            request.setPage(0);
            request.setLimit(null);

            assertThatThrownBy(() -> KiwiPaging.createPageable(request))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("limit cannot be null");
        }
    }

    @Nested
    class CreatePageableUsingExplicitArguments {

        @Test
        void shouldCreatePageable_WithSorting() {
            var directionAndFieldNamePairs = createTestSortDirectionAndFieldPairArray();
            var pageable = KiwiPaging.createPageable(1, 10, directionAndFieldNamePairs);

            assertThat(pageable).isNotNull();
            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertSortChain(pageable.getSort());
        }

        @Test
        void shouldCreatePageable_WithoutSorting() {
            var pageable = KiwiPaging.createPageable(1, 10);

            assertThat(pageable.getPageSize()).isEqualTo(10);
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getSort()).isEqualTo(Sort.unsorted());
        }

        @Test
        void shouldCreatePageable_WhenPagingRequestSortDirectionAndPropertiesAreAllNull() {
            var pageable = KiwiPaging.createPageable(2, 15, null, null, null, null);

            assertThat(pageable.getPageNumber()).isEqualTo(2);
            assertThat(pageable.getPageSize()).isEqualTo(15);
            assertThat(pageable.getSort()).isEqualTo(Sort.unsorted());
        }

        @Test
        void shouldCreatePageable_WhenPagingRequestSortPropertiesAreAllNull() {
            var pageable = KiwiPaging.createPageable(2, 15, Sort.Direction.ASC, null, Sort.Direction.ASC, null);

            assertThat(pageable.getPageNumber()).isEqualTo(2);
            assertThat(pageable.getPageSize()).isEqualTo(15);
            assertThat(pageable.getSort()).isEqualTo(Sort.unsorted());
        }

        @ParameterizedTest
        @ValueSource(ints = {-10, -1})
        void shouldNotAllowNegativePageNumbers(int pageNumber) {
            assertThatThrownBy(() -> KiwiPaging.createPageable(pageNumber, 0))
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage("pageNumber cannot be negative");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0})
        void shouldNotAllowNegativeOrZeroLimit(int limit) {
            assertThatThrownBy(() -> KiwiPaging.createPageable(0, limit))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage(KiwiSearching.PAGE_SIZE_ERROR);
        }
    }

    @Nested
    class ConstructSortChainFromPairs {

        @Test
        void shouldConstructSortChain() {
            var directionAndFieldPairs = createTestSortDirectionAndFieldPairArray();
            var sortChain = KiwiPaging.constructSortChainFromPairs(directionAndFieldPairs);
            assertSortChain(sortChain);
        }

        @Test
        void shouldRequireEvenNumberOfVarargsToFormPairs() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() ->
                            KiwiPaging.constructSortChainFromPairs(Sort.Direction.ASC, "property1", Sort.Direction.DESC));
        }

        @Test
        void shouldIgnorePairsThatAreAllNull() {
            var sortChain = KiwiPaging.constructSortChainFromPairs(
                    null, null,
                    null, null
            );

            assertThat(sortChain).isNull();
        }

        @Test
        void shouldIgnorePairsThatContainNull() {
            var sortChain = KiwiPaging.constructSortChainFromPairs(
                    Sort.Direction.ASC, "property1",
                    Sort.Direction.ASC, null,  // should be ignored
                    Sort.Direction.DESC, "property2",
                    null, "ignoredProperty",  // should be ignored
                    Sort.Direction.ASC, "property3"
            );

            assertSortChain(sortChain);
        }

        @Test
        void shouldReturnNull_WhenEmptyVarargsGiven() {
            var sortChain = KiwiPaging.constructSortChainFromPairs();
            assertThat(sortChain).isNull();
        }
    }

    @Nested
    class ConstructSortListFromPairs {

        @Test
        void shouldConstructList() {
            var sorts = KiwiPaging.constructSortListFromPairs(
                    Sort.Direction.DESC, "lastName",
                    Sort.Direction.ASC, "firstName"
            );

            assertFirstLastNameSorting(sorts);
        }

        @Test
        void shouldIgnoreNullValues() {
            var sorts = KiwiPaging.constructSortListFromPairs(
                    Sort.Direction.ASC, null,
                    Sort.Direction.DESC, "lastName",
                    null, null,
                    null, "middleName",
                    Sort.Direction.ASC, "firstName"
            );

            assertFirstLastNameSorting(sorts);
        }

        private void assertFirstLastNameSorting(List<Sort> sorts) {
            assertThat(sorts).hasSize(2);

            var lastNameOrder = first(sorts).getOrderFor("lastName");
            assertThat(lastNameOrder).isNotNull();
            assertThat(lastNameOrder.getDirection()).isEqualTo(Sort.Direction.DESC);

            var firstNameOrder = second(sorts).getOrderFor("firstName");
            assertThat(firstNameOrder).isNotNull();
            assertThat(firstNameOrder.getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        void shouldReturnEmptyList_WhenGivenEmptyVarargs() {
            assertThat(KiwiPaging.constructSortListFromPairs()).isEmpty();
        }

        @Test
        void shouldRequireEvenNumberOfVarargs() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() ->
                            KiwiPaging.constructSortListFromPairs(Sort.Direction.ASC, "property1", Sort.Direction.DESC));
        }
    }

    @Nested
    class ConstructSortChainFromSortVarargs {

        @Test
        void shouldConstructSortChain() {
            var sortObjects = createTestSortList().toArray(new Sort[0]);
            var sortChain = KiwiPaging.constructSortChain(sortObjects);
            assertSortChain(sortChain);
        }

        @Test
        void shouldIgnoreNullValues() {
            var sortObjects = createTestSortListWithNulls().toArray(new Sort[0]);
            var sortChain = KiwiPaging.constructSortChain(sortObjects);
            assertSortChain(sortChain);
        }

        @Test
        void shouldReturnNull_WhenEmptyVarargsGiven() {
            var sortChain = KiwiPaging.constructSortChain();
            assertThat(sortChain).isNull();
        }
    }

    @Nested
    class ConstructSortChainFromList {

        @Test
        void shouldConstructSortChain() {
            var sorts = createTestSortList();
            var sortChain = KiwiPaging.constructSortChain(sorts);
            assertSortChain(sortChain);
        }

        @Test
        void shouldReturnTheOnlySortObject_WhenListContainsOneSort() {
            var sort = Sort.by(Sort.Order.asc("someProperty"));
            var sorts = List.of(sort);
            var sortChain = KiwiPaging.constructSortChain(sorts);
            assertThat(sortChain).isSameAs(sort);
        }

        @Test
        void shouldIgnoreNullSortObjects() {
            var sorts = createTestSortListWithNulls();
            var sortChain = KiwiPaging.constructSortChain(sorts);
            assertSortChain(sortChain);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnNull_WhenEmptyListGiven(List<Sort> sorts) {
            var sortChain = KiwiPaging.constructSortChain(sorts);
            assertThat(sortChain).isNull();
        }
    }

    private static List<Sort> createTestSortList() {
        var directionAndFieldPairs = createTestSortDirectionAndFieldPairArray();
        return KiwiPaging.constructSortListFromPairs(directionAndFieldPairs);
    }

    private static List<Sort> createTestSortListWithNulls() {
        var sorts = new ArrayList<>(createTestSortList());
        sorts.add(0, null);
        sorts.add(null);
        sorts.add(null);
        return sorts;
    }

    private static Object[] createTestSortDirectionAndFieldPairArray() {
        return createTestSortDirectionFieldPairList().toArray();
    }

    private static List<Object> createTestSortDirectionFieldPairList() {
        return List.of(
                Sort.Direction.ASC, "property1",
                Sort.Direction.DESC, "property2",
                Sort.Direction.ASC, "property3");
    }

    /**
     * Asserts that the given sort chain matches the sorts created by the various "create" methods above.
     */
    private static void assertSortChain(Sort sortChain) {
        assertThat(sortChain).isNotNull();

        //noinspection UnstableApiUsage
        var sortOrders = Streams.stream(sortChain.iterator()).collect(toList());

        assertThat(sortOrders)
                .extracting("property", "direction")
                .containsExactly(
                        tuple("property1", Sort.Direction.ASC),
                        tuple("property2", Sort.Direction.DESC),
                        tuple("property3", Sort.Direction.ASC)
                );
    }
}
