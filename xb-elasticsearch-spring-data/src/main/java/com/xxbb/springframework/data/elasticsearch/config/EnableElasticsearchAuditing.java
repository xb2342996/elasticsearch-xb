package com.xxbb.springframework.data.elasticsearch.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ElasticsearchAuditingRegistrar.class)
public @interface EnableElasticsearchAuditing {

    String auditorAwareRef() default "";

    boolean setDates() default true;

    boolean modifyOnCreate() default true;

    String dateTimeProviderRef() default "";
}
