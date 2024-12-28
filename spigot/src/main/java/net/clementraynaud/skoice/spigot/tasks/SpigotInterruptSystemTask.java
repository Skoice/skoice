package net.clementraynaud.skoice.spigot.tasks;


import net.clementraynaud.skoice.common.tasks.InterruptSystemTask;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.spigot.api.events.system.SystemInterruptionEvent;

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
