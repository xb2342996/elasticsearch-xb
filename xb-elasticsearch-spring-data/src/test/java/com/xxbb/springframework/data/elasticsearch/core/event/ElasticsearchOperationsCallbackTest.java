package com.xxbb.springframework.data.elasticsearch.core.event;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.IndexOperations;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

abstract class ElasticsearchOperationsCallbackTest {
    @Autowired private ElasticsearchOperations operations;

    @Configuration
    static class Config {
        @Component
        static class SampleEntityBeforConvertCallback implements BeforeConvertCallback<SampleEntity> {

            @Override
            public SampleEntity onBeforeConvert(SampleEntity entity, IndexCoordinates index) {
                entity.setText("converted");
                return entity;
            }
        }
    }
    @BeforeEach
    void setup() {
        IndexOperations indexOperations = operations.indexOps(SampleEntity.class);
        indexOperations.delete();
        indexOperations.create();
        indexOperations.putMapping(SampleEntity.class);
    }

    @AfterEach
    void tearDown() {
        IndexOperations indexOperations = operations.indexOps(SampleEntity.class);
        indexOperations.delete();
    }

    @Test
    public void shouldCallbackBeforeConvertCallback() {
        SampleEntity sample = new SampleEntity("1", "test");
        SampleEntity saved = operations.save(sample);
        assertThat(saved.getText()).isEqualTo("converted");
    }

    @Document(indexName = "test-operations-callback")
    static class SampleEntity {
        @Id
        private String id;
        private String text;

        public SampleEntity(String id, String text) {
            this.id = id;
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
