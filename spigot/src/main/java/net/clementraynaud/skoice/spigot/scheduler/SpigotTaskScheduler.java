package net.clementraynaud.skoice.spigot.scheduler;

import net.clementraynaud.skoice.common.model.scheduler.SkoiceTaskScheduler;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotTaskScheduler implements SkoiceTaskScheduler {

    private final JavaPlugin plugin;

    public SpigotTaskScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        return this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, task, delay / 50L, period / 50L).getTaskId();
    }

    @Override
    public int runTaskLaterAsynchronously(Runnable task, long delay) {
        return this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, task, delay / 50L).getTaskId();
    }

    @Override
    public void cancelTask(int taskId) {
        this.plugin.getServer().getScheduler().cancelTask(taskId);
    }
}
