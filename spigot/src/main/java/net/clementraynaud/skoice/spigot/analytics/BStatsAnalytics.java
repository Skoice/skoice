/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.common.analytics.AnalyticManager;

import net.clementraynaud.skoice.spigot.utils.ChartUtil;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

public class BStatsAnalytics {

    private static final int BSTATS_SERVICE_ID = 11380;
    private final SkoiceSpigot plugin;
    private final AnalyticManager analyticManager;

    public BStatsAnalytics(SkoiceSpigot plugin, AnalyticManager analyticManager) {
        this.plugin = plugin;
        this.analyticManager = analyticManager;
    }

    public void addCustomCharts() {
        Metrics metrics = new Metrics(this.plugin.getPlugin(), BStatsAnalytics.BSTATS_SERVICE_ID);

        this.analyticManager.getSharedConfigFields().forEach(field ->
                metrics.addCustomChart(new SimplePie(field.toCamelCase(), () ->
                        this.plugin.getConfigYamlFile().getString(field.toString())
                ))
        );

        this.analyticManager.getSharedIntConfigFields().forEach(field ->
                metrics.addCustomChart(ChartUtil.createDrilldownPie(field.toCamelCase(),
                        this.plugin.getConfigYamlFile().getInt(field.toString()), 0, 10, 11)
                )
        );

        int linkedUsers = this.plugin.getLinksYamlFile().getLinks().size();
        metrics.addCustomChart(ChartUtil.createDrilldownPie("linkedUsers", linkedUsers, 0, 10, 11));

        metrics.addCustomChart(new SimplePie("botStatus", () -> this.plugin.getBot().getStatus().toString()));
    }
}