package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.annotations.Mapping;
import com.xxbb.springframework.data.elasticsearch.annotations.Setting;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.document.Document;
import com.xxbb.springframework.data.elasticsearch.core.index.MappingBuilder;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractDefaultIndexOperations implements IndexOperations {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDefaultIndexOperations.class);

    protected final ElasticsearchConverter elasticsearchConverter;
    protected final RequestFactory requestFactory;

    @Nullable
    protected final Class<?> boundClass;
    @Nullable
    private final IndexCoordinates boundIndex;

    public AbstractDefaultIndexOperations(ElasticsearchConverter elasticsearchConverter, Class<?> boundClass) {
        Assert.notNull(boundClass, "boundClass may not be null");
        this.elasticsearchConverter = elasticsearchConverter;
        requestFactory = new RequestFactory(elasticsearchConverter);
        this.boundClass = boundClass;
        this.boundIndex = null;
    }

    public AbstractDefaultIndexOperations(ElasticsearchConverter elasticsearchConverter, IndexCoordinates boundIndex) {
        Assert.notNull(boundIndex, "boundIndex may not be null");
        this.elasticsearchConverter = elasticsearchConverter;
        requestFactory = new RequestFactory(elasticsearchConverter);
        this.boundIndex = boundIndex;
        this.boundClass = null;
    }

    protected Class<?> checkForBoundClass() {
        if (boundClass == null) {
            throw new InvalidDataAccessApiUsageException("IndexOperations are not bound");
        }
        return boundClass;
    }

    @Override
    public boolean create() {
        Document settings = null;
        if (boundClass != null) {
            settings = createSettings(boundClass);
        }
        return doCreate(getIndexCoordinates().getIndexName(), settings);
    }

    @Override
    public Document createSettings(Class<?> clazz) {
        Assert.notNull(clazz, "class must not be null");
        Document settings = null;

        if (clazz.isAnnotationPresent(Setting.class)) {
            String settingPath = clazz.getAnnotation(Setting.class).settingPath();
            settings = loadSettings(settingPath);
        }

        if (settings == null) {
            settings = getRequiredPersistentEntity(clazz).getDefaultSettings();
        }
        return settings;
    }

    @Override
    public boolean create(Document settings) {
        return doCreate(getIndexCoordinates().getIndexName(), settings);
    }

    protected abstract boolean doCreate(String indexName, @Nullable Document settings);

    @Override
    public boolean delete() {
        return doDelete(getIndexCoordinates().getIndexName());
    }

    protected abstract boolean doDelete(String indexName);

    @Override
    public boolean exists() {
        return doExists(getIndexCoordinates().getIndexName());
    }

    protected abstract boolean doExists(String indexName);

    @Override
    public void refresh() {
        doRefresh(getIndexCoordinates());
    }

    protected abstract void doRefresh(IndexCoordinates indexCoordinates);
//    protected abstract boolean doRemoveAlias()

    @Override
    public Document createMapping() {
        return createMapping(checkForBoundClass());
    }

    @Override
    public Document createMapping(Class<?> clazz) {
        return buildMapping(clazz);
    }

    @Override
    public boolean putMapping(Document mapping) {
        return doPutMapping(getIndexCoordinates(), mapping);
    }

    protected abstract boolean doPutMapping(IndexCoordinates index, Document mapping);

    @Override
    public Map<String, Object> getMapping() {
        return doGetMapping(getIndexCoordinates());
    }

    protected abstract Map<String, Object> doGetMapping(IndexCoordinates index);

    @Override
    public Map<String, Object> getSettings() {
        return getSettings(false);
    }

    @Override
    public Map<String, Object> getSettings(boolean indcludeDefaults) {
        return doGetSettings(getIndexCoordinates().getIndexName(), indcludeDefaults);
    }

    protected abstract Map<String, Object> doGetSettings(String indexName, boolean includeDefaults);

    @Override
    public Document createSettings() {
        return createSettings(checkForBoundClass());
    }

    protected Document buildMapping(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Mapping.class)) {
            String mappingPath = clazz.getAnnotation(Mapping.class).mappingPath();

            if (!StringUtils.isEmpty(mappingPath)) {
                String mappings = ResourceUtil.readFileFromClasspath(mappingPath);
                if (!StringUtils.isEmpty(mappings)) {
                    return Document.parse(mappings);
                }
            } else {
                LOGGER.info("mappingPath in @Mapping has to be defined. Building mappings using @Field");
            }
        }

        try {
            String mapping = new MappingBuilder(elasticsearchConverter).buildPropertyMapping(clazz);
            return Document.parse(mapping);
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to build mapping for " + clazz.getSimpleName(), e);
        }
    }

    private <T> Document getDefaultSettings(ElasticsearchPersistentEntity<T> persistentEntity) {
        if (persistentEntity.isUseServerConfiguration()) {
            return Document.create();
        }
        Map<String, String> map = new MapBuilder<String, String>()
                .put("index.number_of_shards", String.valueOf(persistentEntity.getShards()))
                .put("index.number_of_replicas", String.valueOf(persistentEntity.getReplicas()))
                .put("index.refresh_interval", String.valueOf(persistentEntity.getRefreshInterval()))
                .put("index.store.type", persistentEntity.getIndexStoreType()).map();
        return Document.from(map);
    }

    ElasticsearchPersistentEntity<?> getRequiredPersistentEntity(Class<?> clazz) {
        return elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz);
    }

    @Override
    public IndexCoordinates getIndexCoordinates() {
        return (boundClass != null) ? getIndexCoordinatesFor(boundClass) : boundIndex;
    }

    public IndexCoordinates getIndexCoordinatesFor(Class<?> clazz) {
        return getRequiredPersistentEntity(clazz).getIndexCoordinates();
    }

    protected Map<String, Object> convertSettingsResponseToMap(GetSettingsResponse response, String indexName) {
        Map<String, Object> settings = new HashMap<>();

        if (!response.getIndexToDefaultSettings().isEmpty()) {
            Settings defaultSettings = response.getIndexToDefaultSettings().get(indexName);
            for (String key :
                    defaultSettings.keySet()) {
                settings.put(key, defaultSettings.get(key));
            }
        }

        if (!response.getIndexToSettings().isEmpty()) {
            Settings defaultSettings = response.getIndexToSettings().get(indexName);
            for (String key :
                    defaultSettings.keySet()) {
                settings.put(key, defaultSettings.get(key));
            }
        }
        return settings;
    }

    @Nullable
    private Document loadSettings(String settingPath) {
        if (StringUtils.hasText(settingPath)) {
            String settingFile = ResourceUtil.readFileFromClasspath(settingPath);

            if (StringUtils.hasText(settingFile)) {
                return Document.parse(settingFile);
            }
        }
        else {
            LOGGER.info("settingPath in @Setting has to be defined, Using default instead");
        }

        return null;
    }
}
