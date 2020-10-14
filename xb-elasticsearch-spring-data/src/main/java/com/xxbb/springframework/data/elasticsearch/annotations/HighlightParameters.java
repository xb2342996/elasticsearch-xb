package com.xxbb.springframework.data.elasticsearch.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface HighlightParameters {
    String boundaryChars() default "";

    int boundaryMaxScan() default -1;

    String boundaryScanner() default "";

    String boundaryScannerLocale() default "";

    String encoder() default "";

    boolean forceSource() default false;

    String fragmenter() default "";

    int fragmentOffset() default -1;

    int fragmentSize() default -1;

    String[] matchFields() default {};

    int noMatchSize() default -1;

    int numberOfFragment() default -1;

    String order() default "";

    int pharseLimit() default -1;

    String[] preTags() default {};

    String[] postTags() default {};

    boolean requiredFieldMatch() default true;

    String tagsSchema() default "";

    String type() default "";
}
