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

import com.bugsnag.Bugsnag;
import com.bugsnag.Report;
import com.bugsnag.Severity;
import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.lang.LangInfo;
import net.clementraynaud.skoice.common.storage.config.ConfigField;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BugsnagAnalytics {

    private static final String BUGSNAG_SERVICE_ID = "";
    private final Skoice plugin;
    private final AnalyticManager analyticManager;
    private final Set<String> reportedExceptions = ConcurrentHashMap.newKeySet();
    private Bugsnag bugsnag;

    public BugsnagAnalytics(Skoice plugin, AnalyticManager analyticManager) {
        this.plugin = plugin;
        this.analyticManager = analyticManager;
    }

    public void notify(Throwable throwable, Severity severity) {
        if (this.bugsnag == null) {
            return;
        }
        this.plugin.getScheduler().runTaskAsynchronously(() -> {
            String exceptionKey = this.createExceptionKey(throwable);

            if (this.reportedExceptions.contains(exceptionKey)) {
                return;
            }

            this.reportedExceptions.add(exceptionKey);

            try {
                this.bugsnag.notify(throwable, severity);
            } catch (Throwable ignored) {
            }
        });
    }

    private String createExceptionKey(Throwable throwable) {
        StringBuilder keyBuilder = new StringBuilder(throwable.toString());
        for (StackTraceElement element : throwable.getStackTrace()) {
            keyBuilder.append(element.toString());
        }
        return keyBuilder.toString();
    }

    public void initialize() {
        if (BugsnagAnalytics.BUGSNAG_SERVICE_ID.isEmpty()) {
            return;
        }
        this.bugsnag = new Bugsnag(BugsnagAnalytics.BUGSNAG_SERVICE_ID);
        this.bugsnag.setAppVersion(this.plugin.getVersion());

        this.bugsnag.addCallback(report -> {
            StackTraceElement[] trace = report.getException().getStackTrace();
            boolean reportError = false;
            for (StackTraceElement element : trace) {
                if (element.getClassName().startsWith("net.clementraynaud.skoice")) {
                    reportError = true;
                    break;
                }
            }
            if (!reportError) {
                report.cancel();
                return;
            }

            this.addAdditionalMetadata(report);

            this.analyticManager.getSharedConfigFields().forEach(field ->
                    report.addToTab("app", field.toCamelCase(), this.plugin.getConfigYamlFile().getString(field.toString()))
            );

            this.analyticManager.getSharedIntConfigFields().forEach(field ->
                    report.addToTab("app", field.toCamelCase(), this.plugin.getConfigYamlFile().getInt(field.toString()))
            );

            report.addToTab("app", ConfigField.LANG.toCamelCase(), LangInfo.valueOf(this.plugin.getConfigYamlFile().getString(ConfigField.LANG.toString())).getFullName());

            int linkedUsers = this.plugin.getLinksYamlFile().getLinks().size();
            report.addToTab("app", "linkedUsers", linkedUsers);
            report.addToTab("app", "botStatus", this.plugin.getBot().getStatus().toString());
        });

        this.bugsnag.setAutoCaptureSessions(false);
        if (!this.plugin.getConfigYamlFile().contains(ConfigField.SESSION_REPORTED.toString())) {
            this.bugsnag.startSession();
            this.plugin.getConfigYamlFile().set(ConfigField.SESSION_REPORTED.toString(), true);
        }
    }

    protected void addAdditionalMetadata(Report report) {
    }
}