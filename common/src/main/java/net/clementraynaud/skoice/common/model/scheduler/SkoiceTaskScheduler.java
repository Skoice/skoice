package net.clementraynaud.skoice.common.model.scheduler;

import java.time.Duration;

public interface SkoiceTaskScheduler {

    int runTaskTimerAsynchronously(Runnable task, Duration delay, Duration period);

    int runTaskTimer(Runnable task, Duration delay, Duration period);

    void runTaskLaterAsynchronously(Runnable task, Duration delay);

    void runTaskAsynchronously(Runnable task);

    void runTask(Runnable task);

    void cancelTask(int taskId);
}
