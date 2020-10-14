package com.xxbb.springframework.data.elasticsearch.core.convert;

import com.xxbb.springframework.data.elasticsearch.annotations.DateFormat;
import org.elasticsearch.common.time.DateFormatter;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

final public class ElasticsearchDateConverter {
    private static final ConcurrentHashMap<String, ElasticsearchDateConverter> converters = new ConcurrentHashMap<>();

    private final DateFormatter dateFormatter;

    public static ElasticsearchDateConverter of(DateFormat dateFormat) {
        Assert.notNull(dateFormat, "dateFormat must not be null");
        return of(dateFormat.name());
    }

    public static ElasticsearchDateConverter of(String pattern) {
        Assert.notNull(pattern, "pattern must not be null");
        return converters.computeIfAbsent(pattern, p -> new ElasticsearchDateConverter((DateFormatter.forPattern(p))));
    }

    public ElasticsearchDateConverter(DateFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public String format(TemporalAccessor accessor) {
        Assert.notNull(accessor, "accessor must not be null");
        return dateFormatter.format(accessor);
    }

    public String format(Date date) {
        Assert.notNull(date, "date must not be null");
        return dateFormatter.format(Instant.ofEpochMilli(date.getTime()));
    }

    public <T extends TemporalAccessor> T parse(String input, Class<T> type) {
        TemporalAccessor accessor = dateFormatter.parse(input);

        try {
            Method method = type.getMethod("from", TemporalAccessor.class);
            Object o = method.invoke(null, accessor);
            return type.cast(o);
        } catch (NoSuchMethodException e) {
            throw new ConversionException("no 'from' factory method found in class" + type.getName());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ConversionException("could not create object of class " + type.getName(), e);
        }
    }

    public Date parse(String input) {
        return new Date(Instant.from(dateFormatter.parse(input)).toEpochMilli());
    }
}
