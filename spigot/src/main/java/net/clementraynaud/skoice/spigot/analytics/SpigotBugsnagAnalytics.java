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

package net.clementraynaud.skoice.spigot.analytics;

import com.bugsnag.Report;
import net.clementraynaud.skoice.common.analytics.AnalyticManager;
import net.clementraynaud.skoice.common.analytics.BugsnagAnalytics;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;

public class SpigotBugsnagAnalytics extends BugsnagAnalytics {

    private final SkoiceSpigot plugin;

    public SpigotBugsnagAnalytics(SkoiceSpigot plugin, AnalyticManager analyticManager) {
        super(plugin, analyticManager);
        this.plugin = plugin;
    }

    @Override
    protected void addAdditionalMetadata(Report report) {
        report.addToTab("server", "version", this.plugin.getPlugin().getServer().getVersion());
        report.addToTab("server", "bukkitVersion", this.plugin.getPlugin().getServer().getBukkitVersion());
    }
}
