package com.xxbb.springframework.data.elasticsearch.core.query;

import org.springframework.lang.Nullable;

public class FetchSourceFilter implements SourceFilter{
    @Nullable
    private final String[] includes;
    @Nullable
    private final String[] excludes;

    public FetchSourceFilter(@Nullable String[] includes, @Nullable String[] excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    @Nullable
    public String[] getIncludes() {
        return includes;
    }

    @Nullable
    public String[] getExcludes() {
        return excludes;
    }
}
