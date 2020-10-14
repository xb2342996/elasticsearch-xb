package com.xxbb.springframework.data.elasticsearch.client;

import org.springframework.util.Assert;

import java.net.InetSocketAddress;
import java.time.Instant;

public class ElasticsearchHost {
    public static final int DEFAULT_HOST = 9200;

    private final InetSocketAddress endpoint;
    private final State state;
    private final Instant timestamp;

    public ElasticsearchHost(InetSocketAddress endpoint, State state) {

        Assert.notNull(endpoint, "Endpoint must not be null");
        Assert.notNull(state, "State must not be null");

        this.endpoint = endpoint;
        this.state = state;
        this.timestamp = Instant.now();
    }

    public static ElasticsearchHost online(InetSocketAddress host) {
        return new ElasticsearchHost(host, State.ONLINE);
    }

    public static ElasticsearchHost offline(InetSocketAddress host) {
        return new ElasticsearchHost(host, State.OFFLINE);
    }

    public static InetSocketAddress parse(String hostAndPort) {
        return InetSocketAddressParser.parse(hostAndPort, DEFAULT_HOST);
    }

    public boolean isOnline() {
        return State.ONLINE.equals(state);
    }

    public InetSocketAddress getEndpoint() {
        return endpoint;
    }

    public State getState() {
        return state;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    private enum State {
        ONLINE, OFFLINE, UNKNOWN
    }
}
