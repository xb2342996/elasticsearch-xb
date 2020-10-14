package com.xxbb.springframework.data.elasticsearch.repositories.complex.custommethod.autowiring;

import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.beans.factory.annotation.Autowired;

public class ComplexElasticsearchRepositoryImpl implements ComplexElasticsearchRepositoryCustom {
    @Autowired
    private ElasticsearchOperations operations;

    @Override
    public String doSomethingSpecial() {
        assert (operations.getElasticsearchConverter() != null);
        return "1 == 1";
    }
}
