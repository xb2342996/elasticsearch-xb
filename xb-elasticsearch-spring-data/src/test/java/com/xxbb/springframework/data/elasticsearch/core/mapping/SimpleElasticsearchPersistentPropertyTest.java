package com.xxbb.springframework.data.elasticsearch.core.mapping;

import com.xxbb.springframework.data.elasticsearch.annotations.DateFormat;
import com.xxbb.springframework.data.elasticsearch.annotations.Field;
import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import com.xxbb.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;
import lombok.Data;
import org.junit.Test;
import org.springframework.data.mapping.MappingException;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


public class SimpleElasticsearchPersistentPropertyTest {

    private final SimpleElasticsearchMappingContext context = new SimpleElasticsearchMappingContext();

    @Test
    public void fieldAnnotationWithNameSetsFieldname() {
        SimpleElasticsearchPersistentEntity<?> entity = context.getRequiredPersistentEntity(FieldNameProperty.class);
        ElasticsearchPersistentProperty persistentProperty = entity.getPersistentProperty("fieldProperty");

        assertThat(persistentProperty).isNotNull();
        assertThat(persistentProperty.getFieldName()).isEqualTo("by-name");
    }

    @Test
    public void fieldAnnotationWithValueSetsFieldname() {
        SimpleElasticsearchPersistentEntity<?> entity = context.getRequiredPersistentEntity(FieldValueProperty.class);
        ElasticsearchPersistentProperty persistentProperty = entity.getPersistentProperty("fieldProperty");
        assertThat(persistentProperty).isNotNull();
        assertThat(persistentProperty.getFieldName()).isEqualTo("by-value");
    }

    @Test
    public void shouldSetPropertyConverters() {
        SimpleElasticsearchPersistentEntity<?> entity = context.getRequiredPersistentEntity(DateProperty.class);
        ElasticsearchPersistentProperty persistentProperty = entity.getRequiredPersistentProperty("localDate");
        assertThat(persistentProperty.hasPropertyConverter()).isTrue();
        assertThat(persistentProperty.getPropertyConverter()).isNotNull();

        persistentProperty = entity.getRequiredPersistentProperty("localDateTime");
        assertThat(persistentProperty.hasPropertyConverter()).isTrue();
        assertThat(persistentProperty.getPropertyConverter()).isNotNull();

        persistentProperty = entity.getRequiredPersistentProperty("legacyDate");
        assertThat(persistentProperty.hasPropertyConverter()).isTrue();
        assertThat(persistentProperty.getPropertyConverter()).isNotNull();

        persistentProperty = entity.getRequiredPersistentProperty("localDates");
        assertThat(persistentProperty.hasPropertyConverter()).isTrue();
        assertThat(persistentProperty.getPropertyConverter()).isNotNull();
    }

    @Test
    public void shouldConvertFromLocalDate() {
        SimpleElasticsearchPersistentEntity<?> entity = context.getRequiredPersistentEntity(DateProperty.class);
        ElasticsearchPersistentProperty property = entity.getRequiredPersistentProperty("localDate");
        LocalDate date = LocalDate.of(2020, 9, 28);
        String converted = property.getPropertyConverter().write(date);

        assertThat(converted).isEqualTo("28.09.2020");
    }

    @Test
    public void shouldConvertToLocalDate() {
        SimpleElasticsearchPersistentEntity<?> entity = context.getRequiredPersistentEntity(DateProperty.class);
        ElasticsearchPersistentProperty property = entity.getRequiredPersistentProperty("localDate");
        Object converted = property.getPropertyConverter().read("28.09.2020");
        assertThat(converted).isInstanceOf(LocalDate.class);
        assertThat(converted).isEqualTo(LocalDate.of(2020, 9,28));
    }

    @Test
    public void shouldConvertFromLegacyDate() {
        SimpleElasticsearchPersistentEntity<?> entity = context.getRequiredPersistentEntity(DateProperty.class);
        ElasticsearchPersistentProperty property = entity.getRequiredPersistentProperty("legacyDate");
        GregorianCalendar calendar = GregorianCalendar.from(ZonedDateTime.of(LocalDateTime.of(2030, 12, 22, 11, 43), ZoneId.of("UTC")));
        Date lagecy = calendar.getTime();
        String converted = property.getPropertyConverter().write(lagecy);
        assertThat(converted).isEqualTo("20301222T114300.000Z");
    }

