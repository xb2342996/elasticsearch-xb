package com.xxbb.springframework.data.elasticsearch.repository.cdi;

import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.repository.cdi.CdiRepositoryBean;
import org.springframework.data.repository.cdi.CdiRepositoryExtensionSupport;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessBean;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ElasticsearchRepositoryExtension extends CdiRepositoryExtensionSupport {

    private final Map<Set<Annotation>, Bean<ElasticsearchOperations>> elasticsearchOperationsMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    <T> void processBean(@Observes ProcessBean<T> processBean) {
        Bean<T> bean = processBean.getBean();
        for (Type type : bean.getTypes()) {
            if (type instanceof Class<?> && ElasticsearchOperations.class.isAssignableFrom((Class<?>) type)) {
                elasticsearchOperationsMap.put(bean.getQualifiers(), ((Bean<ElasticsearchOperations>) bean));
            }
        }
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        for (Entry<Class<?>, Set<Annotation>> entry : getRepositoryTypes()) {
            Class<?> repositoryType = entry.getKey();
            Set<Annotation> qualifiers = entry.getValue();

            CdiRepositoryBean<?> repositoryBean = createRepositoryBean(repositoryType, qualifiers, beanManager);
            afterBeanDiscovery.addBean(repositoryBean);
            registerBean(repositoryBean);
        }
    }

    private <T> CdiRepositoryBean<T> createRepositoryBean(Class<T> repositoryType, Set<Annotation> qualifiers, BeanManager beanManager) {
        if (!this.elasticsearchOperationsMap.containsKey(qualifiers)) {
            throw new UnsatisfiedResolutionException(String.format("Unable to resolve a bean for '%s' with qualifiers %s.",
                    ElasticsearchOperations.class.getName(), qualifiers));
        }

        Bean<ElasticsearchOperations> elasticsearchOperationsBean = this.elasticsearchOperationsMap.get(qualifiers);

        return new ElasticsearchRepositoryBean<>(elasticsearchOperationsBean, qualifiers, repositoryType, beanManager, getCustomImplementationDetector());
    }
}
