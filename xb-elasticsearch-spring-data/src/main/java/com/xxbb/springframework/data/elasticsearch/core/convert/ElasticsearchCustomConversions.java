package com.xxbb.springframework.data.elasticsearch.core.convert;

import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchSimpleTypes;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.util.NumberUtils;

import java.math.BigDecimal;
import java.util.*;

public class ElasticsearchCustomConversions extends CustomConversions {

    private static final StoreConversions STORE_CONVERSIONS;
    private static final List<Converter<?, ?>> STORE_CONVERTERS;

    public ElasticsearchCustomConversions(Collection<?> converters) {
        super(STORE_CONVERSIONS, converters);
    }

    static {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(Base64ToByteArrayConverter.INSTANCE);
        converters.add(ByteArrayToBase64Converter.INSTANCE);
        converters.add(StringToUUIDConverter.INSTANCE);
        converters.add(UUIDToStringConverter.INSTANCE);
        converters.add(DoubleToBigDecimalConverter.INSTANCE);
        converters.add(BigDecimalToDoubleConverter.INSTANCE);
        STORE_CONVERTERS = Collections.unmodifiableList(converters);
        STORE_CONVERSIONS = StoreConversions.of(ElasticsearchSimpleTypes.HOLDER, STORE_CONVERTERS);
    }

    @ReadingConverter
    static enum Base64ToByteArrayConverter implements Converter<String, byte[]> {
        INSTANCE;

        Base64ToByteArrayConverter() {
        }

        @Override
        public byte[] convert(String source) {
            return Base64.getDecoder().decode(source);
        }
    }

    @WritingConverter
    static enum ByteArrayToBase64Converter implements Converter<byte[], String> {
        INSTANCE;

        ByteArrayToBase64Converter() {
        }

        @Override
        public String convert(byte[] source) {
            return Base64.getEncoder().encodeToString(source);
        }
    }

    @ReadingConverter
    static enum DoubleToBigDecimalConverter implements Converter<Double, BigDecimal> {
        INSTANCE;

        DoubleToBigDecimalConverter() {
        }

        @Override
        public BigDecimal convert(Double source) {
            return (BigDecimal) NumberUtils.convertNumberToTargetClass(source, BigDecimal.class);
        }
    }

    @WritingConverter
    static enum BigDecimalToDoubleConverter implements Converter<BigDecimal, Double> {
        INSTANCE;

        BigDecimalToDoubleConverter() {
        }

        @Override
        public Double convert(BigDecimal source) {
            return (Double) NumberUtils.convertNumberToTargetClass(source, Double.class);
        }
    }

    @WritingConverter
    static enum UUIDToStringConverter implements Converter<UUID, String> {
        INSTANCE;

        UUIDToStringConverter() {
        }

        @Override
        public String convert(UUID source) {
            return source.toString();
        }
    }
    @ReadingConverter
    static enum StringToUUIDConverter implements Converter<String, UUID> {
        INSTANCE;

        StringToUUIDConverter() {
        }

        @Override
        public UUID convert(String source) {
            return UUID.fromString(source);
        }
    }
}
