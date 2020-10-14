package com.xxbb.springframework.data.elasticsearch.client;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import java.net.InetSocketAddress;

public class InetsocketAddressParserUnitTest {
    @Test
    public void testFromStringWellformed() {
        checkFromStringCase("pirate.iso", 80, "pirate.iso", 80, false);
        checkFromStringCase("192.10.10.1", 88, "192.10.10.1", 88, false);
        checkFromStringCase("[2000::1]", 78, "2000::1", 78, false);
        checkFromStringCase("2000::1", 78, "2000::1", 78, false);
        checkFromStringCase("host:", 90, "host", 90, false);
    }

    @Test
    public void testFromStringBadDefaultPort() {
        checkFromStringCase("gmail.com:8888", -1, "gmail.com", 8888, true);
        checkFromStringCase("128.3.192.1:8888", -1, "128.3.192.1", 8888, true);
        checkFromStringCase("[2000::1]:8888", -1, "2000::1", 8888, true);
        checkFromStringCase("google.com:65535", 65536, "google.com", 65535, true);

        checkFromStringCase("google.com", -1, null, -1, false);
        checkFromStringCase("192.164.1.1", 65536, null, -1, false);
        checkFromStringCase("[2000::1]", -1, null, -1, false);
        checkFromStringCase("2000::1", 65536, null, -1, false);
    }

    @Test
    public void testFromStringBadPort() {
        // Out-of-range ports.
        checkFromStringCase("pivotal.io:65536", 1, null, 99, false);
        checkFromStringCase("pivotal.io:9999999999", 1, null, 99, false);
        // Invalid port parts.
        checkFromStringCase("pivotal.io:port", 1, null, 99, false);
        checkFromStringCase("pivotal.io:-25", 1, null, 99, false);
        checkFromStringCase("pivotal.io:+25", 1, null, 99, false);
        checkFromStringCase("pivotal.io:25  ", 1, null, 99, false);
        checkFromStringCase("pivotal.io:25\t", 1, null, 99, false);
        checkFromStringCase("pivotal.io:0x25 ", 1, null, 99, false);
    }

    @Test
    public void testFromStringUnparseableNonsense() {
        // Some nonsense that causes parse failures.
        checkFromStringCase("[goo.gl]", 1, null, 99, false);
        checkFromStringCase("[goo.gl]:80", 1, null, 99, false);
        checkFromStringCase("[", 1, null, 99, false);
        checkFromStringCase("[]:", 1, null, 99, false);
        checkFromStringCase("[]:80", 1, null, 99, false);
        checkFromStringCase("[]bad", 1, null, 99, false);
    }

    @Test
    public void testFromStringParseableNonsense() {
        // Examples of nonsense that gets through.
        checkFromStringCase("[[:]]", 86, "[:]", 86, false);
        checkFromStringCase("x:y:z", 87, "x:y:z", 87, false);
        checkFromStringCase("", 88, "", 88, false);
        checkFromStringCase(":", 99, "", 99, false);
        checkFromStringCase(":123", -1, "", 123, true);
        checkFromStringCase("\nOMG\t", 89, "\nOMG\t", 89, false);
    }

    private static void checkFromStringCase(String hpString, int defaultPort, String exceptHost, int exceptPort, boolean exceptHasExplicitPort) {
        InetSocketAddress hp;

        try {
            hp = InetSocketAddressParser.parse(hpString, defaultPort);
        } catch (IllegalArgumentException e) {
            assertThat(exceptHost).isNull();
            return;
        }

        assertThat(exceptHost).isNotNull();

        if (exceptHasExplicitPort) {
            assertThat(hp.getPort()).isEqualTo(exceptPort);
        } else {
            assertThat(hp.getPort()).isEqualTo(defaultPort);
        }
        assertThat(hp.getHostString()).isEqualTo(exceptHost);
    }
}
