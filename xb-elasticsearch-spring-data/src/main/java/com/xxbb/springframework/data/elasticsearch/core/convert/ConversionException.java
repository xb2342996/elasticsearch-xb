package com.xxbb.springframework.data.elasticsearch.core.convert;

public class ConversionException extends RuntimeException {
    public ConversionException() {
        super();
    }

    public ConversionException(Throwable cause) {
        super(cause);
    }

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
