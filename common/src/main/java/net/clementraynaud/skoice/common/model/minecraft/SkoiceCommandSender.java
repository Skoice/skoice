package net.clementraynaud.skoice.common.model.minecraft;

import net.clementraynaud.skoice.common.util.ComponentUtil;
import net.kyori.adventure.text.Component;

public abstract class SkoiceCommandSender {

    public void sendMessage(String message) {
        this.sendMessage(ComponentUtil.translateAlternateColorCodes(message));
    }

    public abstract void sendMessage(Component message);

    public abstract boolean hasPermission(String permission);
}
