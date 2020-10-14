package com.xxbb.springframework.data.elasticsearch.junit.jupiter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(SpringDataElasticsearchExtension.class)
@ExtendWith(SpringExtension.class)
@Tag(Tags.INTEGRATION_TEST)
public @interface SpringIntegrationTest {
}
