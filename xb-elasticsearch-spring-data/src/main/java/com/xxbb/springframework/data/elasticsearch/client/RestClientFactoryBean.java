package com.xxbb.springframework.data.elasticsearch.client;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.net.URL;
import java.util.ArrayList;

public class RestClientFactoryBean implements FactoryBean<RestHighLevelClient>, InitializingBean, DisposableBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(RestClientFactoryBean.class);

    private @Nullable RestHighLevelClient client;
    private String hosts = "http://localhost:9200";
    private final String COMMA = ",";

    @Override
    public void destroy() {
        try {
            LOGGER.info("Closing elastic search client");
            if (client != null) {
                client.close();
            }
        } catch (final Exception e) {
            LOGGER.error("Error closing Elasticsearh client:", e);
        }
    }

    @Override
    public RestHighLevelClient getObject() {
        if (client == null) {
            throw new FactoryBeanNotInitializedException();
        }
        return client;
    }

    @Override
    public Class<?> getObjectType() {
        return RestHighLevelClient.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        buildClient();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    protected void buildClient() throws Exception {
        Assert.hasText(hosts, "[Assertion Failed] at least one host");
        ArrayList<HttpHost> httpHosts = new ArrayList<>();
        for (String host : hosts.split(COMMA)) {
            URL url = new URL(host);
            httpHosts.add(new HttpHost(url.getHost(), url.getPort(), url.getProtocol()));
        }
        client = new RestHighLevelClient(RestClient.builder(httpHosts.toArray(new HttpHost[httpHosts.size()])));
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }
}
