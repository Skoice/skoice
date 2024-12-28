package net.clementraynaud.skoice.spigot.minecraft;

import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
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
        SkoiceSpigot.adventure().player(this.player).sendActionBar(message);
    }

    @Override
    public void sendMessage(Component message) {
        SkoiceSpigot.adventure().player(this.player).sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.hasPermission(permission);
    }
}
