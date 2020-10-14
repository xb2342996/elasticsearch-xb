package com.xxbb.springframework.data.elasticsearch.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Documented
public @interface DynamicMapping {

    DynamicMappingValue value() default DynamicMappingValue.True;
}
