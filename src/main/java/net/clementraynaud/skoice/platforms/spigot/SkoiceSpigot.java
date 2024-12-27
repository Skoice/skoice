package net.clementraynaud.skoice.platforms.spigot;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.analytics.AnalyticManager;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.hooks.HookManager;
import net.clementraynaud.skoice.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.model.minecraft.FullPlayer;
import net.clementraynaud.skoice.platforms.spigot.analytics.SpigotAnalyticManager;
import net.clementraynaud.skoice.platforms.spigot.api.SkoiceAPI;
import net.clementraynaud.skoice.platforms.spigot.commands.skoice.SkoiceCommandSpigot;
import net.clementraynaud.skoice.platforms.spigot.logger.JULLoggerAdapter;
import net.clementraynaud.skoice.platforms.spigot.minecraft.SpigotBasePlayer;
import net.clementraynaud.skoice.platforms.spigot.minecraft.SpigotFullPlayer;
import net.clementraynaud.skoice.platforms.spigot.scheduler.SpigotTaskScheduler;
import net.clementraynaud.skoice.platforms.spigot.storage.SpigotLinksYamlFile;
import net.clementraynaud.skoice.platforms.spigot.system.SpigotListenerManager;
import net.clementraynaud.skoice.storage.LinksYamlFile;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.GameMode;
import org.bukkit.generator.WorldInfo;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SkoiceSpigot extends Skoice {

    private static final String OUTDATED_MINECRAFT_SERVER_ERROR_MESSAGE = "Skoice only supports Minecraft 1.8 or later. Please update your Minecraft server to use the proximity voice chat.";
    private static BukkitAudiences adventure;
    private static SkoiceAPI api;
    private final SkoicePluginSpigot plugin;
    private HookManager hookManager;

    public SkoiceSpigot(SkoicePluginSpigot plugin) {
        super(new JULLoggerAdapter(plugin.getLogger()), new SpigotTaskScheduler(plugin));
        super.setListenerManager(new SpigotListenerManager(this));
        this.plugin = plugin;
    }

    public static SkoiceAPI api() {
        return SkoiceSpigot.api;
    }

    public static BukkitAudiences adventure() {
        return SkoiceSpigot.adventure;
    }

    @Override
    public void onEnable() {
        if (!this.isMinecraftServerCompatible()) {
            this.getLogger().severe(SkoiceSpigot.OUTDATED_MINECRAFT_SERVER_ERROR_MESSAGE);
            this.getPlugin().getServer().getPluginManager().disablePlugin(this.getPlugin());
            return;
        }
        SkoiceSpigot.api = new SkoiceAPI(this);
        SkoiceSpigot.adventure = BukkitAudiences.create(this.plugin);
        this.hookManager = new HookManager(this);
        this.hookManager.initialize();
        Updater updater = new Updater(this, this.plugin.getFile().getAbsolutePath());
        updater.runUpdaterTaskTimer();
        super.onEnable();
    }

    @Override
    public Bot createBot() {
        return new SpigotBot(this);
    }

    @Override
    public LinksYamlFile createLinksYamlFile() {
        return new SpigotLinksYamlFile(this);
    }

    @Override
    public void onDisable() {
        if (SkoiceSpigot.adventure != null) {
            SkoiceSpigot.adventure.close();
        }
        this.hookManager.close();
        super.onDisable();

    }

    public HookManager getHookManager() {
        return this.hookManager;
    }

    private boolean isMinecraftServerCompatible() {
        try {
            GameMode.SPECTATOR.toString();
        } catch (NoSuchFieldError exception) {
            return false;
        }
        return true;
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

    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    protected AnalyticManager createAnalyticManager() {
        return new SpigotAnalyticManager(this);
    }

    public SkoicePluginSpigot getPlugin() {
        return this.plugin;
    }
}
