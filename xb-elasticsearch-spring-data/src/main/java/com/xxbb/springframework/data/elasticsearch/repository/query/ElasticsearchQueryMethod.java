package com.xxbb.springframework.data.elasticsearch.repository.query;

import com.xxbb.springframework.data.elasticsearch.annotations.Highlight;
import com.xxbb.springframework.data.elasticsearch.core.SearchHit;
import com.xxbb.springframework.data.elasticsearch.core.SearchPage;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.xxbb.springframework.data.elasticsearch.annotations.Query;
import com.xxbb.springframework.data.elasticsearch.core.query.HighlightQuery;
import com.xxbb.springframework.data.elasticsearch.core.query.HighlightQueryBuilder;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.util.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.stream.Stream;

public class ElasticsearchQueryMethod extends QueryMethod {
    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;
    private @Nullable ElasticsearchEntityMetadata<?> metadata;
    private final Method method;
    private final Query queryAnnotation;
    private final Highlight highlightAnnotation;
    private final Lazy<HighlightQuery> highlightQueryLazy = Lazy.of(this::createAnnotatedHighlightQuery);


    public ElasticsearchQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                                    MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
        super(method, metadata, factory);

        Assert.notNull(mappingContext, "MappingContext must not be null");

        this.mappingContext = mappingContext;
        this.method = method;
        this.queryAnnotation = method.getAnnotation(Query.class);
        this.highlightAnnotation = method.getAnnotation(Highlight.class);
    }

    public boolean hasAnnotatedQuery() {
        return this.queryAnnotation != null;
    }

    public String getAnnotatedQuery() {
        return (String) AnnotationUtils.getValue(queryAnnotation, "value");
    }

    public boolean hasAnnotatedHighlight() {
        return this.highlightAnnotation != null;
    }

    public HighlightQuery getAnnotatedHighlightQuery() {
        Assert.isTrue(hasAnnotatedHighlight(), "no highlight annotation present on " + getName());
        return highlightQueryLazy.get();
    }

    public HighlightQuery createAnnotatedHighlightQuery() {
        return new HighlightQueryBuilder(mappingContext).getHighlightQuery(highlightAnnotation, getDomainClass());
    }

    @Override
    public ElasticsearchEntityMetadata<?> getEntityInformation() {
        if (metadata == null) {
            Class<?> returnObjectType = getReturnedObjectType();
            Class<?> domainClass = getDomainClass();

            if (ClassUtils.isPrimitiveOrWrapper(returnObjectType)) {
                this.metadata = new SimpleElasticsearchEntityMetadata<>((Class<Object>) domainClass, mappingContext.getRequiredPersistentEntity(domainClass));
            } else {
                ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(returnObjectType);
                ElasticsearchPersistentEntity<?> managedEntity = mappingContext.getRequiredPersistentEntity(domainClass);

                persistentEntity = persistentEntity == null || persistentEntity.getType().isInterface() ? managedEntity : persistentEntity;

                ElasticsearchPersistentEntity<?> collectionEntity = domainClass.isAssignableFrom(returnObjectType) ? persistentEntity : managedEntity;
                this.metadata = new SimpleElasticsearchEntityMetadata<>((Class<Object>) persistentEntity.getType(), collectionEntity);
            }
        }

        return this.metadata;
    }

    protected MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> getMappingContext() {
        return mappingContext;
    }

    public boolean isSearchHitMethod() {
        Class<?> methodReturnType = method.getReturnType();
        if (SearchHit.class.isAssignableFrom(methodReturnType)) {
            return true;
        }

        try {
            ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
            if (isAllowGenericType(parameterizedType)) {
                ParameterizedType collectionTypeArgument = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];
                if (SearchHit.class.isAssignableFrom((Class<?>) collectionTypeArgument.getRawType())) {
                    return true;
                }
            }
        } catch (Exception ignored) {}

        return false;
    }

    public boolean isSearchPageMethod() {
        return SearchPage.class.isAssignableFrom(methodReturnType());
    }

    public Class<?> methodReturnType() {
        return method.getReturnType();
    }

    protected boolean isAllowGenericType(ParameterizedType parameterizedType) {
        return Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())
                || Stream.class.isAssignableFrom((Class<?>) parameterizedType.getRawType());
    }

    public boolean isNotSearchHitMethod() {
        return !isSearchHitMethod();
    }
}
