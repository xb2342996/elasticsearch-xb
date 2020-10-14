package com.xxbb.springframework.data.elasticsearch.annotations;

import org.springframework.data.annotation.ReadOnlyProperty;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
@ReadOnlyProperty
public @interface Score {
}
