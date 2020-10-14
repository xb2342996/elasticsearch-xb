package com.xxbb.springframework.data.elasticsearch.core.index;

import com.xxbb.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.util.Lazy;

abstract class MappingContextBaseTest {
    protected final Lazy<ElasticsearchConverter> elasticsearchConverter = Lazy.of(this::setupElasticsearchConverter);

    private ElasticsearchConverter setupElasticsearchConverter() {
        return new MappingElasticsearchConverter(setupMappingContext());
    }

    private SimpleElasticsearchMappingContext setupMappingContext() {
        SimpleElasticsearchMappingContext mappingContext = new ElasticsearchConfigurationSupport().elasticsearchMappingContext();
        mappingContext.initialize();
        return mappingContext;
    }

    final MappingBuilder getMappingBuilder() {
        return new MappingBuilder(elasticsearchConverter.get());
    }

}
