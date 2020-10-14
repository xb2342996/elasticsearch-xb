package com.xxbb.springframework.data.elasticsearch.utils;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {
    private static final AtomicInteger NEXT = new AtomicInteger();

    private IdGenerator() {}

    public static int nextIdAsInt() {
        return NEXT.incrementAndGet();
    }

    public static double nextIdAsDouble() {
        return NEXT.incrementAndGet();
    }

    public static String nextIdAsString() {
        return "" + nextIdAsInt();
    }
}
