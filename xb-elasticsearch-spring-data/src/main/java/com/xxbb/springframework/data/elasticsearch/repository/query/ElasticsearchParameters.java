package com.xxbb.springframework.data.elasticsearch.repository.query;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;

import java.lang.reflect.Method;
import java.util.List;

public class ElasticsearchParameters extends Parameters<ElasticsearchParameters, ElasticsearchParameters.ElasticsearchParameter> {

    public ElasticsearchParameters(Method method) {
        super(method);
    }

    public ElasticsearchParameters(List<ElasticsearchParameter> originals) {
        super(originals);
    }

    @Override
    protected ElasticsearchParameter createParameter(MethodParameter parameter) {
        return new ElasticsearchParameter(parameter);
    }

    @Override
    protected ElasticsearchParameters createFrom(List<ElasticsearchParameter> parameters) {
        return new ElasticsearchParameters(parameters);
    }

    class ElasticsearchParameter extends Parameter {
        protected ElasticsearchParameter(MethodParameter parameter) {
            super(parameter);
        }
    }
}
