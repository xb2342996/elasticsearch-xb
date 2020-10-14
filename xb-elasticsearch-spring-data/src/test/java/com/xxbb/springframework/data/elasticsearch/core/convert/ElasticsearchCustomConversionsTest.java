package com.xxbb.springframework.data.elasticsearch.core.convert;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticsearchCustomConversionsTest {

    private byte[] bytes = new byte[] {0x01, 0x02, 0x03, 0x04};
    private String base64 = "AQIDBA==";

    @Test
    public void shouldConvertFromStringToBase64() {
        assertThat(ElasticsearchCustomConversions.Base64ToByteArrayConverter.INSTANCE.convert(base64)).isEqualTo(bytes);
    }
}