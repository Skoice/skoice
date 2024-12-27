package net.clementraynaud.skoice.spigot.minecraft;

import net.clementraynaud.skoice.common.model.minecraft.FullPlayer;
import net.clementraynaud.skoice.common.model.minecraft.PlayerInfo;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceGameMode;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceLocation;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class SpigotFullPlayer extends FullPlayer {

    private final Player playerInfo;

    public SpigotFullPlayer(Player player) {
        super(new SpigotBasePlayer(player), null);
        this.playerInfo = player;
    }

    @Override
    public boolean isDead() {
        return this.playerInfo.isDead();
    }

    @Override
    public SkoiceGameMode getGameMode() {
        return SkoiceGameMode.valueOf(this.playerInfo.getGameMode().toString());
    }

    @Override
    public String getWorld() {
        return this.playerInfo.getWorld().getName();
    }

    @Override
    public SkoiceLocation getLocation() {
        return new SkoiceLocation(this.playerInfo.getLocation().getX(), this.playerInfo.getLocation().getY(), this.playerInfo.getLocation().getZ());
    }

    @Override
    public void setPlayerInfo(PlayerInfo playerInfo) {
        throw new UnsupportedOperationException("Cannot set player info for SpigotFullPlayer");
    }

    @Override
    public String getTeam() {
        Scoreboard scoreboard = this.playerInfo.getScoreboard();
        Team playerTeam = scoreboard.getEntryTeam(this.playerInfo.getName());
        return playerTeam == null ? null : playerTeam.getName();
    }
}
