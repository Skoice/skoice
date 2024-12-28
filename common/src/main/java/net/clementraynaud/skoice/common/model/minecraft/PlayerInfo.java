package net.clementraynaud.skoice.common.model.minecraft;

import net.clementraynaud.skoice.common.model.JsonModel;

import java.util.UUID;

public class PlayerInfo extends JsonModel {

    private final UUID id;
    private final boolean dead;
    private final SkoiceGameMode gameMode;
    private final SkoiceLocation location;
    private final String team;
    private final String pluginVersion;
    private String world;

    public PlayerInfo(UUID id, boolean dead, SkoiceGameMode gameMode, String world, SkoiceLocation location, String team, String pluginVersion) {
        this.id = id;
        this.dead = dead;
        this.gameMode = gameMode;
        this.world = world;
        this.location = location;
        this.team = team;
        this.pluginVersion = pluginVersion;
    }

    public UUID getId() {
        return this.id;
    }

    public boolean isDead() {
        return this.dead;
    }

    public String getPluginVersion() {
        return this.pluginVersion;
    }

    public SkoiceGameMode getGameMode() {
        return this.gameMode;
    }

    public String getWorld() {
        return this.world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public SkoiceLocation getLocation() {
        return this.location;
    }

    public String getTeam() {
        return this.team;
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "id=" + this.id +
                ", dead=" + this.dead +
                ", gameMode=" + this.gameMode +
                ", world='" + this.world + '\'' +
                ", location=" + this.location +
                ", team='" + this.team + '\'' +
                ", pluginVersion='" + this.pluginVersion + '\'' +
                '}';
    }
}
