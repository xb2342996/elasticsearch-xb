package com.xxbb.springframework.data.elasticsearch.junit.jupiter;

public class ClusterConnectionException extends RuntimeException{
    public ClusterConnectionException(String message) {
        super(message);
    }

    public ClusterConnectionException(Throwable cause) {
        super(cause);
    }
}
