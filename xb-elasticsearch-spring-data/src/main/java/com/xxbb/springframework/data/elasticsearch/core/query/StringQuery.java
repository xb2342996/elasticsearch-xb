package com.xxbb.springframework.data.elasticsearch.core.query;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class StringQuery extends AbstractQuery {
    private String source;

    public StringQuery(String source) {
        this.source = source;
    }

    public StringQuery(String source, Pageable pageable) {
        this.source = source;
        this.pageable = pageable;
    }

    public StringQuery(String source, Pageable pageable, Sort sort) {
        this.source = source;
        this.pageable = pageable;
        this.sort = sort;
    }

    public String getSource() {
        return source;
    }
}
