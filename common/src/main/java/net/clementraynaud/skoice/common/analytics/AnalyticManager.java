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

package net.clementraynaud.skoice.common.analytics;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.storage.config.ConfigField;

import java.util.EnumSet;
import java.util.Set;

public class AnalyticManager {

    private final Skoice plugin;
    private BugsnagAnalytics bugsnagAnalytics;

    public AnalyticManager(Skoice plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        this.bugsnagAnalytics = this.createBugsnagAnalytics();
        this.bugsnagAnalytics.initialize();
        this.initializeAdditionalAnalytics();
    }

    protected BugsnagAnalytics createBugsnagAnalytics() {
        return new BugsnagAnalytics(this.plugin, this);
    }

    protected void initializeAdditionalAnalytics() {
    }

    public Set<ConfigField> getSharedConfigFields() {
        return EnumSet.of(
                ConfigField.LANG,
                ConfigField.LOGIN_NOTIFICATION,
                ConfigField.CONNECTING_ALERT,
                ConfigField.DISCONNECTING_ALERT,
                ConfigField.TOOLTIPS,
                ConfigField.PLAYERS_ON_DEATH_SCREEN_INCLUDED,
                ConfigField.SPECTATORS_INCLUDED,
                ConfigField.SEPARATED_TEAMS,
                ConfigField.TEXT_CHAT,
                ConfigField.CHANNEL_VISIBILITY,
                ConfigField.DISCORDSRV_SYNCHRONIZATION,
                ConfigField.ESSENTIALSX_SYNCHRONIZATION,
                ConfigField.RELEASE_CHANNEL
        );
    }

    public Set<ConfigField> getSharedIntConfigFields() {
        EnumSet<ConfigField> fields = EnumSet.noneOf(ConfigField.class);
        if (this.plugin.getConfigYamlFile().contains(ConfigField.HORIZONTAL_RADIUS.toString())) {
            fields.add(ConfigField.HORIZONTAL_RADIUS);
        }
        if (this.plugin.getConfigYamlFile().contains(ConfigField.VERTICAL_RADIUS.toString())) {
            fields.add(ConfigField.VERTICAL_RADIUS);
        }
        return fields;
    }

    public BugsnagAnalytics getBugsnag() {
        return this.bugsnagAnalytics;
    }

}