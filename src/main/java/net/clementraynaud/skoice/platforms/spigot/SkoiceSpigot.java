package net.clementraynaud.skoice.platforms.spigot;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.model.minecraft.FullPlayer;
import net.clementraynaud.skoice.platforms.spigot.commands.skoice.SkoiceCommandSpigot;
import net.clementraynaud.skoice.platforms.spigot.logger.JULLoggerAdapter;
import net.clementraynaud.skoice.platforms.spigot.minecraft.SpigotBasePlayer;
import net.clementraynaud.skoice.platforms.spigot.minecraft.SpigotFullPlayer;
import net.clementraynaud.skoice.platforms.spigot.scheduler.SpigotTaskScheduler;
import net.clementraynaud.skoice.platforms.spigot.system.SpigotListenerManager;
import org.bukkit.generator.WorldInfo;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SkoiceSpigot extends Skoice {

    private final SkoicePluginSpigot plugin;

    public SkoiceSpigot(SkoicePluginSpigot plugin) {
        super(new JULLoggerAdapter(plugin.getLogger()), new SpigotTaskScheduler(plugin));
        super.setListenerManager(new SpigotListenerManager(this));
        this.plugin = plugin;
    }

    @Override
    public SkoiceCommand setSkoiceCommand() {
        return new SkoiceCommandSpigot(this);
    }

    @Override
    public boolean isEnabled() {
        return this.plugin.isEnabled();
    }

    @Override
    public File getDataFolder() {
        return this.plugin.getDataFolder();
    }

    @Override
    public BasePlayer getPlayer(UUID uuid) {
        return Optional.ofNullable(this.plugin.getServer().getPlayer(uuid)).map(SpigotBasePlayer::new).orElse(null);
    }

    @Override
    public Collection<FullPlayer> getOnlinePlayers() {
        return this.plugin.getServer().getOnlinePlayers().stream()
                .map(SpigotFullPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getWorlds() {
        return this.plugin.getServer().getWorlds().stream().map(WorldInfo::getName).toList();
    }

    @Override
    public FullPlayer getFullPlayer(BasePlayer player) {
        return Optional.ofNullable(this.plugin.getServer().getPlayer(player.getUniqueId())).map(SpigotFullPlayer::new).orElse(null);
    }

    public SkoicePluginSpigot getPlugin() {
        return this.plugin;
    }
}
