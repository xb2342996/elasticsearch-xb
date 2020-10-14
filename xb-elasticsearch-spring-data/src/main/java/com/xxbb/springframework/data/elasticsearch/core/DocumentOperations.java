package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.core.query.*;
import org.springframework.lang.Nullable;

import java.util.List;

public interface DocumentOperations {
    <T> T save(T entity);

    <T> T save(T entity, IndexCoordinates index);

    <T> Iterable<T> save(Iterable<T> entities);

    <T> Iterable<T> save(Iterable<T> entities, IndexCoordinates index);

    <T> Iterable<T> save(T... entities);

    String index(IndexQuery query, IndexCoordinates index);

    @Nullable
    <T> T get(String id, Class<T> clazz);

    @Nullable
    <T> T get(String id, Class<T> clazz, IndexCoordinates index);

    <T> List<T> multiGet(Query query, Class<T> clazz);

    <T> List<T> multiGet(Query query, Class<T> clazz, IndexCoordinates index);

    boolean exists(String id, Class<?> clazz);

    boolean exists(String id, IndexCoordinates index);

    default List<IndexObjectInformation> bulkIndex(List<IndexQuery> queries, IndexCoordinates index) {
        return bulkIndex(queries, BulkOptions.defaultOptions(), index);
    }

    default List<IndexObjectInformation> bulkIndex(List<IndexQuery> queries, Class<?> clazz) {
        return bulkIndex(queries, BulkOptions.defaultOptions(), clazz);
    }

    List<IndexObjectInformation> bulkIndex(List<IndexQuery> queries, BulkOptions bulkOptions, IndexCoordinates index);

    List<IndexObjectInformation> bulkIndex(List<IndexQuery> queries, BulkOptions bulkOptions, Class<?> clazz);

    default void bulkUpdate(List<UpdateQuery> queries, IndexCoordinates index) {
        bulkUpdate(queries, BulkOptions.defaultOptions(), index);
    }

    void bulkUpdate(List<UpdateQuery> queries, Class<?> clazz);

    void bulkUpdate(List<UpdateQuery> queries, BulkOptions bulkOptions, IndexCoordinates index);

    default String delete(String id, IndexCoordinates index) {
        return delete(id, null, index);
    }

    String delete(String id, @Nullable String routing, IndexCoordinates index);

    String delete(String id, Class<?> entityType);

    String delete(Object entity);

    String delete(Object entity, IndexCoordinates index);

    void delete(Query query, Class<?> clazz);

    void delete(Query query, Class<?> clazz, IndexCoordinates index);

    UpdateResponse update(UpdateQuery query, IndexCoordinates index);
}
