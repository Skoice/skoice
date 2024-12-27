package net.clementraynaud.skoice.model.minecraft;

import net.kyori.adventure.text.Component;

public class FullPlayer extends BasePlayer {

    private final BasePlayer basePlayer;
    private PlayerInfo playerInfo;

    public FullPlayer(BasePlayer basePlayer, PlayerInfo playerInfo) {
        super(basePlayer.getUniqueId());
        this.basePlayer = basePlayer;
        this.playerInfo = playerInfo;
    }

    public boolean isDead() {
        return this.playerInfo.isDead();
    }

    public SkoiceGameMode getGameMode() {
        return this.playerInfo.getGameMode();
    }

    public String getWorld() {
        return this.playerInfo.getWorld();
    }

    public SkoiceLocation getLocation() {
        return this.playerInfo.getLocation();
    }

    public String getTeam() {
        return this.playerInfo.getTeam();
    }

    public void setPlayerInfo(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    @Override
    public void sendActionBar(Component message) {
        this.basePlayer.sendActionBar(message);
    }

    @Override
    public void sendMessage(Component message) {
        this.basePlayer.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.basePlayer.hasPermission(permission);
    }

    @Override
    public String toString() {
        return "FullPlayer{" +
                "basePlayer=" + this.basePlayer +
                ", playerInfo=" + this.playerInfo +
                ", id=" + this.id +
                '}';
    }
}
