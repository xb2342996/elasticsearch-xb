package com.xxbb.springframework.data.elasticsearch.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface HighlightField {

    String name() default "";

    HighlightParameters parameters() default @HighlightParameters;
}
