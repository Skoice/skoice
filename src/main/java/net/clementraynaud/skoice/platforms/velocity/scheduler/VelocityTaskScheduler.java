package net.clementraynaud.skoice.platforms.velocity.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import net.clementraynaud.skoice.model.scheduler.SkoiceTaskScheduler;
import net.clementraynaud.skoice.platforms.velocity.SkoicePluginVelocity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class VelocityTaskScheduler implements SkoiceTaskScheduler {

    private final SkoicePluginVelocity plugin;
    private final Scheduler taskScheduler;
    private final ConcurrentHashMap<Integer, ScheduledTask> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger taskIdCounter = new AtomicInteger(0);

    public VelocityTaskScheduler(SkoicePluginVelocity plugin) {
        this.plugin = plugin;
        this.taskScheduler = plugin.getProxy().getScheduler();
    }

    @Override
    public int runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        int taskId = this.taskIdCounter.incrementAndGet();
        ScheduledTask scheduledTask = this.taskScheduler.buildTask(this.plugin, task)
                .repeat(period, TimeUnit.MILLISECONDS)
                .delay(delay, TimeUnit.MILLISECONDS)
                .schedule();

        this.tasks.put(taskId, scheduledTask);

        return taskId;
    }

    @Override
    public void cancelTask(int taskId) {
        ScheduledTask task = this.tasks.remove(taskId);
        if (task != null) {
            task.cancel();
        }
    }
}
