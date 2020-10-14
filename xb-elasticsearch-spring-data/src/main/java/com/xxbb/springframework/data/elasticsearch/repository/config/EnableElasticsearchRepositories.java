package com.xxbb.springframework.data.elasticsearch.repository.config;

import com.xxbb.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactoryBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ElasticsearchReporsitoriesRegistrar.class)
public @interface EnableElasticsearchRepositories {
    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Filter[] includeFilters() default {};

    Filter[] excludeFilters() default {};

    String repositoryImplementationPostfix() default "Impl";

    String namedQueriesLocation() default "";

    Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND;

    Class<?> repositoryFactoryBeanClass() default ElasticsearchRepositoryFactoryBean.class;

    Class<?> repositoryBeanClass() default DefaultRepositoryBaseClass.class;

    String elasticsearchTemplateRef() default "elasticsearchTemplate";

    boolean considerNestedRepositories() default false;
}
