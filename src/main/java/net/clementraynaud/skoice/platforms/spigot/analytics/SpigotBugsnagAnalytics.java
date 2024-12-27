package net.clementraynaud.skoice.platforms.spigot.analytics;

import com.bugsnag.Report;
import net.clementraynaud.skoice.analytics.AnalyticManager;
import net.clementraynaud.skoice.analytics.BugsnagAnalytics;
import net.clementraynaud.skoice.platforms.spigot.SkoiceSpigot;

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
