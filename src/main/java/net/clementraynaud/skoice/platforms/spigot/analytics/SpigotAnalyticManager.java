package net.clementraynaud.skoice.platforms.spigot.analytics;

import net.clementraynaud.skoice.analytics.AnalyticManager;
import net.clementraynaud.skoice.analytics.BugsnagAnalytics;
import net.clementraynaud.skoice.platforms.spigot.SkoiceSpigot;

public class SpigotAnalyticManager extends AnalyticManager {

    private final SkoiceSpigot plugin;

    public SpigotAnalyticManager(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected BugsnagAnalytics createBugsnagAnalytics() {
        return new SpigotBugsnagAnalytics(this.plugin, this);
    }

    @Override
    protected void initializeAdditionalAnalytics() {
        new BStatsAnalytics(this.plugin, this).addCustomCharts();
    }
}
