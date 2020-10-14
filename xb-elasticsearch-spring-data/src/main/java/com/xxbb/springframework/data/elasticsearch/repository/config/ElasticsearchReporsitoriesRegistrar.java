package com.xxbb.springframework.data.elasticsearch.repository.config;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

class ElasticsearchReporsitoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableElasticsearchRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new ElasticsearchRepositoryConfigExtension();
    }
}
