package net.clementraynaud.skoice.velocity.minecraft;

import com.velocitypowered.api.command.CommandSource;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceCommandSender;
import net.kyori.adventure.text.Component;

public class VelocityCommandSender extends SkoiceCommandSender {

    private final CommandSource source;

    public VelocityCommandSender(CommandSource source) {
        this.source = source;
    }

    @Override
    public void sendMessage(Component message) {
        this.source.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.source.hasPermission(permission);
    }
}
