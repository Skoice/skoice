package net.clementraynaud.skoice.hooks;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.hooks.discordsrv.DiscordSRVHook;
import net.clementraynaud.skoice.hooks.essentialsx.EssentialsXHook;

public class HookManager {

    private final Skoice plugin;
    private DiscordSRVHook discordSRVHook;
    private EssentialsXHook essentialsXHook;

    public HookManager(Skoice plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        this.discordSRVHook = new DiscordSRVHook(this.plugin);
        this.discordSRVHook.initialize();
        this.essentialsXHook = new EssentialsXHook(this.plugin);
        this.essentialsXHook.initialize();
    }

    public void linkUser(String minecraftId, String discordId) {
        this.discordSRVHook.linkUser(minecraftId, discordId);
        this.essentialsXHook.linkUser(minecraftId, discordId);
    }

    public void unlinkUser(String minecraftId) {
        this.discordSRVHook.unlinkUser(minecraftId);
        this.essentialsXHook.unlinkUser(minecraftId);
    }

    public void close() {
        this.discordSRVHook.close();
    }
}
