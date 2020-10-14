package com.xxbb.springframework.data.elasticsearch.repository.support;

import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;

public class ElasticsearchRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends RepositoryFactoryBeanSupport<T, S, ID> {

    @Nullable
    private ElasticsearchOperations operations;

    public ElasticsearchRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    public void setElasticsearchOperations(ElasticsearchOperations operations) {
        Assert.notNull(operations, "ElasticsearchOperations must not be null!");
        setMappingContext(operations.getElasticsearchConverter().getMappingContext());
        this.operations = operations;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.notNull(operations, "elasticsearchOperations must be configured!");
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        Assert.notNull(operations, "operations must not be null");
        return new ElasticsearchRepositoryFactory(operations);
    }
}
