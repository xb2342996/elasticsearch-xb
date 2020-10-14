package com.xxbb.springframework.data.elasticsearch.repository;


import com.xxbb.springframework.data.elasticsearch.core.query.Query;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.Nullable;

@NoRepositoryBean
public interface ElasticsearchRepository<T, ID> extends PagingAndSortingRepository<T, ID> {

    Iterable<T> search(QueryBuilder query);

    Page<T> search(QueryBuilder query, Pageable pageable);

    Page<T> search(Query searchQuery);

    Page<T> searchSimilar(T entity, @Nullable String[] fields, Pageable pageable);
}
