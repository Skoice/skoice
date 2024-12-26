package net.clementraynaud.skoice.model.scheduler;

public interface SkoiceTaskScheduler {

    int runTaskTimerAsynchronously(Runnable task, long delay, long period);

    void cancelTask(int taskId);
}
