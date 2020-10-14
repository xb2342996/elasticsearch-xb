package com.xxbb.springframework.data.elasticsearch.core.query;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;

public class Criteria {
    public static final String CRITERIA_VALUE_SEPARATOR = " ";
    public static final String CRITERIA_VALUE_SEPERATOR = CRITERIA_VALUE_SEPARATOR;

    private @Nullable Field field;
    private float boost = Float.NaN;
    private boolean negating = false;

    private final CriteriaChain criteriaChain = new CriteriaChain();
    private final Set<CriteriaEntry> queryCriteriaEntries = new LinkedHashSet<>();
    private final Set<CriteriaEntry> filterCriteriaEntries = new LinkedHashSet<>();
    private final Set<Criteria> subCriteria = new LinkedHashSet<>();

    public static Criteria and() {
        return new Criteria();
    }

    public static Criteria or() {
        return new OrCriteria();
    }

    public Criteria() {}

    public Criteria(String fieldName) {
        this(new SimpleField(fieldName));
    }

    public Criteria(Field field) {
        Assert.notNull(field, "Field for criteria must not be null");
        Assert.hasText(field.getName(), "Field.name for criteria must not be empty or null");
        this.field = field;
        this.criteriaChain.add(this);
    }

    protected Criteria(List<Criteria> criteriaChain, String fieldName) {
        this(criteriaChain, new SimpleField(fieldName));
    }

    protected Criteria(List<Criteria> criteriaChain, Field field) {
        Assert.notNull(criteriaChain, "CriteriaChain must not be null");
        Assert.notNull(field, "field must not be null");
        Assert.hasText(field.getName(), "field.name must not be empty or null");

        this.field = field;
        this.criteriaChain.addAll(criteriaChain);
        this.criteriaChain.add(this);
    }

    public static Criteria where(String fieldName) {
        return new Criteria(fieldName);
    }

    public static Criteria where(Field field) {
        return new Criteria(field);
    }

    @Nullable
    public Field getField() {
        return field;
    }

    public Set<CriteriaEntry> getQueryCriteriaEntries() {
        return Collections.unmodifiableSet(this.queryCriteriaEntries);
    }

    public Set<CriteriaEntry> getFilterCriteriaEntries() {
        return Collections.unmodifiableSet(this.filterCriteriaEntries);
    }

    public Operator getOperator() {
        return Operator.AND;
    }

    public List<Criteria> getCriteriaChain() {
        return Collections.unmodifiableList(this.criteriaChain);
    }

    public Criteria not() {
        this.negating = true;
        return this;
    }

    public boolean isNegating() {
        return this.negating;
    }

    public Criteria boost(float boost) {
        Assert.isTrue(boost >= 0, "boost must be positive");
        this.boost = boost;
        return this;
    }

    public float getBoost() {
        return this.boost;
    }

    public boolean isAnd() {
        return getOperator() == Operator.AND;
    }

    public boolean isOr() {
        return getOperator() == Operator.OR;
    }

    public Set<Criteria> getSubCriteria() {
        return subCriteria;
    }

    public Criteria and(Field field) {
        return new Criteria(criteriaChain, field);
    }

    public Criteria and(String fieldName) {
        return new Criteria(criteriaChain, fieldName);
    }

    public Criteria and(Criteria... criterias) {
        Assert.notNull(criterias, "cannot chain null criterias");
        this.criteriaChain.addAll(Arrays.asList(criterias));
        return this;
    }

    public Criteria and(Criteria criteria) {
        Assert.notNull(criteria, "Cannot chain null criteria");

        this.criteriaChain.add(criteria);
        return this;
    }

    public Criteria or(Field field) {
        return new OrCriteria(this.criteriaChain, field);
    }

    public Criteria or(String fieldName) {
        return or(new SimpleField(fieldName));
    }

    public Criteria or(Criteria criteria) {
        Assert.notNull(criteria, "Cannot chain null criteria");
        Assert.notNull(criteria.getField(), "Cannot chain criteria with no field");

        Criteria orCriteria = new OrCriteria(this.criteriaChain, criteria.getField());
        orCriteria.queryCriteriaEntries.addAll(criteria.queryCriteriaEntries);
        orCriteria.filterCriteriaEntries.addAll(criteria.filterCriteriaEntries);
        return orCriteria;
    }

    public Criteria subCriteria(Criteria criteria) {
        Assert.notNull(criteria, "criteria must not be null");
        subCriteria.add(criteria);
        return this;
    }

