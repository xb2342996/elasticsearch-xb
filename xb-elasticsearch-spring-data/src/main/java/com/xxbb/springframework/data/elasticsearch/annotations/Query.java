package com.xxbb.springframework.data.elasticsearch.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {

    String value() default "";
    String name() default "";
}
