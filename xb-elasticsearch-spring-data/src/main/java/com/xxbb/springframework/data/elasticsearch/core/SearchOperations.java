package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.core.query.MoreLikeThisQuery;
import com.xxbb.springframework.data.elasticsearch.core.query.Query;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.springframework.lang.Nullable;

import java.util.List;

public interface SearchOperations {
    default long count(Query query, IndexCoordinates index) {
        return count(query, null, index);
    }

    long count(Query query, Class<?> clazz);

    long count(Query query, @Nullable Class<?> clazz, IndexCoordinates index);

    SearchResponse suggest(SuggestBuilder suggestion, Class<?> clazz);

    SearchResponse suggest(SuggestBuilder suggestion, IndexCoordinates index);

    @Nullable
    default <T> SearchHit<T> searchOne(Query query, Class<T> clazz) {
        List<SearchHit<T>> content = search(query, clazz).getSearchHits();
        return content.isEmpty() ? null : content.get(0);
    }

    @Nullable
    default <T> SearchHit<T> searchOne(Query query, Class<T> clazz, IndexCoordinates index) {
        List<SearchHit<T>> content = search(query, clazz, index).getSearchHits();
        return content.isEmpty() ? null : content.get(0);
    }

    <T> List<SearchHits<T>> multiSearch(List<? extends Query> queries, Class<T> clazz);

    <T> List<SearchHits<T>> multiSearch(List<? extends Query> queries, Class<T> clazz, IndexCoordinates index);

    List<SearchHits<?>> multiSearch(List<? extends Query> queries, List<Class<?>> classes);

    List<SearchHits<?>> multiSearch(List<? extends Query> queries, List<Class<?>> classes, IndexCoordinates index);

    <T> SearchHits<T> search(Query query, Class<T> clazz);

    <T> SearchHits<T> search(Query query, Class<T> clazz, IndexCoordinates index);

    <T> SearchHits<T> search(MoreLikeThisQuery query, Class<T> clazz);

    <T> SearchHits<T> search(MoreLikeThisQuery query, Class<T> clazz, IndexCoordinates index);

    <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz);

    <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz, IndexCoordinates index);
}
