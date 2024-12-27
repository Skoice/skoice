package net.clementraynaud.skoice.platforms.velocity;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.model.minecraft.FullPlayer;
import net.clementraynaud.skoice.model.minecraft.PlayerInfo;
import net.clementraynaud.skoice.platforms.velocity.commands.skoice.SkoiceCommandVelocity;
import net.clementraynaud.skoice.platforms.velocity.logger.SLF4JLoggerAdapter;
import net.clementraynaud.skoice.platforms.velocity.minecraft.VelocityBasePlayer;
import net.clementraynaud.skoice.platforms.velocity.scheduler.VelocityTaskScheduler;
import net.clementraynaud.skoice.platforms.velocity.system.VelocityListenerManager;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkoiceVelocity extends Skoice {

    private final SkoicePluginVelocity plugin;
    private final Map<UUID, FullPlayer> playerInfo = new ConcurrentHashMap<>();

    public SkoiceVelocity(SkoicePluginVelocity plugin) {
        super(new SLF4JLoggerAdapter(plugin.getLogger()), new VelocityTaskScheduler(plugin));
        this.plugin = plugin;
        super.setListenerManager(new VelocityListenerManager(this));
    }

    @Override
    public boolean isEnabled() {
        return this.plugin.getProxy().getPluginManager().getPlugin("skoice").isPresent();
    }

    @Override
    public File getDataFolder() {
        this.plugin.getDataDirectory().toFile().mkdirs();
        return this.plugin.getDataDirectory().toFile();
    }

    @Override
    public BasePlayer getPlayer(UUID uuid) {
        return this.plugin.getProxy().getPlayer(uuid).map(VelocityBasePlayer::new).orElse(null);
    }

    @Override
    public Collection<FullPlayer> getOnlinePlayers() {
        return this.playerInfo.values();
    }

    @Override
    public Collection<String> getWorlds() {
        return Collections.emptyList();
    }

    @Override
    public FullPlayer getFullPlayer(BasePlayer player) {
        if (this.playerInfo.containsKey(player.getUniqueId())) {
            return this.playerInfo.get(player.getUniqueId());
        }
        return null;
    }

    @Override
    public String getVersion() {
        return "3.2.3";
    }

    @Override
    public SkoiceCommand setSkoiceCommand() {
        return new SkoiceCommandVelocity(this);
    }

    public SkoicePluginVelocity getPlugin() {
        return this.plugin;
    }

    public VelocityListenerManager getListenerManager() {
        return (VelocityListenerManager) super.getListenerManager();
    }

    public void setPlayerInfo(PlayerInfo newInfo) {
        if (this.playerInfo.containsKey(newInfo.getId())) {
            FullPlayer info = this.playerInfo.get(newInfo.getId());
            info.setPlayerInfo(newInfo);
        } else {
            this.plugin.getProxy().getPlayer(newInfo.getId()).ifPresent(player -> {
                var newPlayer = new VelocityBasePlayer(player);
                this.playerInfo.put(newInfo.getId(), new FullPlayer(new VelocityBasePlayer(player), newInfo));
                this.getListenerManager().onPlayerJoin(newPlayer, true);
            });
        }
    }

    public void removePlayerInfo(UUID id) {
        this.playerInfo.remove(id);
    }
}
