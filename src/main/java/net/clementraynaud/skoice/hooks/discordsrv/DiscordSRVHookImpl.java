package net.clementraynaud.skoice.hooks.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AccountLinkedEvent;
import github.scarsz.discordsrv.api.events.AccountUnlinkedEvent;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import net.clementraynaud.skoice.Skoice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscordSRVHookImpl {

    private final Skoice plugin;
    private boolean synchronizationComplete = false;

    public DiscordSRVHookImpl(Skoice plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        DiscordSRV.api.subscribe(this);
        if (DiscordSRV.getPlugin().getAccountLinkManager() != null) {
            this.synchronizeAccountLinks();
        }
    }

    public void close() {
        try {
            DiscordSRV.api.unsubscribe(this);
        } catch (Throwable ignored) {
        }
    }

    public void linkUserDiscordSRV(String minecraftId, String discordId) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                DiscordSRV.getPlugin().getAccountLinkManager().link(discordId, UUID.fromString(minecraftId));
            } catch (Throwable ignored) {
            }
        });
    }

    public void unlinkUserDiscordSRV(String minecraftId) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                DiscordSRV.getPlugin().getAccountLinkManager().unlink(UUID.fromString(minecraftId));
            } catch (Throwable ignored) {
            }
        });
    }

    @Subscribe
    public void onAccountLinked(AccountLinkedEvent event) {
        if (this.synchronizationComplete && event.getUser() != null && event.getPlayer() != null) {
            Skoice.api().linkUser(event.getPlayer().getUniqueId(), event.getUser().getId());
        }
    }

    @Subscribe
    public void onAccountUnlinked(AccountUnlinkedEvent event) {
        if (this.synchronizationComplete && event.getPlayer() != null) {
            Skoice.api().unlinkUser(event.getPlayer().getUniqueId());
        }
    }

    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        try {
            this.synchronizeAccountLinks();
        } catch (Throwable ignored) {
        }
    }

    private void synchronizeAccountLinks() {
        if (this.synchronizationComplete) {
            return;
        }

        Map<String, UUID> existingHookLinks = new HashMap<>(DiscordSRV.getPlugin().getAccountLinkManager().getLinkedAccounts());
        Map<String, String> existingSkoiceLinks = new HashMap<>(Skoice.api().getLinkedAccounts());

        existingHookLinks.forEach((discordId, minecraftId) -> {
            if (!existingSkoiceLinks.containsValue(discordId)) {
                Skoice.api().linkUser(minecraftId, discordId);
            }
        });

        existingSkoiceLinks.forEach((minecraftId, discordId) -> {
            try {
                UUID uuid = UUID.fromString(minecraftId);
                if (!existingHookLinks.containsValue(uuid)) {
                    this.linkUserDiscordSRV(minecraftId, discordId);
                }
            } catch (IllegalArgumentException ignored) {
            }
        });

        this.synchronizationComplete = true;
    }
}
