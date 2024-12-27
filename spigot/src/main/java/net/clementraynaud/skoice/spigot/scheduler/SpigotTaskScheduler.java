package net.clementraynaud.skoice.spigot.scheduler;

import net.clementraynaud.skoice.common.model.scheduler.SkoiceTaskScheduler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class SpigotTaskScheduler implements SkoiceTaskScheduler {

    private final JavaPlugin plugin;

    public SpigotTaskScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        BukkitTask bukkitTask = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, task, delay / 50L, period / 50L);
        return bukkitTask.getTaskId();
    }

    @Override
    public void cancelTask(int taskId) {
        this.plugin.getServer().getScheduler().cancelTask(taskId);
    }
}
