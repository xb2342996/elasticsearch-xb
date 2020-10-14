package com.xxbb.springframework.data.elasticsearch.core.query;

import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

public class CriteriaQuery extends AbstractQuery {
    private Criteria criteria;

    public CriteriaQuery(Criteria criteria) {
        this(criteria, Pageable.unpaged());
    }

    public CriteriaQuery(Criteria criteria, Pageable pageable) {
        Assert.notNull(criteria, "criteria must not be null");
        Assert.notNull(pageable, "pageable must not be null");

        this.criteria = criteria;
        this.pageable = pageable;
        this.addSort(pageable.getSort());
    }

    public static Query fromQuery(CriteriaQuery source) {
        return fromQuery(source, new CriteriaQuery(source.criteria));
    }

    public static <T extends CriteriaQuery> T fromQuery(CriteriaQuery source, T destination) {
        Assert.notNull(source, "source must not be null");
        Assert.notNull(destination, "destination must not be null");

        destination.addCriteria(source.getCriteria());

        if (source.getSort() != null) {
            destination.addSort(source.getSort());
        }
        return destination;
    }

    public final <T extends CriteriaQuery> T addCriteria(Criteria criteria) {
        Assert.notNull(criteria, "criteria must not be null");
        this.criteria.and(criteria);
        return (T) this;
    }

    public Criteria getCriteria() {
        return this.criteria;
    }
}
