package com.xxbb.springframework.data.elasticsearch.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface MultiField {
    Field mainField();
    InnerField[] otherFields() default {};
}
