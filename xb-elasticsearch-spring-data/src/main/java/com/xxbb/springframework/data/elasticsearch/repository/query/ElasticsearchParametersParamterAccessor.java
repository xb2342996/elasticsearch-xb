package com.xxbb.springframework.data.elasticsearch.repository.query;

import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;

import java.util.Arrays;
import java.util.List;

public class ElasticsearchParametersParamterAccessor extends ParametersParameterAccessor implements ElasticsearchParameterAccessor {

    private final List<Object> values;

    public ElasticsearchParametersParamterAccessor(ElasticsearchQueryMethod method, Object[] values) {
        super(method.getParameters(), values);
        this.values = Arrays.asList(values);
    }

    @Override
    public Object[] getValues() {
        return values.toArray();
    }
}
