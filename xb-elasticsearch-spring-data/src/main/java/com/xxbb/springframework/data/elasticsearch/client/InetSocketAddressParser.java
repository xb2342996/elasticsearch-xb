package com.xxbb.springframework.data.elasticsearch.client;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;

public class InetSocketAddressParser {
    static InetSocketAddress parse(String hostPortString, int defaultPort) {
        Assert.notNull(hostPortString, "HostportString must not be null");
        String host;
        String portString = null;
        if (hostPortString.startsWith("[")) {
            String[] hostAndPort = getHostAndPortFromBracketedHost(hostPortString);
            host = hostAndPort[0];
            portString = hostAndPort[1];
        } else {
            int colonPos = hostPortString.indexOf(':');
            if (colonPos >= 0 && hostPortString.indexOf(':', colonPos + 1) == -1) {
                host = hostPortString.substring(0, colonPos);
                portString = hostPortString.substring(colonPos + 1);
            } else {
                host = hostPortString;
            }
        }
        int port = defaultPort;
        if (StringUtils.hasText(portString)) {
            Assert.isTrue(!portString.startsWith("+"), String.format("Cannot parse port number: %s", hostPortString));
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("Cannot parse port number: %s", hostPortString));
            }
            Assert.isTrue(isValidPort(port), String.format("Cannot parse port number: %s", hostPortString));
        }
        return InetSocketAddress.createUnresolved(host, port);
    }

    private static String[] getHostAndPortFromBracketedHost(String hostPortString) {
        Assert.isTrue(hostPortString.charAt(0) == '[', String.format("Bracketed host-port string must start with a bracket: %s", hostPortString));
        int colonIndex = hostPortString.indexOf(":");
        int closeBracketIndex = hostPortString.lastIndexOf("]");
        Assert.isTrue(colonIndex > -1 && closeBracketIndex > colonIndex, String.format("Invalid bracketed host/port: %s", hostPortString));

        String host = hostPortString.substring(1, closeBracketIndex);

        if (closeBracketIndex + 1 == hostPortString.length()) {
            return new String[] {host, ""};
        } else {
            Assert.isTrue(hostPortString.charAt(closeBracketIndex + 1) == ':', "Only a colon may follow a close bracket: " + hostPortString);
            for (int i = closeBracketIndex + 2; i < hostPortString.length(); ++i) {
                Assert.isTrue(Character.isDigit(hostPortString.charAt(i)), String.format("Post must be numeric: %s", hostPortString));
            }
            return new String[] {host, hostPortString.substring(closeBracketIndex + 2)};
        }
    }

    private static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }
}
