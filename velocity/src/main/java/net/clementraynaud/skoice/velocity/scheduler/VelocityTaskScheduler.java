/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 *
 * This file is part of Skoice.
 *
 * Skoice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skoice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skoice.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.clementraynaud.skoice.velocity.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import net.clementraynaud.skoice.common.model.scheduler.SkoiceTaskScheduler;
import net.clementraynaud.skoice.velocity.SkoicePluginVelocity;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
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
    public int runTaskTimerAsynchronously(Runnable task, Duration delay, Duration period) {
        return this.runTaskTimer(task, delay, period);
    }

    @Override
    public int runTaskTimer(Runnable task, Duration delay, Duration period) {
        int taskId = this.taskIdCounter.incrementAndGet();
        ScheduledTask scheduledTask = this.taskScheduler.buildTask(this.plugin, task)
                .delay(delay)
                .repeat(period)
                .schedule();

        this.tasks.put(taskId, scheduledTask);

        return taskId;
    }

    @Override
    public void runTaskLaterAsynchronously(Runnable task, Duration delay) {
        this.taskScheduler.buildTask(this.plugin, task)
                .delay(delay)
                .schedule();
    }

    @Override
    public void runTaskAsynchronously(Runnable task) {
        this.runTask(task);
    }

    @Override
    public void runTask(Runnable task) {
        this.taskScheduler.buildTask(this.plugin, task)
                .schedule();
    }

    @Override
    public void cancelTask(int taskId) {
        ScheduledTask task = this.tasks.remove(taskId);
        if (task != null) {
            task.cancel();
        }
    }
}
