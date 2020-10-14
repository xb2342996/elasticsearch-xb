package com.xxbb.springframework.data.elasticsearch.repository.query;

import com.xxbb.springframework.data.elasticsearch.annotations.*;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import com.xxbb.springframework.data.elasticsearch.core.query.StringQuery;
import com.xxbb.springframework.data.elasticsearch.repository.query.ElasticsearchParametersParamterAccessor;
import com.xxbb.springframework.data.elasticsearch.repository.query.ElasticsearchQueryMethod;
import com.xxbb.springframework.data.elasticsearch.repository.query.ElasticsearchStringQuery;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.annotation.Id;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ElasticsearchStringQueryUnitTests {
    @Mock
    ElasticsearchOperations operations;

    ElasticsearchConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext());
    }

    @Test
    public void shouldReplaceParametersCorrectly() throws NoSuchMethodException {
        com.xxbb.springframework.data.elasticsearch.core.query.Query query = createQuery("findByName", "Luke");
        assertThat(query).isInstanceOf(StringQuery.class);
        assertThat(((StringQuery) query).getSource()).isEqualTo("{ 'bool' : { 'must' : { 'term' : { 'name' : 'Luke' } } } }");
    }

    @Test
    public void shouldReplaceRepeatedParametersCorrectly() throws NoSuchMethodException {
        com.xxbb.springframework.data.elasticsearch.core.query.Query query = createQuery("findWithRepeatedPlaceholder",
                "zero",
                "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven");

        assertThat(query).isInstanceOf(StringQuery.class);
        assertThat(((StringQuery) query).getSource()).isEqualTo("name:(zero, eleven, one, two, three, four, five, six, seven, eight, nine, ten, eleven, zero, one)");
    }

    private com.xxbb.springframework.data.elasticsearch.core.query.Query createQuery(String methodName, String... args) throws NoSuchMethodException {
        Class<?>[] argTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        ElasticsearchQueryMethod queryMethod = getQueryMethod(methodName, argTypes);
        ElasticsearchStringQuery elasticsearchStringQuery = queryForMethod(queryMethod);
        return elasticsearchStringQuery.createQuery(new ElasticsearchParametersParamterAccessor(queryMethod, args));
    }

    private ElasticsearchStringQuery queryForMethod(ElasticsearchQueryMethod queryMethod) {
        return new ElasticsearchStringQuery(queryMethod, operations, queryMethod.getAnnotatedQuery());
    }

    private ElasticsearchQueryMethod getQueryMethod(String name, Class<?>... parameters) throws NoSuchMethodException {
        Method method = SampleRepository.class.getMethod(name, parameters);
        return new ElasticsearchQueryMethod(method, new DefaultRepositoryMetadata(SampleRepository.class),
                new SpelAwareProxyProjectionFactory(), converter.getMappingContext());
    }

    private interface SampleRepository extends Repository<Person, String> {
        @Query("{ 'bool' : { 'must' : { 'term' : { 'name' : '?0' } } } }")
        Person findByName(String name);

        @Query(value = "name:(?0, ?11, ?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?0, ?1)")
        Person findWithRepeatedPlaceholder(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5,
                                           String arg6, String arg7, String arg8, String arg9, String arg10, String arg11);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Document(indexName = "test-index-person-query", replicas = 0, refreshInterval = "-1")
    static class Person {
        @Nullable @Id
        private String id;
        @Nullable
        private String name;
        @Nullable
        @Field(type = FieldType.Nested)
        private List<Car> car;
        @Field(type = FieldType.Nested, includeInParent = true)
        private List<Book> books;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Document(indexName = "test-index-book-query", replicas = 0, refreshInterval = "-1")
    static class Book {
        @Id
        private String id;
        private String name;
        @Field(type = FieldType.Object)
        private Author author;
        @Field(type = FieldType.Nested)
        private Map<Integer, Collection<String>> buckets = new HashMap<>();
//        @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "whitespace"),
//                    otherFields = { @InnerField(suffix = "prefix", )})
//        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class Car {
        private String name;
        private String model;
    }

    @Data
    static class Author {
        @Nullable
        private String id;
        @Nullable
        private String name;
    }
}
