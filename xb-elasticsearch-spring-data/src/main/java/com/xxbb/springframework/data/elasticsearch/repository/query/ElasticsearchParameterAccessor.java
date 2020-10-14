package com.xxbb.springframework.data.elasticsearch.repository.query;

import org.springframework.data.repository.query.ParameterAccessor;

public interface ElasticsearchParameterAccessor extends ParameterAccessor {
    Object[] getValues();
}
