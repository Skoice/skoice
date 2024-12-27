package net.clementraynaud.skoice.platforms.spigot.storage;

import net.clementraynaud.skoice.api.events.account.AccountLinkEvent;
import net.clementraynaud.skoice.api.events.account.AccountUnlinkEvent;
import net.clementraynaud.skoice.api.events.player.PlayerProximityConnectEvent;
import net.clementraynaud.skoice.api.events.player.PlayerProximityDisconnectEvent;
import net.clementraynaud.skoice.platforms.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.storage.LinksYamlFile;

import java.util.UUID;

public class SpigotLinksYamlFile extends LinksYamlFile {

    private final SkoiceSpigot plugin;

    public SpigotLinksYamlFile(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void additionalLinkProcessing(String minecraftId, String discordId) {
        this.plugin.getHookManager().linkUser(minecraftId, discordId);
        this.plugin.getPlugin().getServer().getScheduler().runTask(this.plugin.getPlugin(), () -> {
            AccountLinkEvent event = new AccountLinkEvent(minecraftId, discordId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }

    @Override
    protected void additionalUnlinkProcessing(String minecraftId) {
        this.plugin.getHookManager().unlinkUser(minecraftId);
        this.plugin.getPlugin().getServer().getScheduler().runTask(this.plugin.getPlugin(), () -> {
            AccountUnlinkEvent event = new AccountUnlinkEvent(minecraftId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }

    @Override
    protected void callPlayerProximityConnectEvent(String minecraftId, String memberId) {
        this.plugin.getPlugin().getServer().getScheduler().runTask(this.plugin.getPlugin(), () -> {
            PlayerProximityConnectEvent event = new PlayerProximityConnectEvent(minecraftId, memberId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }

    @Override
    protected void callPlayerProximityDisconnectEventIfConnected(String minecraftId) {
        if (SkoiceSpigot.api().isProximityConnected(UUID.fromString(minecraftId))) {
            this.plugin.getPlugin().getServer().getScheduler().runTask(this.plugin.getPlugin(), () -> {
                PlayerProximityDisconnectEvent event = new PlayerProximityDisconnectEvent(minecraftId);
                this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
            });
        }
    }
}
