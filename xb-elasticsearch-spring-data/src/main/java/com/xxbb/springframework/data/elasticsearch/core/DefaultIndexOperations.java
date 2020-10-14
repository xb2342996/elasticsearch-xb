package com.xxbb.springframework.data.elasticsearch.core;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxbb.springframework.data.elasticsearch.UncategorizedElasticsearchException;
import com.xxbb.springframework.data.elasticsearch.core.document.Document;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.client.Requests.*;

public class DefaultIndexOperations extends AbstractDefaultIndexOperations implements IndexOperations {

    private ElasticsearchRestTemplate restTemplate;

    public DefaultIndexOperations(ElasticsearchRestTemplate restTemplate, Class<?> boundClass) {
        super(restTemplate.getElasticsearchConverter(), boundClass);
        this.restTemplate = restTemplate;
    }

    public DefaultIndexOperations(ElasticsearchRestTemplate restTemplate, IndexCoordinates boundIndex) {
        super(restTemplate.getElasticsearchConverter(), boundIndex);
        this.restTemplate = restTemplate;
    }

    @Override
    protected boolean doCreate(String indexName,@Nullable Document settings) {
        CreateIndexRequest request = requestFactory.createIndexRequest(indexName, settings);
        return restTemplate.execute(client -> client.indices().create(request, RequestOptions.DEFAULT).isAcknowledged());
    }

    @Override
    protected boolean doDelete(String indexName) {
        Assert.notNull(indexName, "No index defined for delete operation");
        if (doExists(indexName)) {
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            return restTemplate.execute(client -> client.indices().delete(request, RequestOptions.DEFAULT).isAcknowledged());
        }
        return false;
    }

    @Override
    protected boolean doExists(String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return restTemplate.execute(client -> client.indices().exists(request, RequestOptions.DEFAULT));
    }

    @Override
    protected void doRefresh(IndexCoordinates indexCoordinates) {
        Assert.notNull(indexCoordinates, "No index defined for refresh()");
        restTemplate.execute(client -> client.indices().refresh(refreshRequest(indexCoordinates.getIndexNames()), RequestOptions.DEFAULT));
    }

    @Override
    protected boolean doPutMapping(IndexCoordinates index, Document mapping) {
        Assert.notNull(index, "No index defined for putMapping");
        PutMappingRequest request = requestFactory.putMappingRequest(index, mapping);
        return restTemplate.execute(client -> client.indices().putMapping(request, RequestOptions.DEFAULT).isAcknowledged());
    }

    @Override
    protected Map<String, Object> doGetMapping(IndexCoordinates index) {
        Assert.notNull(index, "No index defined for getMapping");
        return restTemplate.execute(client -> {
            RestClient restClient = client.getLowLevelClient();
            Request request = new Request("GET", "/"+index.getIndexName()+"/_mapping");
            Response response = restClient.performRequest(request);
            return convertMappingResponse(EntityUtils.toString(response.getEntity()));
        });
    }

    @Override
    protected Map<String, Object> doGetSettings(String indexName, boolean includeDefaults) {
        Assert.notNull(indexName, "No index defined for getSettings");
        GetSettingsRequest request = new GetSettingsRequest().indices(indexName).includeDefaults(includeDefaults);
        GetSettingsResponse response = restTemplate.execute(client -> client.indices().getSettings(request, RequestOptions.DEFAULT));
        return convertSettingsResponseToMap(response, indexName);
    }

    private Map<String, Object> convertMappingResponse(String mappingResponse) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> result = null;
            JsonNode node = mapper.readTree(mappingResponse);

            node = node.findValue("mappings");
            result = mapper.readValue(mapper.writeValueAsString(node), HashMap.class);

            return result;
        } catch (IOException e) {
            throw new UncategorizedElasticsearchException("Could not map Alias Response" + mappingResponse, e);
        }
    }
}
