package com.xxbb.springframework.data.elasticsearch.annotations;

import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InnerField {
}
