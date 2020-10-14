package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.client.util.ScrollState;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

abstract class StreamQueries {
    static <T> SearchHitsIterator<T> streamResults(int maxCount, SearchScrollHits<T> searchHits,
                                                   Function<String, SearchScrollHits<T>> continueScrollFunction,
                                                   Consumer<List<String>> clearScrollConsumer) {
        Assert.notNull(searchHits, "searchHits must not be null");
        Assert.notNull(searchHits.getScrollId(), "scrollId of searchHits must not be null");
        Assert.notNull(continueScrollFunction, "continueScrollFunction must not be null");
        Assert.notNull(clearScrollConsumer, "clearScrollConsumer must not be null");

        Aggregations aggregations = searchHits.getAggregations();
        float maxScore = searchHits.getMaxScore();
        long totalHits = searchHits.getTotalHits();
        TotalHitsRelation totalHitsRelation = searchHits.getTotalHitsRelation();

        return new SearchHitsIterator<T>() {

            private volatile AtomicInteger currentCount = new AtomicInteger();
            private volatile Iterator<SearchHit<T>> currentScrollHits = searchHits.iterator();
            private volatile boolean continueScroll = currentScrollHits.hasNext();
            private volatile ScrollState scrollState = new ScrollState(searchHits.getScrollId());

            @Override
            @Nullable
            public Aggregations getAggregations() {
                return aggregations;
            }

            @Override
            public float getMaxScore() {
                return maxScore;
            }

            @Override
            public long getTotalHits() {
                return totalHits;
            }

            @Override
            public TotalHitsRelation getTotalHitsRelation() {
                return totalHitsRelation;
            }

            @Override
            public void close() {
                clearScrollConsumer.accept(scrollState.getScrollIds());
            }

            @Override
            public boolean hasNext() {
                if (!continueScroll || (maxCount > 0 && currentCount.get() >= maxCount)) {
                    return false;
                }

                if (!currentScrollHits.hasNext()) {
                    SearchScrollHits<T> nextPage = continueScrollFunction.apply(scrollState.getScrollId());
                    currentScrollHits = nextPage.iterator();
                    scrollState.updateScrollId(nextPage.getScrollId());
                    continueScroll = currentScrollHits.hasNext();
                }
                return currentScrollHits.hasNext();
            }

            @Override
            public SearchHit<T> next() {
                if (hasNext()) {
                    currentCount.incrementAndGet();
                    return currentScrollHits.next();
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private StreamQueries() {}
}
