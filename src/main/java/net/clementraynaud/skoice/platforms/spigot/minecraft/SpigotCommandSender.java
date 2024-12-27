package net.clementraynaud.skoice.platforms.spigot.minecraft;

import net.clementraynaud.skoice.model.minecraft.SkoiceCommandSender;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class SpigotCommandSender extends SkoiceCommandSender {

    private final CommandSender sender;

    public SpigotCommandSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(Component message) {
        //todo
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.sender.hasPermission(permission);
    }
}
