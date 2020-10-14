package com.xxbb.springframework.data.elasticsearch;

import org.springframework.dao.UncategorizedDataAccessException;

public class UncategorizedElasticsearchException extends UncategorizedDataAccessException {
    public UncategorizedElasticsearchException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
