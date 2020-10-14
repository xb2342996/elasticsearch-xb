package com.xxbb.springframework.data.elasticsearch.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Highlight {
    HighlightParameters parameters() default @HighlightParameters;

    HighlightField[] fields();
}
