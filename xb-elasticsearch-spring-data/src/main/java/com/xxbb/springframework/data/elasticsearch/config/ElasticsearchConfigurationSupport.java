package com.xxbb.springframework.data.elasticsearch.config;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import com.xxbb.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class ElasticsearchConfigurationSupport {
    @Bean
    public ElasticsearchConverter elasticsearcEntityMapper(SimpleElasticsearchMappingContext elasticsearchMappingContext) {
        MappingElasticsearchConverter converter = new MappingElasticsearchConverter(elasticsearchMappingContext);
        converter.setConversions(elasticsearchCustomConversions());

        return converter;
    }

    @Bean
    public SimpleElasticsearchMappingContext elasticsearchMappingContext() {
        SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());
        mappingContext.setSimpleTypeHolder(elasticsearchCustomConversions().getSimpleTypeHolder());
        return mappingContext;
    }

    @Bean
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(Collections.emptyList());
    }

    protected Collection<String> getMappingBasePackages() {
        Package mappingBasePackage = getClass().getPackage();
        return Collections.singleton(mappingBasePackage == null ? null : mappingBasePackage.getName());
    }

    protected Set<Class<?>> getInitialEntitySet() {
        Set<Class<?>> initialEntitySet = new HashSet<>();
        for (String basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(scanForEntities(basePackage));
        }
        return initialEntitySet;
    }

    protected Set<Class<?>> scanForEntities(String basePackage) {
        if (!StringUtils.hasText(basePackage)) {
            return Collections.emptySet();
        }

        Set<Class<?>> initialEntitySet = new HashSet<>();

        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

        for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
            String beanClassName = candidate.getBeanClassName();

            if (beanClassName != null) {
                try {
                    initialEntitySet.add(
                            ClassUtils.forName(beanClassName, AbstractElasticsearchConfiguration.class.getClassLoader())
                    );
                } catch (ClassNotFoundException | LinkageError ignored) {
                }
            }
        }
        return initialEntitySet;
    }
}
