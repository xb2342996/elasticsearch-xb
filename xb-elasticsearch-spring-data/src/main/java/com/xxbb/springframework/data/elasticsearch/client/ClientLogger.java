package com.xxbb.springframework.data.elasticsearch.client;

import org.apache.logging.log4j.util.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;


public abstract class ClientLogger {
    private static final String lineSperator = System.getProperty("line.seprator");
    private static final Logger logger = LoggerFactory.getLogger(ClientLogger.class);

    private ClientLogger() {}

    public static boolean isEnable() {
        return logger.isTraceEnabled();
    }

    public static void logRequest(String logId, String method, String endpoint, Object parameters) {
        if (isEnable()) {
            logger.trace("[{}] sending request {} {} with paramters: {}", logId, method.toUpperCase(), endpoint, parameters);
        }
    }

    public static void logRequest(String logId, String method, String endpoint, Object parameters, Supplier<Object> body) {
        if (isEnable()) {
            logger.trace("[{}] sending request {} {} with paramters: {}{} RequestBody: {}", logId, method.toUpperCase(), endpoint, parameters, lineSperator, body.get());
        }
    }

    public static void logRawResponse(String logId, HttpStatus statusCode) {
        if (isEnable()) {
            logger.trace("[{}] Received raw response: {}", logId, statusCode);
        }
    }
    public static void logRawResponse(String logId, HttpStatus statusCode, String body) {
        if (isEnable()) {
            logger.trace("[{}] Received raw response: {}{} ResponseBody: {}", logId, statusCode, lineSperator, body);
        }
    }
    public static String newLogId() {
        if (!isEnable()) {
            return "-";
        }
        return ObjectUtils.getIdentityHexString(new Object());
    }
}