    @Test
    public void shouldConvertToLegacyDate() {
        SimpleElasticsearchPersistentEntity<?> entity = context.getRequiredPersistentEntity(DateProperty.class);
        ElasticsearchPersistentProperty property = entity.getRequiredPersistentProperty("legacyDate");
        Object converted = property.getPropertyConverter().read("20301222T114300.000Z");
        assertThat(converted).isInstanceOf(Date.class);
        assertThat(converted).isEqualTo(GregorianCalendar.from(ZonedDateTime.of(LocalDateTime.of(2030, 12,22,11,43),ZoneId.of("UTC"))).getTime());
    }

    @Test
    public void shouldReportSeqNoPrimaryTermPropertyWhenTheTypeIsSeqNoPrimaryTerm() {
        SimpleElasticsearchPersistentEntity<?> entity = context.getRequiredPersistentEntity(SeqNoPrimaryTermProperty.class);
        ElasticsearchPersistentProperty property = entity.getRequiredPersistentProperty("seqNoPrimaryTerm");
        assertThat(property.isSeqNoPrimaryTermProperty()).isTrue();
    }

    @Test
    public void shouldNotReportSeqNoPrimaryTermPropertyWhenTheTypeIsNotSeqNoPrimaryTerm() {
        SimpleElasticsearchPersistentEntity<?> entity = context.getRequiredPersistentEntity(SeqNoPrimaryTermProperty.class);
        ElasticsearchPersistentProperty property = entity.getRequiredPersistentProperty("string");
        assertThat(property.isSeqNoPrimaryTermProperty()).isFalse();
    }

    @Test
    public void seqNoPrimaryTermPropertyShouldNotBeWritable() {
        SimpleElasticsearchPersistentEntity<?> entity = context.getRequiredPersistentEntity(SeqNoPrimaryTermProperty.class);
        ElasticsearchPersistentProperty property = entity.getRequiredPersistentProperty("seqNoPrimaryTerm");
        assertThat(property.isWritable()).isFalse();
    }

    @Test
    public void seqNoPrimaryTermPropertyShouldNotBereadable() {
        SimpleElasticsearchPersistentEntity<?> entity = context.getRequiredPersistentEntity(SeqNoPrimaryTermProperty.class);
        ElasticsearchPersistentProperty property = entity.getRequiredPersistentProperty("seqNoPrimaryTerm");
        assertThat(property.isReadable()).isFalse();
    }

    @Test
    public void shouldRequireFormatForDateField() {
        assertThatExceptionOfType(MappingException.class)
                .isThrownBy(() -> context.getRequiredPersistentEntity(DateFieldWithNoFormat.class))
                .withMessageContaining("date");
    }

    @Test
    public void shouldRequireFormatForDateNanosField() {
        assertThatExceptionOfType(MappingException.class)
                .isThrownBy(() -> context.getRequiredPersistentEntity(DateNanosFieldWithNoFormat.class))
                .withMessageContaining("date");
    }

    @Test
    public void shouldRequirePatternForCustomDateFormat() {
        assertThatExceptionOfType(MappingException.class)
                .isThrownBy(() -> context.getRequiredPersistentEntity(DateFieldWithCustomFormatAndNoPattern.class))
                .withMessageContaining("pattern");
    }

    static class FieldNameProperty {
        @Nullable
        @Field(name = "by-name")
        String fieldProperty;
    }

    static class FieldValueProperty {
        @Nullable
        @Field(name = "by-value")
        String fieldProperty;
    }

    static class DateProperty{
        @Nullable
        @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "dd.MM.uuuu")
        LocalDate localDate;
        @Nullable
        @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
        LocalDateTime localDateTime;

        @Nullable
        @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
        Date legacyDate;

        @Nullable
        @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "dd.MM.uuuu")
        List<LocalDate> localDates;
    }

    @Data
    static class SeqNoPrimaryTermProperty {
        SeqNoPrimaryTerm seqNoPrimaryTerm;
        String string;
    }

    @Data
    static class DateFieldWithNoFormat {
        @Field(type = FieldType.Date)
        LocalDateTime dateTime;
    }

    @Data
    static class DateFieldWithCustomFormatAndNoPattern {
        @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "")
        LocalDateTime dateTime;
    }

    @Data
    static class DateNanosFieldWithNoFormat {
        @Field(type = FieldType.Date_Nanos)
        LocalDateTime dateTime;
    }
}