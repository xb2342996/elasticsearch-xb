package com.xxbb.springframework.data.elasticsearch.core.index;

import com.xxbb.springframework.data.elasticsearch.annotations.DateFormat;
import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.annotations.Field;
import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.IndexOperations;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.core.query.IndexQuery;
import com.xxbb.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import com.xxbb.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.SpringIntegrationTest;
import com.xxbb.springframework.data.elasticsearch.utils.IndexBuilder;
import lombok.*;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.*;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@SpringIntegrationTest
@ContextConfiguration(classes = {ElasticsearchRestTemplateConfiguration.class})
public class MappingBuilderTest extends MappingContextBaseTest{

    @Autowired
    private ElasticsearchOperations operations;
    private IndexOperations indexOperations;

    @AfterEach
    @BeforeEach
    public void deleteIndices() {
        indexOperations = operations.indexOps(SimpleRecursiveEntity.class);
        indexOperations.delete();
        operations.indexOps(SampleInheritedEntity.class).delete();
        operations.indexOps(StockPrice.class).delete();
        operations.indexOps(User.class).delete();
        operations.indexOps(Group.class).delete();
        operations.indexOps(Book.class).delete();

    }

    @Test
    public void shouldNotFailOnCircularReference() {
        operations.indexOps(SimpleRecursiveEntity.class).create();

        indexOperations.putMapping(SimpleRecursiveEntity.class);
        indexOperations.refresh();
    }

    @Test
    public void testInfiniteLoopAvoidance() throws JSONException {
        String expected = "{\"properties\":{\"message\":{\"store\":true,\"" + "type\":\"text\",\"index\":false,"
                + "\"analyzer\":\"standard\"}}}";

        String mapping = getMappingBuilder().buildPropertyMapping(SampleTransientEntity.class);
        assertEquals(expected, mapping, false);
    }

    @Test
    public void shouldUseValueFromAnnotationType() throws JSONException {
        String expected = "{\"properties\":{\"price\":{\"type\":\"double\"}}}";

        String mapping = getMappingBuilder().buildPropertyMapping(StockPrice.class);

        assertEquals(expected, mapping, false);
    }

    @Test
    public void shouldAddStockPriceDocumentToIndex() {
        IndexOperations indexOps = operations.indexOps(StockPrice.class);
        indexOps.create();
        indexOps.putMapping(StockPrice.class);

        String symbol = "AU";
        double price = 2.34;
        String id = "abc";

        IndexCoordinates index = IndexCoordinates.of("test-index-stock-mapping-builder");
        operations.index(IndexBuilder.buildIndex(StockPrice.builder()
                .id(id)
                .symbol(symbol)
                .price(BigDecimal.valueOf(price))
                .build()), index);

        operations.indexOps(StockPrice.class).refresh();

//        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder
    }

    @Test
    public void shouldBuildMappingWithSuperclass() throws JSONException {
        String expected = "{\"properties\":{\"message\":{\"store\":true,\""
                + "type\":\"text\",\"index\":false,\"analyzer\":\"standard\"}" + ",\"createdDate\":{"
                + "\"type\":\"date\",\"index\":false}}}";

        String mapping = getMappingBuilder().buildPropertyMapping(SampleInheritedEntity.class);
        assertEquals(expected, mapping, false);
    }

    @Data
    @Document(indexName = "test-index-book-mapping-builder", replicas = 0, refreshInterval = "-1")
    static class Book {
        @Id
        private String id;
        private String name;
        @Field(type = FieldType.Object)
        private Author author;
        @Field(type = FieldType.Nested)
        private Map<Integer, Collection<String>> buckets = new HashMap<>();
    }

    @Data
    static class Author {
        @Nullable
        private String id;
        @Nullable
        private String name;
    }

    @Data
    @Document(indexName = "test-index-simple-recursive-mapping-builder", replicas = 0, refreshInterval = "-1")
    static class SimpleRecursiveEntity {
        @Nullable @Id
        private String id;
        @Nullable @Field(type = FieldType.Object, ignoreFields = {"circularObject"})
        private SimpleRecursiveEntity circularObject;
    }

