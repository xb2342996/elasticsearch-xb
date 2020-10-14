package com.xxbb.springframework.data.elasticsearch.core.query;

import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.lang.Nullable;

public class BulkOptions {
    private static final BulkOptions defaultOptions = builder().build();
    @Nullable
    private final TimeValue timeout;
    @Nullable
    private final WriteRequest.RefreshPolicy refreshPolicy;
    @Nullable
    private final ActiveShardCount waitForActiveShards;
    @Nullable
    private final String pipeline;
    @Nullable
    private final String routingId;

    public BulkOptions(@Nullable TimeValue timeout, @Nullable WriteRequest.RefreshPolicy refreshPolicy,
                       @Nullable ActiveShardCount waitForActiveShards, @Nullable String pipeline, @Nullable String routingId) {
        this.timeout = timeout;
        this.refreshPolicy = refreshPolicy;
        this.waitForActiveShards = waitForActiveShards;
        this.pipeline = pipeline;
        this.routingId = routingId;
    }

    @Nullable
    public WriteRequest.RefreshPolicy getRefreshPolicy() {
        return refreshPolicy;
    }

    @Nullable
    public ActiveShardCount getWaitForActiveShards() {
        return waitForActiveShards;
    }

    @Nullable
    public String getPipeline() {
        return pipeline;
    }

    @Nullable
    public String getRoutingId() {
        return routingId;
    }

    @Nullable
    public TimeValue getTimeout() {
        return timeout;
    }

    public static BulkOptionsBuilder builder() {
        return new BulkOptionsBuilder();
    }

    public static BulkOptions defaultOptions() {
        return defaultOptions;
    }

    public static class BulkOptionsBuilder {
        @Nullable
        private TimeValue timeout;
        @Nullable
        private WriteRequest.RefreshPolicy refreshPolicy;
        @Nullable
        private ActiveShardCount waitForActiveShards;
        @Nullable
        private String pipeline;
        @Nullable
        private String routingId;

        public BulkOptionsBuilder() {
        }

        public BulkOptionsBuilder withTimeout(TimeValue timeout) {
            this.timeout = timeout;
            return this;
        }

        public BulkOptionsBuilder withRefreshPolicy(WriteRequest.RefreshPolicy refreshPolicy) {
            this.refreshPolicy = refreshPolicy;
            return this;
        }

        public BulkOptionsBuilder withWaitForActiveShards(ActiveShardCount waitForActiveShards) {
            this.waitForActiveShards = waitForActiveShards;
            return this;
        }

        public BulkOptionsBuilder withPipeline(String pipeline) {
            this.pipeline = pipeline;
            return this;
        }

        public BulkOptionsBuilder withRoutingId(String routingId) {
            this.routingId = routingId;
            return this;
        }

        public BulkOptions build() {
            return new BulkOptions(timeout, refreshPolicy, waitForActiveShards, pipeline, routingId);
        }
    }
}


