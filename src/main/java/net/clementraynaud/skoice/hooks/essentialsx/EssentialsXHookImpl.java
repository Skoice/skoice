package net.clementraynaud.skoice.hooks.essentialsx;

import net.clementraynaud.skoice.Skoice;
import net.essentialsx.api.v2.events.discordlink.DiscordLinkStatusChangeEvent;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EssentialsXHookImpl implements Listener {

    private final Skoice plugin;
    private DiscordLinkService linkApi;
    private DiscordService discordApi;

    public EssentialsXHookImpl(Skoice plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void initialize() {
        try {
            this.linkApi = this.plugin.getServer().getServicesManager().load(DiscordLinkService.class);
            this.discordApi = Bukkit.getServicesManager().load(DiscordService.class);
            this.sendSkoiceLinks();
        } catch (Throwable ignored) {
        }
    }

    public void linkUser(String minecraftId, String discordId) {
        try {
            this.discordApi.getMemberById(discordId).thenAccept(member -> this.linkApi.linkAccount(UUID.fromString(minecraftId), member));
        } catch (Throwable ignored) {
        }
    }

    public void unlinkUser(String minecraftId) {
        try {
            this.linkApi.unlinkAccount(UUID.fromString(minecraftId));
        } catch (Throwable ignored) {
        }
    }

    @EventHandler
    public void onDiscordLinkStatusChange(DiscordLinkStatusChangeEvent event) {
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

    private void sendSkoiceLinks() {
        Map<String, String> existingSkoiceLinks = new HashMap<>(Skoice.api().getLinkedAccounts());
        existingSkoiceLinks.forEach((minecraftId, discordId) -> {
            try {
                UUID uuid = UUID.fromString(minecraftId);
                this.discordApi.getMemberById(discordId).thenAccept(member -> {
                    this.linkApi.linkAccount(uuid, member);
                });
            } catch (Throwable ignored) {
            }
        });
    }
}
