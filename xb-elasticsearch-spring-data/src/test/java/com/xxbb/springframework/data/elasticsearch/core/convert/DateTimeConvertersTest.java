package com.xxbb.springframework.data.elasticsearch.core.convert;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;


public class DateTimeConvertersTest {
    @Test
    public void testJodaDateTimeConverterWithNullValue() {
        assertThat(DateTimeConverters.JodaDateTimeConverter.INSTANCE.convert(null)).isNull();
    }

    @Test
    public void testJodaDateTimeConverter() {
        DateTime date = new DateTime(2020,1,23,4, 55, 0, DateTimeZone.UTC);
        assertThat(DateTimeConverters.JodaDateTimeConverter.INSTANCE.convert(date)).isEqualTo("2020-01-23T04:55:00.000Z");
    }

    @Test
    public void testJodaLocalDateTimeConverterWithNullValue() {
        assertThat(DateTimeConverters.JodaLocalDateTimeConverter.INSTANCE.convert(null)).isNull();
    }

    @Test
    public void testJodaLocalDateTimeConverter() {
        LocalDateTime date = new LocalDateTime(new DateTime(2020, 1, 23, 4, 55, 6, DateTimeZone.UTC).getMillis(), DateTimeZone.UTC);
        assertThat(DateTimeConverters.JodaLocalDateTimeConverter.INSTANCE.convert(date)).isEqualTo("2020-01-23T04:55:06.000Z");
    }

    @Test
    public void testJavaLocalDateTimeConverterWithNullValue() {
        assertThat(DateTimeConverters.JavaDateConverter.INSTANCE.convert(null)).isNull();
    }

    @Test
    public void testJavaLocalDateTimeConverter() {
        DateTime dateTime = new DateTime(2020, 1, 23, 4, 55, 6, DateTimeZone.UTC);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(dateTime.getMillis());
        assertThat(DateTimeConverters.JavaDateConverter.INSTANCE.convert(calendar.getTime())).isEqualTo("2020-01-23T04:55:06.000Z");
    }
}