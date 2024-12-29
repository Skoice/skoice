package net.clementraynaud.skoice.common.model.scheduler;

public interface SkoiceTaskScheduler {

    int runTaskTimerAsynchronously(Runnable task, long delay, long period);

    void runTaskLaterAsynchronously(Runnable task, long delay);

    void runTaskAsynchronously(Runnable task);

    void cancelTask(int taskId);
}
