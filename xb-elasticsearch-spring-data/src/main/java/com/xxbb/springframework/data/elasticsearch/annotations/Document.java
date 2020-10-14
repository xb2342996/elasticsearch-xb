package com.xxbb.springframework.data.elasticsearch.annotations;

import org.elasticsearch.index.VersionType;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Document {

    String indexName();

    boolean useServerConfigration() default false;

    short shards() default 1;

    short replicas() default 1;

    String refreshInterval() default "1s";

    String indexStoreType() default "fs";

    boolean createIndex() default true;

    VersionType versionType() default VersionType.EXTERNAL;
}
