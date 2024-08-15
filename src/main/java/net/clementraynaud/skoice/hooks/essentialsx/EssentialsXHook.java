package net.clementraynaud.skoice.hooks.essentialsx;

import net.clementraynaud.skoice.Skoice;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;
import net.essentialsx.discordlink.EssentialsDiscordLink;
import org.bukkit.Bukkit;

public class EssentialsXHook {

    private final Skoice plugin;
    private EssentialsXHookImpl essentialsXHookImpl;

    public EssentialsXHook(Skoice plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (this.essentialsXHookImpl != null) {
            return;
        }
        try {
            this.plugin.getServer().getServicesManager().load(DiscordLinkService.class).hashCode();
            this.plugin.getServer().getServicesManager().load(DiscordService.class).hashCode();
            ((EssentialsDiscordLink) Bukkit.getPluginManager().getPlugin("EssentialsDiscordLink")).getAccountStorage().hashCode();
            EssentialsXHookImpl essentialsXHookImpl = new EssentialsXHookImpl(this.plugin);
            essentialsXHookImpl.initialize();
            this.essentialsXHookImpl = essentialsXHookImpl;
        } catch (Throwable ignored) {
        }
    }

    public void linkUser(String minecraftId, String discordId) {
        if (this.essentialsXHookImpl != null) {
            this.essentialsXHookImpl.linkUser(minecraftId, discordId);
        }
    }

    public void unlinkUser(String minecraftId) {
        if (this.essentialsXHookImpl != null) {
            this.essentialsXHookImpl.unlinkUser(minecraftId);
        }
    }
}
