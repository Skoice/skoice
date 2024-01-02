package net.clementraynaud.skoice.hooks.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import net.clementraynaud.skoice.Skoice;

public class DiscordSRVHook {

    private final Skoice plugin;
    private DiscordSRVHookImpl discordSRVHookImpl;

    public DiscordSRVHook(Skoice plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (this.discordSRVHookImpl != null) {
            return;
        }
        try {
            DiscordSRV.api.hashCode();
            this.discordSRVHookImpl = new DiscordSRVHookImpl(this.plugin);
            this.discordSRVHookImpl.initialize();
        } catch (Throwable ignored) {
        }
    }

    public void linkUser(String minecraftId, String discordId) {
        if (this.discordSRVHookImpl != null) {
            this.discordSRVHookImpl.linkUser(minecraftId, discordId);
        }
    }

    public void unlinkUser(String minecraftId) {
        if (this.discordSRVHookImpl != null) {
            this.discordSRVHookImpl.unlinkUser(minecraftId);
        }
    }

    public void close() {
        if (this.discordSRVHookImpl != null) {
            this.discordSRVHookImpl.close();
        }
    }
}
