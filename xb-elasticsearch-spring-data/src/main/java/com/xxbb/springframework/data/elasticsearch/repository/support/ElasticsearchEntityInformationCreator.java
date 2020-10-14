package com.xxbb.springframework.data.elasticsearch.repository.support;

public interface ElasticsearchEntityInformationCreator {
    <T, ID> ElasticsearchEntityInformation<T, ID> getEntityInformation(Class<T> domainClass);
}
