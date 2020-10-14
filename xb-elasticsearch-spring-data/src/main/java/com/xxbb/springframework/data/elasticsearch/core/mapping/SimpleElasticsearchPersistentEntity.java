package com.xxbb.springframework.data.elasticsearch.core.mapping;

import com.xxbb.springframework.data.elasticsearch.annotations.Setting;
import com.xxbb.springframework.data.elasticsearch.core.document.Document;
import com.xxbb.springframework.data.elasticsearch.core.join.JoinField;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.VersionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mapping.model.PersistentPropertyAccessorFactory;
import org.springframework.data.util.TypeInformation;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleElasticsearchPersistentEntity<T> extends BasicPersistentEntity<T, ElasticsearchPersistentProperty> implements ElasticsearchPersistentEntity<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleElasticsearchPersistentEntity.class);
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    private @Nullable String indexName;
    private boolean useServerConfiguration;
    private short shards;
    private short replicas;
    private @Nullable String refreshInterval;
    private @Nullable String indexStoreType;
    private @Nullable String parentType;
    private @Nullable ElasticsearchPersistentProperty scoreProperty;
    private @Nullable ElasticsearchPersistentProperty seqNoPrimaryTermProperty;
    private @Nullable ElasticsearchPersistentProperty joinFieldProperty;
    private @Nullable String settingPath;
    private @Nullable VersionType versionType;
    private boolean createIndexAndMapping;
    private final Map<String, ElasticsearchPersistentProperty> fieldNamePropertyCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Expression> indexNameExpression = new ConcurrentHashMap<>();

    public SimpleElasticsearchPersistentEntity(TypeInformation<T> information) {
        super(information);

        Class<T> clazz = information.getType();
        if (clazz.isAnnotationPresent(com.xxbb.springframework.data.elasticsearch.annotations.Document.class)) {
            com.xxbb.springframework.data.elasticsearch.annotations.Document document = clazz.getAnnotation(com.xxbb.springframework.data.elasticsearch.annotations.Document.class);
            Assert.notNull(document.indexName(), "Unknown indexName. Make sure the indexName is defined. e.g @Document(indexName=\"foo\"");
            this.indexName = document.indexName();
            this.useServerConfiguration = document.useServerConfigration();
            this.shards = document.shards();
            this.replicas = document.replicas();
            this.refreshInterval = document.refreshInterval();
            this.indexStoreType = document.indexStoreType();
            this.versionType = document.versionType();
            this.createIndexAndMapping = document.createIndex();
        }

        if (clazz.isAnnotationPresent(Setting.class)) {
            this.settingPath = information.getType().getAnnotation(Setting.class).settingPath();
        }
    }

    private String getIndexName() {
        return indexName != null ? indexName : getTypeInformation().getType().getSimpleName();
    }

    @Override
    public IndexCoordinates getIndexCoordinates() {
        return resolve(IndexCoordinates.of(getIndexName()));
    }

    @Override
    public short getShards() {
        return shards;
    }

    @Override
    public short getReplicas() {
        return replicas;
    }

    @Override
    public boolean isUseServerConfiguration() {
        return useServerConfiguration;
    }

    @Override
    public String getRefreshInterval() {
        return refreshInterval;
    }

    @Override
    public String getIndexStoreType() {
        return indexStoreType;
    }

    @Override
    public String getParentType() {
        return parentType;
    }

    @Override
    public String settingPath() {
        return settingPath;
    }

    @Override
    public VersionType getVersionType() {
        return versionType;
    }

    @Override
    public boolean isCreateIndexAndMapping() {
        return createIndexAndMapping;
    }

    @Override
    public boolean hasScoreProperty() {
        return scoreProperty != null;
    }

    @Override
    public ElasticsearchPersistentProperty getScoreProperty() {
        return scoreProperty;
    }

    @Override
    public void addPersistentProperty(ElasticsearchPersistentProperty property) {
        super.addPersistentProperty(property);

        if (property.isSeqNoPrimaryTermProperty()) {
            ElasticsearchPersistentProperty seqNoPrimaryTermProperty = this.seqNoPrimaryTermProperty;

            if (seqNoPrimaryTermProperty != null) {
                throw new MappingException(String.format(
                        "Attempt to add SeqNoPrimaryTerm property %s but already have property %s registered"
                        +" as SeqNoPrimaryTerm property, Check your entity Configuration!",
                        property.getField(), seqNoPrimaryTermProperty.getField()));
            }
            this.seqNoPrimaryTermProperty = property;
            if (hasVersionProperty()) {
                warnAboutBothSeqNoPrimaryTermAndVersionProperties();
            }
        }

        if (property.isVersionProperty()) {
            if (hasSeqNoPrimaryTermProperty()) {
                warnAboutBothSeqNoPrimaryTermAndVersionProperties();
            }
        }

        if (property.getActualType() == JoinField.class) {
            ElasticsearchPersistentProperty joinProperty = this.joinFieldProperty;

            if (joinProperty != null) {
                throw new MappingException(String.format(
                        "Attempt to add Join property %s but already have property %s registered"
                                +" as Join property, Check your entity Configuration!",
                        property.getField(), joinProperty.getField()));
            }

            this.joinFieldProperty = property;
        }
    }

    private void warnAboutBothSeqNoPrimaryTermAndVersionProperties() {
        LOGGER.warn("Both SeqNoPrimaryTerm and @Version properties are defined on {}. Version will not be sent in index requests when seq_no is sent!", getType());
    }

    @Override
    public void setPersistentPropertyAccessorFactory(PersistentPropertyAccessorFactory factory) {

    }

    @Nullable
    @Override
    public ElasticsearchPersistentProperty getPersistentPropertyWithFieldName(String fieldName) {
        Assert.notNull(fieldName, "fieldname must not be null");
        return fieldNamePropertyCache.computeIfAbsent(fieldName, key -> {
            AtomicReference<ElasticsearchPersistentProperty> propertyRef = new AtomicReference<>();
            doWithProperties((PropertyHandler<ElasticsearchPersistentProperty>) property -> {
                if (key.equals(property.getFieldName())) {
                    propertyRef.set(property);
                }
            });
            return propertyRef.get();
        });
    }

    @Override
    public boolean hasSeqNoPrimaryTermProperty() {
        return seqNoPrimaryTermProperty != null;
    }

    @Override
    public boolean hasJoinFieldProperty() {
        return joinFieldProperty != null;
    }

    @Override
    public ElasticsearchPersistentProperty getSeqNoPrimaryTermProperty() {
        return seqNoPrimaryTermProperty;
    }

    @Override
    public ElasticsearchPersistentProperty getJoinFieldPorperty() {
        return joinFieldProperty;
    }

    @Override
    public Document getDefaultSettings() {
        if (isUseServerConfiguration()) {
            return Document.create();
        }
        Map<String, String> map = new MapBuilder<String, String>()
                .put("index.number_of_shards", String.valueOf(getShards()))
                .put("index.number_of_replicas", String.valueOf(getReplicas()))
                .put("index.refresh_interval", getRefreshInterval())
                .put("index.store.type", getIndexStoreType())
                .map();
        return Document.from(map);
    }

    private IndexCoordinates resolve(IndexCoordinates indexCoordinates) {
        EvaluationContext context = getEvaluationContext(null);

        String[] indexNames = indexCoordinates.getIndexNames();
        String[] resolvedNames = new String[indexNames.length];
        for (int i = 0; i < indexNames.length; i++) {
            String indexName = indexNames[i];
            resolvedNames[i] = resolve(context, indexName);
        }
        return IndexCoordinates.of(resolvedNames);
    }

    private String resolve(EvaluationContext context, String name) {
        Assert.notNull(name, "name must not be null");
        Expression expression = indexNameExpression.computeIfAbsent(name, s -> {
            Expression expr = PARSER.parseExpression(name, ParserContext.TEMPLATE_EXPRESSION);
            return expr instanceof LiteralExpression ? null : expr;
        });
        String resolvedName = expression != null ? expression.getValue(context, String.class) : null;
        return resolvedName != null ? resolvedName : name;
    }

}
