package net.clementraynaud.skoice.platforms.spigot.minecraft;

import net.clementraynaud.skoice.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.platforms.spigot.SkoiceSpigot;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class SpigotBasePlayer extends BasePlayer {

    private final Player player;

    public SpigotBasePlayer(Player player) {
        super(player.getUniqueId());
        this.player = player;
    }

    @Override
    public void sendActionBar(Component message) {
        SkoiceSpigot.adventure().player(player).sendActionBar(message);
    }

    @Override
    public void sendMessage(Component message) {
        SkoiceSpigot.adventure().player(player).sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.hasPermission(permission);
    }
}
