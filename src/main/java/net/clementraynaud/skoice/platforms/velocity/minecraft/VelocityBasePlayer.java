package net.clementraynaud.skoice.platforms.velocity.minecraft;

import com.velocitypowered.api.proxy.Player;
import net.clementraynaud.skoice.model.minecraft.BasePlayer;
import net.kyori.adventure.text.Component;

public class VelocityBasePlayer extends BasePlayer {

    private final Player player;

    public VelocityBasePlayer(Player player) {
        super(player.getUniqueId());
        this.player = player;
    }

    @Override
    public void sendMessage(Component message) {
        this.player.sendMessage(message);
    }

    @Override
    public void sendActionBar(String message) {
        this.player.sendActionBar(Component.text(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.hasPermission(permission);
    }
}
