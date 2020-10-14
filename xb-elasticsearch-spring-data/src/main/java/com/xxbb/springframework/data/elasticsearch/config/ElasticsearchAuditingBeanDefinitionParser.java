package com.xxbb.springframework.data.elasticsearch.config;

import com.xxbb.springframework.data.elasticsearch.core.event.AuditingEntityCallback;
import com.xxbb.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.auditing.config.IsNewAwareAuditingHandlerBeanDefinitionParser;
import org.springframework.data.config.ParsingUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class ElasticsearchAuditingBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static String MAPPING_CONTEXT_BEAN_NAME = "simpleElasticsearchMappingContext";

    @Override
    protected Class<?> getBeanClass(Element element) {
        return AuditingEntityCallback.class;
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String mappingContextRef = element.getAttribute("mapping-context-ref");

        if (!StringUtils.hasText(mappingContextRef)) {
            BeanDefinitionRegistry registry = parserContext.getRegistry();

            if (!registry.containsBeanDefinition(MAPPING_CONTEXT_BEAN_NAME)) {
                registry.registerBeanDefinition(MAPPING_CONTEXT_BEAN_NAME, new RootBeanDefinition(SimpleElasticsearchMappingContext.class));
            }
            mappingContextRef = MAPPING_CONTEXT_BEAN_NAME;
        }

        IsNewAwareAuditingHandlerBeanDefinitionParser parser = new IsNewAwareAuditingHandlerBeanDefinitionParser(mappingContextRef);
        parser.parse(element, parserContext);

        AbstractBeanDefinition isNewAwareAuditingHandler = ParsingUtils.getObjectFactoryBeanDefinition(parser.getResolvedBeanName(), parserContext.extractSource(element));
        builder.addConstructorArgValue(isNewAwareAuditingHandler);

    }

}
