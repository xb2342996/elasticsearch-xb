package com.xxbb.springframework.data.elasticsearch.repository.cdi;

import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactory;
import org.springframework.data.repository.cdi.CdiRepositoryBean;
import org.springframework.data.repository.config.CustomRepositoryImplementationDetector;
import org.springframework.util.Assert;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

public class ElasticsearchRepositoryBean<T> extends CdiRepositoryBean<T> {

    private final Bean<ElasticsearchOperations> elasticsearchOperationsBean;

    public ElasticsearchRepositoryBean(Bean<ElasticsearchOperations> operations, Set<Annotation> qualifiers,
                                       Class<T> repositoryType, BeanManager beanManager, CustomRepositoryImplementationDetector detector) {
        super(qualifiers, repositoryType, beanManager, Optional.of(detector));
        Assert.notNull(operations, "operations must not be null");
        this.elasticsearchOperationsBean = operations;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return elasticsearchOperationsBean.getScope();
    }

    @Override
    protected T create(CreationalContext<T> creationalContext, Class<T> repositoryType) {
        ElasticsearchOperations operations = getDependencyInstance(elasticsearchOperationsBean, ElasticsearchOperations.class);
        return create(() -> new ElasticsearchRepositoryFactory(operations), repositoryType);
    }
}
