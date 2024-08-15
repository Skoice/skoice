package net.clementraynaud.skoice.hooks.essentialsx;

import net.clementraynaud.skoice.Skoice;
import net.essentialsx.api.v2.events.discordlink.DiscordLinkStatusChangeEvent;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;
import net.essentialsx.discordlink.EssentialsDiscordLink;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EssentialsXHookImpl implements Listener {

    private final Skoice plugin;
    private DiscordLinkService essentialsLinkApi;
    private DiscordService essentialsDiscordApi;
    private Map<String, String> essentialsLinkedAccounts;
    private boolean isDiscordReady = false;

    public EssentialsXHookImpl(Skoice plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void initialize() {
        this.essentialsLinkApi = this.plugin.getServer().getServicesManager().load(DiscordLinkService.class);
        this.essentialsDiscordApi = Bukkit.getServicesManager().load(DiscordService.class);
        EssentialsDiscordLink ess = (EssentialsDiscordLink) Bukkit.getPluginManager().getPlugin("EssentialsDiscordLink");
        if (ess != null && ess.getAccountStorage() != null) {
            this.essentialsLinkedAccounts = Collections.unmodifiableMap(ess.getAccountStorage().getRawStorageMap());
            this.synchronizeAccountLinks();
        }
    }

    public void linkUser(String minecraftId, String discordId) {
        try {
            this.essentialsDiscordApi.getMemberById(discordId).thenAccept(member -> this.essentialsLinkApi.linkAccount(UUID.fromString(minecraftId), member));
        } catch (Throwable ignored) {
        }
    }

    public void unlinkUser(String minecraftId) {
        try {
            this.essentialsLinkApi.unlinkAccount(UUID.fromString(minecraftId));
        } catch (Throwable ignored) {
        }
    }

    @EventHandler
    public void onDiscordLinkStatusChange(DiscordLinkStatusChangeEvent event) {
        if (!this.isDiscordReady) {
            return;
        }
        if (event.isLinked()) {
            if (event.getMemberId() != null && event.getUser() != null && event.getUser().getUUID() != null) {
                Skoice.api().linkUser(event.getUser().getUUID(), event.getMemberId());
            }
        } else {
            if (event.getUser() != null && event.getUser().getUUID() != null) {
                Skoice.api().unlinkUser(event.getUser().getUUID());
            }
        }
    }

    private void synchronizeAccountLinks() {
        if (this.isDiscordReady) {
            return;
        }

        Map<String, String> existingHookLinks = this.essentialsLinkedAccounts;
        Map<String, String> existingSkoiceLinks = new HashMap<>(Skoice.api().getLinkedAccounts());

        existingHookLinks.forEach((minecraftId, discordId) -> {
            if (!existingSkoiceLinks.containsValue(discordId)) {
                this.plugin.getLinksYamlFile().linkUserDirectly(minecraftId, discordId);
            }
        });

        existingSkoiceLinks.forEach((minecraftId, discordId) -> {
            if (!existingHookLinks.containsKey(minecraftId)) {
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    try {
                        this.linkUser(minecraftId, discordId);
                    } catch (Throwable ignored) {
                    }
                });
            }
        });

        this.isDiscordReady = true;
    }
}
