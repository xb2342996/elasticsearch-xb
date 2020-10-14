package com.xxbb.springframework.data.elasticsearch.core.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxbb.springframework.data.elasticsearch.annotations.*;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import com.xxbb.springframework.data.elasticsearch.core.ResourceUtil;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.elasticsearch.ElasticsearchException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;

import static com.xxbb.springframework.data.elasticsearch.core.index.MappingParameters.FIELD_PARAM_STORE;
import static com.xxbb.springframework.data.elasticsearch.core.index.MappingParameters.FIELD_PARAM_TYPE;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class MappingBuilder {
    private static final String FIELD_INDEX = "index";
    private static final String FIELD_PROPERTIES = "properties";
    private static final String FIELD_PARENT = "_parent";
    private static final String FIELD_CONTEXT_NAME = "name";
    private static final String FIELD_CONTEXT_TYPE = "type";
    private static final String FIELD_CONTENT_PATH = "path";
    private static final String FIELD_CONTEXT_PRECISION = "precision";
    private static final String FIELD_DYNAMIC_TEMPLATES = "dynamic_templates";

    private static final String COMPLETION_PRESERVE_SEPARATORS = "preserve_separators";
    private static final String COMPLETION_PRESERVE_POSITION_INCREMENTS = "preserve_position_increments";
    private static final String COMPLETION_MAX_INPUT_LENGTH = "max_input_length";
    private static final String COMPLETION_CONTEXTS = "contexts";

    private static final String TYPE_DYNAMIC = "dynamic";
    private static final String TYPE_VALUE_KEYWORD = "keyword";
    private static final String TYPE_VALUE_GEO_POINT = "geo_point";
    private static final String TYPE_VALUE_COMPLETION = "completiion";

    public static final Logger logger = LoggerFactory.getLogger(ElasticsearchRestTemplate.class);

    private final ElasticsearchConverter elasticsearchConverter;

    public MappingBuilder(ElasticsearchConverter elasticsearchConverter) {
        this.elasticsearchConverter = elasticsearchConverter;
    }

    public String buildPropertyMapping(Class<?> clazz) throws ElasticsearchException {
        try {
            ElasticsearchPersistentEntity<?> entity = elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz);
            XContentBuilder builder = jsonBuilder().startObject();

            addDynamicTemplatesMapping(builder, entity);

            // todo parent
//            String parentType = entity.getParentType();

            mapEntity(builder, entity, true, "", false, FieldType.Auto, null, entity.findAnnotation(DynamicMapping.class));

            builder.endObject().close();

            return builder.getOutputStream().toString();
        } catch (MappingException | IOException e) {
            throw new ElasticsearchException("could not build mapping", e);
        }
    }

    private void mapEntity(XContentBuilder builder, @Nullable ElasticsearchPersistentEntity<?> entity, boolean isRootObject, String nestedObjectFieldName,
                           boolean nestedOrObjectField, FieldType fieldType, @Nullable Field parentFieldAnnotation, @Nullable DynamicMapping dynamicMapping) throws IOException {

        boolean writeNestedProperty =  !isRootObject && (isAnyPropertyAnnotatedWithField(entity) || nestedOrObjectField);
        if (writeNestedProperty) {
            String type = nestedOrObjectField ? fieldType.toString().toLowerCase() : FieldType.Object.toString().toLowerCase();

            builder.startObject(nestedObjectFieldName).field(FIELD_PARAM_TYPE, type);
            if (nestedOrObjectField && FieldType.Nested == fieldType && parentFieldAnnotation != null
                    && parentFieldAnnotation.includeInParent()) {
                builder.field("include_in_parent", true);
            }
        }

        if (dynamicMapping != null) {
            builder.field(TYPE_DYNAMIC, dynamicMapping.value().name().toLowerCase());
        }

        builder.startObject(FIELD_PROPERTIES);

        if (entity != null) {
            entity.doWithProperties((PropertyHandler<ElasticsearchPersistentProperty>) property -> {
                try {
                    if (property.isAnnotationPresent(Transient.class) || isInIgnoreFields(property, parentFieldAnnotation)) {
                        return;
                    }

                    if (property.isSeqNoPrimaryTermProperty()) {
                        if (property.isAnnotationPresent(Field.class)) {
                            logger.warn("Property {} of {} is annotated for inclusion in mapping, but its type is" +
                                    "SeqNoPrimaryTerm that is never mapped, so it is skipped", property.getFieldName(), entity.getType());
                        }
                        return;
                    }
                    buildPropertyMapping(builder, isRootObject, property);
                } catch (IOException e) {
                    logger.warn("error mapping property with name {}", property.getName(), e);
                }
            });
        }

        builder.endObject();
        if (writeNestedProperty) {
            builder.endObject();
        }
    }

    private void buildPropertyMapping(XContentBuilder builder, boolean isRootObject, ElasticsearchPersistentProperty property) throws IOException {
        if (property.isAnnotationPresent(Mapping.class)) {
            String mappingPath = property.getRequiredAnnotation(Mapping.class).mappingPath();

            if (!StringUtils.isEmpty(mappingPath)) {
                ClassPathResource mappings = new ClassPathResource(mappingPath);
                if (mappings.exists()) {
                    builder.rawField(property.getFieldName(), mappings.getInputStream(), XContentType.JSON);
                    return;
                }
            }
        }

        if (isGeoPointProperty(property)) {
            applyGeoPointFieldMapping(builder, property);
            return;
        }

        Field fieldAnnotation = property.findAnnotation(Field.class);
        boolean isCompletionProperty = isCompletionProperty(property);
        boolean isNestedOrObjectProperty = isNestedOrObjectProperty(property);

        if (!isCompletionProperty && property.isEntity() && hasRelevantAnnotation(property)) {
            if (fieldAnnotation == null) {
                return;
            }

            if (isNestedOrObjectProperty) {
                Iterator<? extends TypeInformation<?>> iterator = property.getPersistentEntityTypes().iterator();
                ElasticsearchPersistentEntity<?> persistentEntity = iterator.hasNext()
                        ? elasticsearchConverter.getMappingContext().getPersistentEntity(iterator.next())
                        : null;
                mapEntity(builder, persistentEntity, false, property.getFieldName(), isNestedOrObjectProperty, fieldAnnotation.type(), fieldAnnotation, property.findAnnotation(DynamicMapping.class));
                return;
            }
        }
        // todo multi field mapping
        MultiField multiField = property.findAnnotation(MultiField.class);
        if (isCompletionProperty) {
            CompletionField completionField = property.findAnnotation(CompletionField.class);
            applyCompletionFieldMapping(builder, property, completionField);
        }


        if (isRootObject && fieldAnnotation != null && property.isIdProperty()) {
            applyDefaultFieldMapping(builder, property);
        } else if (multiField != null) {
            addMultiFieldMapping(builder, property, multiField, isNestedOrObjectProperty);
        } else if (fieldAnnotation != null) {
            addSingleFieldMapping(builder, property, fieldAnnotation, isNestedOrObjectProperty);
        }
    }

    private boolean hasRelevantAnnotation(ElasticsearchPersistentProperty property) {
        // TODO GEO MULTI COMPLETION ANNOTATION
        return property.findAnnotation(Field.class) != null || property.findAnnotation(MultiField.class) != null
                || property.findAnnotation(CompletionField.class) != null;
    }

    private void applyGeoPointFieldMapping(XContentBuilder builder, ElasticsearchPersistentProperty property) throws IOException {
        throw new UnsupportedOperationException("@GeoPoint is not supported yet");
    }

    private void applyCompletionFieldMapping(XContentBuilder builder, ElasticsearchPersistentProperty property, @Nullable CompletionField annotation) throws IOException {
        throw new UnsupportedOperationException("@CompletionField is not supported yet!");
    }

    private void applyDefaultFieldMapping(XContentBuilder builder, ElasticsearchPersistentProperty property) throws IOException {
        builder.startObject(property.getFieldName()).field(FIELD_PARAM_TYPE, TYPE_VALUE_KEYWORD).field(FIELD_INDEX, true).endObject();
    }

    private void addSingleFieldMapping(XContentBuilder builder, ElasticsearchPersistentProperty property, Field annotation, boolean nestedOrObjectField) throws IOException{
        XContentBuilder propertyBuilder = jsonBuilder().startObject();
        addFieldMappingParameters(propertyBuilder, annotation, nestedOrObjectField);
        propertyBuilder.endObject().close();

        if ("{}".equals(propertyBuilder.getOutputStream().toString())) {
            return;
        }
        builder.startObject(property.getFieldName());
        addFieldMappingParameters(builder, annotation, nestedOrObjectField);
        builder.endObject();
    }

    private void addMultiFieldMapping(XContentBuilder builder, ElasticsearchPersistentProperty property, MultiField annotation, boolean nestedOrObjectField) {
        throw new UnsupportedOperationException("@multiField is not supported now!");
    }

    private void addFieldMappingParameters(XContentBuilder builder, Annotation annotation, boolean nestedOrObjectField) throws IOException {
        MappingParameters mappingParameters = MappingParameters.from(annotation);

        if (!nestedOrObjectField && mappingParameters.isStore()) {
            builder.field(FIELD_PARAM_STORE, true);
        }
        mappingParameters.writeTypeAndParametersTo(builder);
    }


    private void addDynamicTemplatesMapping(XContentBuilder builder, ElasticsearchPersistentEntity<?> entity) throws IOException {
        if (entity.isAnnotationPresent(DynamicTemplates.class)) {
            String mappingPath = entity.getRequiredAnnotation(DynamicTemplates.class).mappingPath();
            if (StringUtils.hasText(mappingPath)) {
                String jsonString = ResourceUtil.readFileFromClasspath(mappingPath);
                if (StringUtils.hasText(jsonString)) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(jsonString).get("dynamic_templates");
                    if (jsonNode != null && jsonNode.isArray()) {
                        String json = objectMapper.writeValueAsString(jsonNode);
                        builder.rawField(FIELD_DYNAMIC_TEMPLATES, new ByteArrayInputStream(json.getBytes()), XContentType.JSON);
                    }
                }
            }
        }
    }

    private boolean isAnyPropertyAnnotatedWithField(@Nullable ElasticsearchPersistentEntity entity) {
        return entity != null && entity.getPersistentProperty(Field.class) != null;
    }

    private boolean isInIgnoreFields(ElasticsearchPersistentProperty property, @Nullable Field parentFieldAnnotatiion) {
        if (null != parentFieldAnnotatiion) {
            String[] ignoreFields = parentFieldAnnotatiion.ignoreFields();
            return Arrays.asList(ignoreFields).contains(property.getFieldName());
        }
        return false;
    }

    private boolean isNestedOrObjectProperty(ElasticsearchPersistentProperty property) {
        Field fieldAnnotation = property.findAnnotation(Field.class);
        return fieldAnnotation != null && (FieldType.Nested == fieldAnnotation.type() || FieldType.Object == fieldAnnotation.type());
    }

    private boolean isGeoPointProperty(ElasticsearchPersistentProperty property) {
//        logger.error("Geo property is not implemented now!");
        return false;
    }

    private boolean isCompletionProperty(ElasticsearchPersistentProperty property) {
//        logger.error("Completion property is not implemented now!");
        return false;
    }
}
