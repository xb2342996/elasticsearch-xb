package com.xxbb.springframework.data.elasticsearch.config.notnested;

import com.xxbb.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SampleElasticsearchRepository extends ElasticsearchRepository<EnableElasticsearchRepositoryTests.SampleEntity, String> {

    long deleteSampleEntityById(String id);

    List<EnableElasticsearchRepositoryTests.SampleEntity> deleteByAvailable(boolean available);

    List<EnableElasticsearchRepositoryTests.SampleEntity> deleteByMessage(String message);

    void deleteByType(String type);
}
