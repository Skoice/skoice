package net.clementraynaud.skoice.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class ComponentUtil {

    public static Component translateAlternateColorCodes(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }
}
