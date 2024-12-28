package net.clementraynaud.skoice.spigot;

import net.clementraynaud.skoice.common.bot.Bot;
import net.clementraynaud.skoice.spigot.api.events.player.PlayerProximityConnectEvent;
import net.clementraynaud.skoice.spigot.api.events.system.SystemReadyEvent;
import net.clementraynaud.skoice.spigot.tasks.SpigotInterruptSystemTask;

public class SpigotBot extends Bot {

    private final SkoiceSpigot plugin;

    public SpigotBot(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void callPlayerProximityConnectEvent(String minecraftId, String memberId) {
        this.plugin.getPlugin().getServer().getScheduler().runTask(this.plugin.getPlugin(), () -> {
            PlayerProximityConnectEvent event = new PlayerProximityConnectEvent(minecraftId, memberId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }

    @Override
    protected void callSystemReadyEvent() {
        this.plugin.getPlugin().getServer().getScheduler().runTask(this.plugin.getPlugin(), () -> {
            SystemReadyEvent event = new SystemReadyEvent();
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }


    @Override
    public void runInterruptSystemTask() {
        new SpigotInterruptSystemTask(this.plugin).run();
    }
}
