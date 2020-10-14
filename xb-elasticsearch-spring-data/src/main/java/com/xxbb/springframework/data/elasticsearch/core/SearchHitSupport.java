package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import com.xxbb.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.CloseableIterator;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SearchHitSupport {
    private SearchHitSupport() {}

    @Nullable
    public static Object unwrapSearchHits(@Nullable Object result) {
        if (result == null) {
            return result;
        }

        if (result instanceof SearchHit<?>) {
            return ((SearchHit<?>) result).getContent();
        }

        if (result instanceof List<?>) {
            return ((List<?>) result).stream().map(SearchHitSupport::unwrapSearchHits).collect(Collectors.toList());
        }

        if (result instanceof AggregatedPage<?>) {
            AggregatedPage<?> page = (AggregatedPage<?>) result;
            List<?> list = page.getContent().stream().map(SearchHitSupport::unwrapSearchHits).collect(Collectors.toList());
            return new AggregatedPageImpl<>(list, page.getPageable(), page.getTotalElements(), page.getAggregations(), page.getScrollId(), page.getMaxScore());
        }

        if (result instanceof Stream<?>) {
            return ((Stream<?>) result).map(SearchHitSupport::unwrapSearchHits);
        }

        if (result instanceof SearchHits<?>) {
            SearchHits<?> searchHits = (SearchHits<?>) result;
            return unwrapSearchHits(searchHits.getSearchHits());
        }

        if (result instanceof SearchHitsIterator<?>) {
            return unwrapSearchHitsIterator((SearchHitsIterator<?>) result);
        }

        return result;
    }

    private static CloseableIterator<?> unwrapSearchHitsIterator(SearchHitsIterator<?> iterator) {
        return new CloseableIterator<Object>() {
            @Override
            public void close() {
                iterator.close();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Object next() {
                return unwrapSearchHits(iterator.next());
            }
        };
    }

    public static <T> AggregatedPage<SearchHit<T>> page(SearchHits<T> searchHits, Pageable pageable) {
        return new AggregatedPageImpl<>(
                searchHits.getSearchHits(),
                pageable,
                searchHits.getTotalHits(),
                searchHits.getAggregations(),
                null,
                searchHits.getMaxScore()
        );
    }

    public static <T> SearchPage<T> searchPageFor(SearchHits<T> searchHits, @Nullable Pageable pageable) {
        return new SearchPageImpl<>(searchHits, (pageable != null) ? pageable : Pageable.unpaged());
    }

    static class SearchPageImpl<T> extends PageImpl<SearchHit<T>> implements SearchPage<T> {
        private final SearchHits<T> searchHits;

        public SearchPageImpl(SearchHits<T> searchHits, Pageable pageable) {
            super(searchHits.getSearchHits(), pageable, searchHits.getTotalHits());
            this.searchHits = searchHits;
        }

        @Override
        public SearchHits<T> getSearchHits() {
            return searchHits;
        }
    }
}
