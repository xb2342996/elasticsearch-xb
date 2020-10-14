package com.xxbb.springframework.data.elasticsearch.repository.query.parser;

import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.xxbb.springframework.data.elasticsearch.core.query.Criteria;
import com.xxbb.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;

public class ElasticsearchQueryCreator extends AbstractQueryCreator<CriteriaQuery, CriteriaQuery> {

    private final MappingContext<?, ElasticsearchPersistentProperty> mappingContext;

    public ElasticsearchQueryCreator(PartTree tree, ParameterAccessor parameters, MappingContext<?, ElasticsearchPersistentProperty> mappingContext) {
        super(tree, parameters);
        this.mappingContext = mappingContext;
    }

    public ElasticsearchQueryCreator(PartTree tree, MappingContext<?, ElasticsearchPersistentProperty> mappingContext) {
        super(tree);
        this.mappingContext = mappingContext;
    }

    @Override
    protected CriteriaQuery create(Part part, Iterator<Object> iterator) {
        PersistentPropertyPath<ElasticsearchPersistentProperty> path = mappingContext.getPersistentPropertyPath(part.getProperty());
        return new CriteriaQuery(from(part,
                new Criteria(path.toDotPath(ElasticsearchPersistentProperty.QueryPropertyToFieldNameConverter.INSTANCE)),
                iterator));
    }

    @Override
    protected CriteriaQuery and(Part part, CriteriaQuery base, Iterator<Object> iterator) {
        if (base == null) {
            return create(part, iterator);
        }
        PersistentPropertyPath<ElasticsearchPersistentProperty> path = mappingContext.getPersistentPropertyPath(part.getProperty());
        return base.addCriteria(from(part,
                new Criteria(path.toDotPath(ElasticsearchPersistentProperty.QueryPropertyToFieldNameConverter.INSTANCE)),
                iterator));
    }

    @Override
    protected CriteriaQuery or(CriteriaQuery base, CriteriaQuery criteria) {
        return new CriteriaQuery(base.getCriteria().or(criteria.getCriteria()));
    }

    @Override
    protected CriteriaQuery complete(@Nullable CriteriaQuery criteria, Sort sort) {
        if (criteria == null) {
            criteria = new CriteriaQuery(new Criteria());
        }
        return criteria.addSort(sort);
    }

    private Criteria from(Part part, Criteria criteria, Iterator<?> parameters) {
        Part.Type type = part.getType();

        switch (type) {
            case TRUE:
                return criteria.is(true);
            case FALSE:
                return criteria.is(false);
            case NEGATING_SIMPLE_PROPERTY:
                return criteria.is(parameters.next()).not();
            case REGEX:
                return criteria.expression(parameters.next().toString());
            case LIKE:
            case STARTING_WITH:
                return criteria.startsWith(parameters.next().toString());
            case ENDING_WITH:
                return criteria.endsWith(parameters.next().toString());
            case CONTAINING:
                return criteria.contains(parameters.next().toString());
            case GREATER_THAN:
                return criteria.greaterThan(parameters.next());
            case AFTER:
            case GREATER_THAN_EQUAL:
                return criteria.greaterThanEqual(parameters.next());
            case LESS_THAN:
                return criteria.lessThan(parameters.next());
            case BEFORE:
            case LESS_THAN_EQUAL:
                return criteria.lessThanEqual(parameters.next());
            case BETWEEN:
                return criteria.between(parameters.next(), parameters.next());
            case IN:
                return criteria.in(asArray(parameters.next()));
            case NOT_IN:
                return criteria.notIn(asArray(parameters.next()));
            case SIMPLE_PROPERTY:
            case WITHIN: {
                Object firstParameter = parameters.next();
                Object secondParameter = null;
                if (type == Part.Type.SIMPLE_PROPERTY) {
                    if (part.getProperty().getType() != GeoPoint.class){
                        if (firstParameter != null) {
                            return criteria.is(firstParameter);
                        } else {
                            return criteria.exists().not();
                        }
                    } else {
                        secondParameter = ".001km";
                    }
                } else {
                    secondParameter = parameters.next();
                }

                if (firstParameter instanceof GeoPoint && secondParameter instanceof String) {
                    throw new UnsupportedOperationException("Geo Point class is not supported yet!");
                }

                if (firstParameter instanceof Point && secondParameter instanceof Distance) {
                    throw new UnsupportedOperationException("Location class is not support yet!");
                }

                if (firstParameter instanceof String && secondParameter instanceof String) {
                    throw new UnsupportedOperationException("Geo Location class is not support yet!");
                }
            }
            case NEAR:
                throw new UnsupportedOperationException("unsupported Geo Location class!");
            default:
                throw new InvalidDataAccessApiUsageException("Illegal criteria found '" + type + "'.");
        }
    }

    private Object[] asArray(Object o) {
        if (o instanceof Collection) {
            return ((Collection<?>) o).toArray();
        } else if (o.getClass().isArray()) {
            return (Object[]) o;
        }
        return new Object[] {o};
    }
}
