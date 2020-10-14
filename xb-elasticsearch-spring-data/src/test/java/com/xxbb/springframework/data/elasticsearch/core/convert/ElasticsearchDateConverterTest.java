package com.xxbb.springframework.data.elasticsearch.core.convert;

import com.xxbb.springframework.data.elasticsearch.annotations.DateFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.*;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ElasticsearchDateConverterTest {
    @ParameterizedTest
    @EnumSource(DateFormat.class)
    public void shouldCreateConvertersForAllKnownFormats(DateFormat dateFormat) {
        if (dateFormat == DateFormat.none) {
            return;
        }
        String pattern = (dateFormat != DateFormat.custom) ? dateFormat.name() : "dd.MM.uuuu";
        ElasticsearchDateConverter converter = ElasticsearchDateConverter.of(pattern);

        assertThat(converter).isNotNull();
    }

    @Test
    public void shouldConvertTemporalAccessorToString() {
        LocalDate dateTime = LocalDate.of(2020, 9, 28);
        ElasticsearchDateConverter converter = ElasticsearchDateConverter.of(DateFormat.basic_date);
        String formatted = converter.format(dateTime);
        assertThat(formatted).isEqualTo("20200928");
    }

    @Test
    public void shouldParseTemporalAccessorFromString() {
        LocalDate localDate = LocalDate.of(2020, 9, 28);
        ElasticsearchDateConverter converter = ElasticsearchDateConverter.of(DateFormat.basic_date);
        LocalDate parsed = converter.parse("20200928", LocalDate.class);
        assertThat(parsed).isEqualTo(localDate);
    }

    @Test
    public void shouldConvertLegacyDateToString() {
        GregorianCalendar calendar = GregorianCalendar.from(ZonedDateTime.of(LocalDateTime.of(2020, 10, 1, 13, 42), ZoneId.of("UTC")));
        Date legacy = calendar.getTime();

        ElasticsearchDateConverter converter = ElasticsearchDateConverter.of(DateFormat.basic_date_time);
        String formatted = converter.format(legacy);

        assertThat(formatted).isEqualTo("20201001T134200.000Z");
    }

    @Test
    public void shouldParseLegacyDateFromString() {
        GregorianCalendar calendar = GregorianCalendar.from(ZonedDateTime.of(LocalDateTime.of(2020, 10, 1, 13, 42), ZoneId.of("UTC")));
        Date legacy = calendar.getTime();

        ElasticsearchDateConverter converter = ElasticsearchDateConverter.of(DateFormat.basic_date_time);
        Date parsed = converter.parse("20201001T134200.000Z");
        assertThat(parsed).isEqualTo(legacy);
    }

    @Test
    public void shouldParseEpochMillisString() {
        Instant instant = Instant.ofEpochMilli(1234568901234L);
        ElasticsearchDateConverter converter = ElasticsearchDateConverter.of(DateFormat.epoch_millis);
        Date date = converter.parse("1234568901234");
        assertThat(date.toInstant()).isEqualTo(instant);
    }

    @Test
    public void shouldConvertInstantToString() {
        Instant instant = Instant.ofEpochMilli(12345678901234L);
        ElasticsearchDateConverter converter = ElasticsearchDateConverter.of(DateFormat.epoch_millis);
        String formatted = converter.format(instant);
        assertThat(formatted).isEqualTo("12345678901234");
    }
}