package org.kiwiproject.hibernate5;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiStrings.splitWithTrimAndOmitEmpty;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;

import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.UtilityClass;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;

import java.util.List;

/**
 * Utility class for creating Hibernate {@link Criteria} queries.
 *
 * @implNote Suppressing all IntelliJ and Sonar deprecation warnings. We are aware that the Hibernate Criteria API
 * is deprecated.
 */
@UtilityClass
@SuppressWarnings({"java:S1874", "deprecation"})
public class CriteriaQueries {

    private static final String ASC = "asc";
    private static final String DESC = "desc";
    private static final int ORDER_SPEC_SIZE_WITH_ORDER = 2;
    private static final int ORDER_SPEC_SIZE_WITHOUT_ORDER = 1;
    private static final String INVALID_ORDER_SPEC_MESSAGE_TEMPLATE =
            "'%s' is not a valid order specification. Must contain a property" +
                    " name optionally followed by (case-insensitive) asc or desc";

    /**
     * Creates a {@link Criteria} query for the specified persistent class, with the specified ordering,
     * and setting {@link FetchMode#JOIN} on the specified {@code fetchAssociations}.
     *
     * @param session           the Hibernate session
     * @param persistentClass   the class for which to create a criteria  query
     * @param orderClause       the order specification
     * @param fetchAssociations one or more associations to fetch via a join
     * @return a {@code Criteria} which you can build upon
     * @see #addOrder(Criteria, String)
     * @see #distinctCriteriaWithFetchAssociations(org.hibernate.Session, Class, String...)
     */
    public static Criteria distinctCriteria(Session session,
                                            Class<?> persistentClass,
                                            String orderClause,
                                            String... fetchAssociations) {

        var criteria = distinctCriteriaWithOrder(session, persistentClass, orderClause);
        addFetchAssociations(criteria, fetchAssociations);
        return criteria;
    }

    /**
     * Creates a {@link Criteria} query for the specified persistent class and the specified HQL order clause.
     *
     * @param session         the Hibernate session
     * @param persistentClass the class for which to create a criteria query
     * @param orderClause     the order specification
     * @return a {code Criteria} which you can build upon
     * @see #addOrder(Criteria, String)
     */
    public static Criteria distinctCriteriaWithOrder(Session session, Class<?> persistentClass, String orderClause) {
        var criteria = distinctCriteria(session, persistentClass);
        return addOrder(criteria, orderClause);
    }

    /**
     * Creates a {@link Criteria} query for the specified persistent class and setting
     * {@link FetchMode#JOIN} on the specified {@code fetchAssociations}.
     *
     * @param session           the Hibernate session
     * @param persistentClass   the class for which to create a criteria query
     * @param fetchAssociations one or more associations to fetch via a join
     * @return a {@code Criteria} which you can build upon
     */
    public static Criteria distinctCriteriaWithFetchAssociations(Session session,
                                                                 Class<?> persistentClass,
                                                                 String... fetchAssociations) {

        var criteria = distinctCriteria(session, persistentClass);
        addFetchAssociations(criteria, fetchAssociations);
        return criteria;
    }

    /**
     * Creates a {@link Criteria} query for the specified persistent class.
     *
     * @param session         the Hibernate session
     * @param persistentClass the class for which to create a criteria query
     * @return a {@code Criteria} which you can build upon
     */
    public static Criteria distinctCriteria(Session session, Class<?> persistentClass) {
        return session.createCriteria(persistentClass)
                .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    private static void addFetchAssociations(Criteria criteria, String... fetchAssociations) {
        for (var association : fetchAssociations) {
            criteria.setFetchMode(association, FetchMode.JOIN);
        }
    }

    /**
     * Adds the specified order clause to an existing {@link Criteria}, returning the criteria that was passed in.
     * <p>
     * The {@code orderClause} should contain a comma-separated list of properties optionally followed by an order
     * designator, which must be {@code asc} or {@code desc}. If neither ascending nor descending is specified,
     * ascending order is used. For example, the order clause for listing people by descending date of birth, then
     * ascending last name, and finally ascending first name is:
     * <pre>
     * dateOfBirth desc, lastName, firstName
     * </pre>
     *
     * @param criteria    an existing {@code Criteria} to add ordering to
     * @param orderClause the order specification
     * @return the <em>same</em> {@code criteria} instance passed in, to allow building upon it
     */
    public static Criteria addOrder(Criteria criteria, String orderClause) {
        if (isBlank(orderClause)) {
            return criteria;
        }

        splitToList(orderClause, ',')
                .stream()
                .map(CriteriaQueries::toOrderFromPropertyOrderClause)
                .forEach(criteria::addOrder);

        return criteria;
    }

    @VisibleForTesting
    static Order toOrderFromPropertyOrderClause(String propertyOrdering) {
        var orderSpec = splitToList(propertyOrdering, ' ');
        validateOrderSpecification(orderSpec, propertyOrdering);

        var propertyName = first(orderSpec);
        var isAscending = orderSpec.size() < ORDER_SPEC_SIZE_WITH_ORDER || ASC.equalsIgnoreCase(second(orderSpec));

        return isAscending ? Order.asc(propertyName) : Order.desc(propertyName);
    }

    private static void validateOrderSpecification(List<String> orderSpec, String rawPropertyOrder) {
        checkArgument(
                orderSpec.size() == ORDER_SPEC_SIZE_WITHOUT_ORDER || orderSpec.size() == ORDER_SPEC_SIZE_WITH_ORDER,
                INVALID_ORDER_SPEC_MESSAGE_TEMPLATE, rawPropertyOrder);

        if (orderSpec.size() == ORDER_SPEC_SIZE_WITH_ORDER) {
            var order = second(orderSpec);
            checkArgument(ASC.equalsIgnoreCase(order) || DESC.equalsIgnoreCase(order),
                    "'%s' is not a valid order. Property order must be either %s or %s (case-insensitive)",
                    order, ASC, DESC);
        }
    }

    private static List<String> splitToList(String value, char separator) {
        return newArrayList(splitWithTrimAndOmitEmpty(value, separator));
    }

}
