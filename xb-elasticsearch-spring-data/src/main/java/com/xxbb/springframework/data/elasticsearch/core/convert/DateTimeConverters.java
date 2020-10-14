package com.xxbb.springframework.data.elasticsearch.core.convert;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.core.convert.converter.Converter;

import java.util.Date;

public class DateTimeConverters {
    private static DateTimeFormatter formatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    public enum JodaDateTimeConverter implements Converter<ReadableInstant, String> {
        INSTANCE;

        @Override
        public String convert(ReadableInstant source) {
            if (source == null) {
                return null;
            }
            return formatter.print(source);
        }
    }

    public enum JodaLocalDateTimeConverter implements Converter<LocalDateTime, String> {
        INSTANCE;

        @Override
        public String convert(LocalDateTime source) {
            if (source == null) {
                return null;
            }
            return formatter.print(source.toDateTime(DateTimeZone.UTC));
        }
    }
    public enum JavaDateConverter implements Converter<Date, String> {
        INSTANCE;

        @Override
        public String convert(Date source) {
            if (source == null) {
                return null;
            }
            return formatter.print(source.getTime());
        }
    }
}
