package com.xxbb.springframework.data.elasticsearch.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.Charset;

public abstract class ResourceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);

    @Nullable
    public static String readFileFromClasspath(String url) {
        ClassPathResource classPathResource = new ClassPathResource(url);
        try (InputStream is = classPathResource.getInputStream()){
            return StreamUtils.copyToString(is, Charset.defaultCharset());
        } catch (Exception e) {
            LOGGER.warn(String.format("Failed to load file from url: %s: %s", url, e.getMessage()));
            return null;
        }
    }

    public ResourceUtil() {
    }
}
