package com.xxbb.springframework.data.elasticsearch.core.query;

import org.springframework.lang.Nullable;

public class FetchSourceFilterBuilder {
    @Nullable
    private String[] includes;
    @Nullable
    private String[] excludes;

    public FetchSourceFilterBuilder withIncludes(String... includes) {
        this.includes = includes;
        return this;
    }

    public FetchSourceFilterBuilder withExcludes(String... excludes) {
        this.excludes = excludes;
        return this;
    }

    public FetchSourceFilter build() {
        if (includes == null) {
            includes = new String[0];
        }
        if (excludes == null) {
            excludes = new String[0];
        }
        return new FetchSourceFilter(includes, excludes);
    }
}
