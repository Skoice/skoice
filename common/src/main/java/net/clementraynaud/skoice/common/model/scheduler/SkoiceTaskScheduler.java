package net.clementraynaud.skoice.common.model.scheduler;

public interface SkoiceTaskScheduler {

    int runTaskTimerAsynchronously(Runnable task, long delay, long period);

    int runTaskLaterAsynchronously(Runnable task, long delay);

    void cancelTask(int taskId);
}