    @Data
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Document(indexName = "test-index-stock-mapping-builder", replicas = 0, refreshInterval = "-1")
    static class StockPrice {
        @Id
        private String id;
        private String symbol;
        @Field(type = FieldType.Double)
        private BigDecimal price;
    }

    @Document(indexName = "test-index-user-builder")
    static class User {
        @Nullable @Id
        private String id;
        @Field(type = FieldType.Nested, ignoreFields = "users")
        private Set<Group> groups = new HashSet<>();
    }

    @Document(indexName = "test-index-field-mapping-parameters")
    static class Group {
        @Nullable
        @Id private String id;
        @Field(type = FieldType.Nested, ignoreFields = {"groups"}) private Set<User> users = new HashSet<>();
    }

    @Setter @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Document(indexName = "test-copy-to-mapping-builder", replicas = 0, refreshInterval = "-1")
    static class CopyToEntity {
        @Id
        private String id;
        @Field(type = FieldType.Keyword, copyTo = "name")
        private String firstName;
        @Field(type = FieldType.Keyword, copyTo = "name")
        private String lastName;
        @Field(type = FieldType.Keyword)
        private String name;
    }

    @Document(indexName = "test-index-recursive-mapping-mapping-builder", replicas = 0, refreshInterval = "-1")
    static class SampleTransientEntity {
        @Nullable @Id
        private String id;
        @Nullable @Field(type = FieldType.Text, index = false, store = true, analyzer = "standard")
        private String message;

        @Nullable @Transient
        private SampleTransientEntity.NestedEntity nested;

        @Nullable
        public String getId() {
            return id;
        }

        public void setId(@Nullable String id) {
            this.id = id;
        }

        @Nullable
        public String getMessage() {
            return message;
        }

        public void setMessage(@Nullable String message) {
            this.message = message;
        }

        static class NestedEntity {
            @Field
            private static NestedEntity someField = new NestedEntity();
            @Nullable @Field
            private Boolean something;

            public static NestedEntity getSomeField() {
                return someField;
            }

            public static void setSomeField(NestedEntity someField) {
                NestedEntity.someField = someField;
            }

            @Nullable
            public Boolean getSomething() {
                return something;
            }

            public void setSomething(@Nullable Boolean something) {
                this.something = something;
            }
        }


    }

    @Document(indexName = "test-index-sample-inherited-mapping-builder", replicas = 0, refreshInterval = "-1")
    static class SampleInheritedEntity extends AbstractInheritedEntity {
        @Nullable
        @Field(type = FieldType.Text, index = false, store = true, analyzer = "standard")
        private String message;

        @Nullable
        public String getMessage() {
            return message;
        }

        public void setMessage(@Nullable String message) {
            this.message = message;
        }
    }

    static class SampleInheritedEntityBuilder {
        private final SampleInheritedEntity result;

        public SampleInheritedEntityBuilder(String id) {
            result = new SampleInheritedEntity();
            result.setId(id);
        }

        public SampleInheritedEntityBuilder createdDate(Date createdDate) {
            result.setCreatedDate(createdDate);
            return this;
        }

        public SampleInheritedEntityBuilder mesage(String message) {
            result.setMessage(message);
            return this;
        }

        public SampleInheritedEntity build() {
            return result;
        }

        public IndexQuery buildIndex() {
            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setId(Objects.requireNonNull(result.getId()));
            indexQuery.setObject(result);
            return indexQuery;
        }
    }

    static class AbstractInheritedEntity {
        @Nullable @Id
        private String id;

        @Nullable
        @Field(type = FieldType.Date, format = DateFormat.date_time, index = false)
        private Date createdDate;

        @Nullable
        public String getId() {
            return id;
        }

        public void setId(@Nullable String id) {
            this.id = id;
        }

        @Nullable
        public Date getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(@Nullable Date createdDate) {
            this.createdDate = createdDate;
        }
    }

    static class ValueObject {
        private String value;

        public ValueObject(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    static class ValueDoc {
        @Nullable
        @Field(type = FieldType.Text)
        private ValueObject valueObject;
    }

    @Data
    @Document(indexName = "test-index-entity-with-seq-no-primary-term-mapping-builder")
    static class EntityWithSeqNoPrimaryTerm {
        @Field(type = FieldType.Object)
        private SeqNoPrimaryTerm seqNoPrimaryTerm;
    }
}