    public Criteria is(Object o) {
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.EQUALS, o));
        return this;
    }

    public Criteria between(@Nullable Object lowerBound, @Nullable Object upperBound) {
        if (lowerBound == null && upperBound == null) {
            throw new InvalidDataAccessApiUsageException("Range [* to *] is not allowed");
        }
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.BETWEEN, new Object[] {lowerBound, upperBound}));
        return this;
    }

    public Criteria exists() {
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.EXISTS));
        return this;
    }

    public Criteria startsWith(String s) {
        Assert.notNull(s, "s may not be null");
        assertNotBlankInWildcardQuery(s, true, false);
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.STARTS_WITH, s));
        return this;
    }

    public Criteria contains(String s) {
        Assert.notNull(s, "s may not be null");
        assertNotBlankInWildcardQuery(s, true, true);
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.CONTAINS, s));
        return this;
    }

    public Criteria endsWith(String s) {
        Assert.notNull(s, "s may not be null");
        assertNotBlankInWildcardQuery(s, false, true);
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.ENDS_WITH, s));
        return this;
    }

    public Criteria in(Object... values) {
        return in(toCollection(values));
    }

    public Criteria in(Iterable<?> values) {
        Assert.notNull(values, "Collection of 'in' values must not be null");
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.IN, values));
        return this;
    }

    public Criteria notIn(Object... values) {
        return notIn(toCollection(values));
    }

    public Criteria notIn(Iterable<?> values) {
        Assert.notNull(values, "Collection of 'notIn' values must not be null");
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.NOT_IN, values));
        return this;
    }

    public Criteria expression(String s) {
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.EXPRESSION, s));
        return this;
    }

    public Criteria fuzzy(String s) {
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.FUZZY, s));
        return this;
    }

    public Criteria lessThanEqual(Object upperBound) {
        Assert.notNull(upperBound, "upperBound must not be null");
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.LESS_EQUAL, upperBound));
        return this;
    }

    public Criteria lessThan(Object upperBound) {
        Assert.notNull(upperBound, "upperBound must not be null");
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.LESS, upperBound));
        return this;
    }

    public Criteria greaterThanEqual(Object lowerBound) {
        Assert.notNull(lowerBound, "lowerBound must not be null");
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.GREATER_EQUAL, lowerBound));
        return this;
    }

    public Criteria greaterThan(Object lowerBound) {
        Assert.notNull(lowerBound, "lowerBound must not be null");
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.GREATER, lowerBound));
        return this;
    }

    public Criteria matches(Object value) {
        Assert.notNull(value, "value must not be null");
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.MATCHES, value));
        return this;
    }

    public Criteria matchesAll(Object value) {
        Assert.notNull(value, "value must not be null");
        queryCriteriaEntries.add(new CriteriaEntry(OperationKey.MATCHES_ALL, value));
        return this;
    }

    private List<Object> toCollection(Object... values) {
        return Arrays.asList(values);
    }

    private void assertNotBlankInWildcardQuery(String searchString, boolean leadingWildcard, boolean trailingWildcard) {
        if (searchString.contains(CRITERIA_VALUE_SEPERATOR)) {
            throw new InvalidDataAccessApiUsageException("Cannot constructQuery '" + (leadingWildcard ? "*" : "") +'"'
            + searchString + '"' + (trailingWildcard ? "*" : "") + "'. Use expression or multiple clauses instead.");
        }
    }

    static class OrCriteria extends Criteria {
        public OrCriteria() {
            super();
        }

        public OrCriteria(String fieldName) {
            super(fieldName);
        }

        public OrCriteria(Field field) {
            super(field);
        }

        public OrCriteria(List<Criteria> criteriaChain, String fieldName) {
            super(criteriaChain, fieldName);
        }

        public OrCriteria(List<Criteria> criteriaChain, Field field) {
            super(criteriaChain, field);
        }

        @Override
        public Operator getOperator() {
            return Operator.OR;
        }
    }

    public static class CriteriaChain extends LinkedList<Criteria> {}

    public enum Operator {
        AND, OR;
    }

    public enum OperationKey {
        EQUALS,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        EXPRESSION, //
        BETWEEN, //
        FUZZY, //
        /**
         * @since 4.1
         */
        MATCHES, //
        /**
         * @since 4.1
         */
        MATCHES_ALL, //
        IN, //
        NOT_IN, //
        WITHIN, //
        BBOX, //
        LESS, //
        LESS_EQUAL, //
        GREATER, //
        GREATER_EQUAL, //
        /**
         * @since 4.0
         */
        EXISTS //
    }

    public static class CriteriaEntry {
        private final OperationKey key;
        @Nullable private Object value;

        protected CriteriaEntry(OperationKey key) {
            Assert.notNull(key, "key must not be null");
            this.key = key;
        }

        CriteriaEntry(OperationKey key, @Nullable Object value) {
            Assert.notNull(key, "key must not be null");
            Assert.notNull(value, "value must not be null");
            this.key = key;
            this.value = value;
        }

        @Nullable
        public Object getValue() {
            return value;
        }

        public void setValue(@Nullable Object value) {
            this.value = value;
        }

        public OperationKey getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CriteriaEntry that = (CriteriaEntry) o;
            if (key != that.key) return false;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "CriteriaEntry{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Criteria criteria = (Criteria) o;
        return Float.compare(criteria.boost, boost) == 0 &&
                negating == criteria.negating &&
                Objects.equals(field, criteria.field) &&
                criteriaChain.equals(criteria.criteriaChain) &&
                queryCriteriaEntries.equals(criteria.queryCriteriaEntries) &&
                filterCriteriaEntries.equals(criteria.filterCriteriaEntries) &&
                subCriteria.equals(criteria.subCriteria);
    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (boost != +0.0f ? Float.floatToIntBits(boost) : 0);
        result = 31 * result + (negating ? 1 : 0);
        result = 31 * result + queryCriteriaEntries.hashCode();
        result = 31 * result + filterCriteriaEntries.hashCode();
        result = 31 * result + subCriteria.hashCode();
        return result;
    }
}
