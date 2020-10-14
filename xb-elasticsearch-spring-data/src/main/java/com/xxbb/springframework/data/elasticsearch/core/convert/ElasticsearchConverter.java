package com.xxbb.springframework.data.elasticsearch.core.convert;

import com.xxbb.springframework.data.elasticsearch.core.document.Document;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.xxbb.springframework.data.elasticsearch.core.query.CriteriaQuery;
import com.xxbb.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.convert.EntityConverter;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public interface ElasticsearchConverter extends EntityConverter<ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty, Object, Document> {

    default ProjectionFactory getProjectionFactory() {
        return new SpelAwareProxyProjectionFactory();
    }

    default String convertId(Object idValue) {
        Assert.notNull(idValue, "idValue must not be null");
        if (!getConversionService().canConvert(idValue.getClass(), String.class)) {
            return idValue.toString();
        }

        return getConversionService().convert(idValue, String.class);
    }

    default Document mapObject(@Nullable Object source) {
        Document target = Document.create();

        if (source != null) {
            write(source, target);
        }
        return target;
    }

    default void updateQuery(Query query, @Nullable Class<?> clazz) {
        if (clazz != null) {
            if (query instanceof CriteriaQuery) {
                updateCriteriaQuery((CriteriaQuery) query, clazz);
            }
        }
    }

    void updateCriteriaQuery(CriteriaQuery query, Class<?> clazz);
}
