package com.xxbb.springframework.data.elasticsearch.junit.jupiter;

import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SpringDataElasticsearchExtension implements BeforeAllCallback, ParameterResolver, ContextCustomizerFactory {

    public static final String SPRING_DATA_ELASTICSEARCH_TEST_CLUSTER_URL = "http://172.16.68.128:9200";
//    public static final String SPRING_DATA_ELASTICSEARCH_TEST_CLUSTER_URL = "http://127.0.0.1:9200";
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringDataElasticsearchExtension.class);

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(SpringDataElasticsearchExtension.class.getName());
    private static final String STORE_KEY_CLUSTER_CONNECTION = ClusterConnection.class.getSimpleName();
    private static final String STORE_KEY_CLUSTER_CONNECTION_INFO = ClusterConnectionInfo.class.getSimpleName();

    private static final Lock initLock = new ReentrantLock();

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        initLock.lock();
        try {
            ExtensionContext.Store store = getStore(extensionContext);
            ClusterConnection clusterConnection = store.getOrComputeIfAbsent(STORE_KEY_CLUSTER_CONNECTION, key -> {
                LOGGER.debug("creating clusterConnection");
                return createClusterConnection();
            }, ClusterConnection.class);
            store.getOrComputeIfAbsent(STORE_KEY_CLUSTER_CONNECTION_INFO, key -> clusterConnection.getClusterConnectionInfo());
        } finally {
            initLock.unlock();
        }
    }

    private ExtensionContext.Store getStore(ExtensionContext extensionContext) {
        return extensionContext.getRoot().getStore(NAMESPACE);
    }

    private ClusterConnection createClusterConnection() {
        return new ClusterConnection(SPRING_DATA_ELASTICSEARCH_TEST_CLUSTER_URL);
    }


    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> parameterType = parameterContext.getParameter().getType();
        return parameterType.isAssignableFrom(ClusterConnectionInfo.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return getStore(extensionContext).get(STORE_KEY_CLUSTER_CONNECTION_INFO, ClusterConnectionInfo.class);
    }

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> aClass, List<ContextConfigurationAttributes> list) {
        return this::customizeContext;
    }

    private void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        ClusterConnectionInfo clusterConnectionInfo = ClusterConnection.clusterConnectionInfo();
        if (clusterConnectionInfo != null) {
            context.getBeanFactory().registerResolvableDependency(ClusterConnectionInfo.class, clusterConnectionInfo);
        }
    }
}
