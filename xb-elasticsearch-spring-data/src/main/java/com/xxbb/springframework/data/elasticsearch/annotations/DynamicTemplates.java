package com.xxbb.springframework.data.elasticsearch.annotations;

import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DynamicTemplates {
    String mappingPath() default "";
}
