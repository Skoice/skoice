package net.clementraynaud.skoice.spigot.scheduler;

import net.clementraynaud.skoice.common.model.scheduler.SkoiceTaskScheduler;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public class SpigotTaskScheduler implements SkoiceTaskScheduler {

    private final JavaPlugin plugin;

    public SpigotTaskScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private long toTicks(Duration duration) {
        return duration.toMillis() / 50L;
    }

    @Override
    public int runTaskTimerAsynchronously(Runnable task, Duration delay, Duration period) {
        return this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, task, this.toTicks(delay), this.toTicks(period)).getTaskId();
    }

    @Override
    public int runTaskTimer(Runnable task, Duration delay, Duration period) {
        return this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, task, this.toTicks(delay), this.toTicks(period)).getTaskId();
    }

    @Override
    public void runTaskLaterAsynchronously(Runnable task, Duration delay) {
        this.plugin.getServer().getScheduler().runTaskLaterAsynchronously(this.plugin, task, this.toTicks(delay));
    }

    @Override
    public void runTaskAsynchronously(Runnable task) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, task);
    }

    @Override
    public void runTask(Runnable task) {
        this.plugin.getServer().getScheduler().runTask(this.plugin, task);
    }

    @Override
    public void cancelTask(int taskId) {
        this.plugin.getServer().getScheduler().cancelTask(taskId);
    }
}
