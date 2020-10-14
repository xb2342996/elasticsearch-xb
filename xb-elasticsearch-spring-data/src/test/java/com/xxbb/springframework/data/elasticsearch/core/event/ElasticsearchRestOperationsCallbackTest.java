package com.xxbb.springframework.data.elasticsearch.core.event;

import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.SpringIntegrationTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@SpringIntegrationTest
@ContextConfiguration(classes = {ElasticsearchRestTemplateConfiguration.class, ElasticsearchOperationsCallbackTest.Config.class})
public class ElasticsearchRestOperationsCallbackTest extends ElasticsearchOperationsCallbackTest{
}
