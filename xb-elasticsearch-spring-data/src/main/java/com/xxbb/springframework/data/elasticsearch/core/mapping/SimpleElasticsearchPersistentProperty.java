package com.xxbb.springframework.data.elasticsearch.core.mapping;

import com.xxbb.springframework.data.elasticsearch.annotations.DateFormat;
import com.xxbb.springframework.data.elasticsearch.annotations.Field;
import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import com.xxbb.springframework.data.elasticsearch.annotations.Score;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchDateConverter;
import com.xxbb.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SimpleElasticsearchPersistentProperty extends AnnotationBasedPersistentProperty<ElasticsearchPersistentProperty> implements ElasticsearchPersistentProperty {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleElasticsearchPersistentProperty.class);
    private static final List<String> SUPPORTED_ID_PROPERTY_NAMES = Arrays.asList("id", "document");
    private final boolean isScore;
    private final boolean isId;
    private final boolean isSeqNoPrimaryTerm;
    private final @Nullable String annotatedFieldName;
    @Nullable private ElasticsearchPersistentPropertyConverter propertyConverter;
    private final boolean storeNullValue;

    public SimpleElasticsearchPersistentProperty(Property property, PersistentEntity<?, ElasticsearchPersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {
        super(property, owner, simpleTypeHolder);

        this.annotatedFieldName = getAnnotatedFieldName();
        this.isId = super.isIdProperty() || SUPPORTED_ID_PROPERTY_NAMES.contains(getFieldName());
        this.isScore = isAnnotationPresent(Score.class);
        this.isSeqNoPrimaryTerm = SeqNoPrimaryTerm.class.isAssignableFrom(getRawType());

        if (isVersionProperty() && !getType().equals(Long.class)) {
            throw new MappingException(String.format("Version property %s must be of type Long!", property.getName()));
        }

        if (isScore && !getType().equals(Float.TYPE) && !getType().equals(Float.class)) {
            throw new MappingException(String.format("Score property %s must be either of type float or Float!", property.getName()));
        }

        boolean isField = isAnnotationPresent(Field.class);

        initDateConverter();
        storeNullValue = isField && getRequiredAnnotation(Field.class).storeNullValue();
    }

    @Override
    public String getFieldName() {
        return annotatedFieldName == null ? getProperty().getName() : annotatedFieldName;
    }

    @Override
    public boolean isIdProperty() {
        return isId;
    }

    @Override
    public boolean isScoreProperty() {
        return isScore;
    }

    @Override
    public boolean isSeqNoPrimaryTermProperty() {
        return isSeqNoPrimaryTerm;
    }

    @Override
    public boolean hasPropertyConverter() {
        return propertyConverter != null;
    }

    @Override
    public ElasticsearchPersistentPropertyConverter getPropertyConverter() {
        return propertyConverter;
    }

    @Override
    public boolean isReadable() {
        return !isTransient() && !isSeqNoPrimaryTermProperty();
    }

    @Override
    public boolean isWritable() {
        return super.isWritable() && !isSeqNoPrimaryTermProperty();
    }

    @Override
    public boolean storeNullValue() {
        return storeNullValue;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public boolean isGeoShapeProperty() {
        return false;
    }

    @Override
    public boolean isJoinFieldProperty() {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isCompletionProperty() {
        throw new UnsupportedOperationException();
    }


    @Override
    protected Association<ElasticsearchPersistentProperty> createAssociation() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private String getAnnotatedFieldName() {
        String name = null;
        if (isAnnotationPresent(Field.class)) {
            name = findAnnotation(Field.class).name();
        }
        return StringUtils.hasText(name) ? name : null;
    }

    private void initDateConverter() {
        Field field = findAnnotation(Field.class);
        Class<?> actualType = getActualType();
        boolean isTemporalAccessor = TemporalAccessor.class.isAssignableFrom(actualType);
        boolean isDate = Date.class.isAssignableFrom(actualType);

        if (field != null && (field.type() == FieldType.Date || field.type() == FieldType.Date_Nanos) && (isTemporalAccessor || isDate)) {
            DateFormat dateFormat = field.format();

            if (dateFormat == DateFormat.none) {
                throw new MappingException(String.format("Property %s is annotated with FieldType.%s but has no Dateformat defined",
                        getOwner().getType().getSimpleName() + "." + getName(), field.type().name()));
            }

            ElasticsearchDateConverter converter;

            if (dateFormat == DateFormat.custom) {
                String pattern = field.pattern();

                if (!StringUtils.hasLength(pattern)) {
                    throw new MappingException(String.format("Property %s is annotated with FieldType.%s and a custom format but has no pattern defined",
                            getOwner().getType().getSimpleName() + "." + getName(), field.type().name()));
                }
                converter = ElasticsearchDateConverter.of(pattern);
            } else {
                converter = ElasticsearchDateConverter.of(dateFormat);
            }

            propertyConverter = new ElasticsearchPersistentPropertyConverter() {
                final ElasticsearchDateConverter dateConverter = converter;
                @Override
                public String write(Object property) {
                    if (isTemporalAccessor && TemporalAccessor.class.isAssignableFrom(property.getClass())){
                        return dateConverter.format((TemporalAccessor) property);
                    } else if (isDate && Date.class.isAssignableFrom(property.getClass())) {
                        return dateConverter.format((Date) property);
                    } else {
                        return property.toString();
                    }
                }
                @SuppressWarnings("unchecked")
                @Override
                public Object read(String s) {
                    if (isTemporalAccessor) {
                        return dateConverter.parse(s, (Class<? extends TemporalAccessor>) actualType);
                    } else {
                        return dateConverter.parse(s);
                    }
                }
            };
        }
    }
}
