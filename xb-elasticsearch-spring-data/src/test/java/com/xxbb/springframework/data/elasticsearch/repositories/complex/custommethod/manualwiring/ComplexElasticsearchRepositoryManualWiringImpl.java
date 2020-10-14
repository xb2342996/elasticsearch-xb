package com.xxbb.springframework.data.elasticsearch.repositories.complex.custommethod.manualwiring;


import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;

public class ComplexElasticsearchRepositoryManualWiringImpl implements ComplexElasticsearchRepositoryCustom {

    private ElasticsearchOperations operations;

    public ComplexElasticsearchRepositoryManualWiringImpl(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public String doSomethingSpecial() {
        assert (operations.getElasticsearchConverter() != null);
        return "1 == 1";
    }
}
