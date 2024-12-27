package net.clementraynaud.skoice.platforms.spigot.tasks;

import net.clementraynaud.skoice.platforms.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.platforms.spigot.api.events.system.SystemInterruptionEvent;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;

public class SpigotInterruptSystemTask extends InterruptSystemTask {

    private final SkoiceSpigot plugin;

    public SpigotInterruptSystemTask(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void callSystemInterruptionEvent() {
        this.plugin.getPlugin().getServer().getScheduler().runTask(this.plugin.getPlugin(), () -> {
            SystemInterruptionEvent event = new SystemInterruptionEvent();
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }
}
