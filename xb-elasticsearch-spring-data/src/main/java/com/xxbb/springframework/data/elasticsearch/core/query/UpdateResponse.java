package com.xxbb.springframework.data.elasticsearch.core.query;

import org.springframework.util.Assert;

public class UpdateResponse {
    private Result result;

    public UpdateResponse(Result result) {
        Assert.notNull(result, "result must not null");
        this.result = result;
    }

    public Result getResult() {
        return result;
    }

    public enum Result {
        CREATED, UPDATED, DELETED, NOT_FOUND, NOOP;
    }
}
