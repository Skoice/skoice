package net.clementraynaud.skoice.storage.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigFieldTest {

    @Test
    public void testToCamelCase() {
        assertEquals("token", ConfigField.TOKEN.toCamelCase());
        assertEquals("horizontalRadius", ConfigField.HORIZONTAL_RADIUS.toCamelCase());
        assertEquals("channelVisibility", ConfigField.CHANNEL_VISIBILITY.toCamelCase());
    }

    @Test
    public void testToLowerCase() {
        assertEquals("token", ConfigField.TOKEN.toString());
        assertEquals("lang", ConfigField.LANG.toString());
        assertEquals("horizontal-radius", ConfigField.HORIZONTAL_RADIUS.toString());
    }
}