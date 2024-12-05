package net.clementraynaud.skoice.listeners;

import net.clementraynaud.skoice.Skoice;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StatusChangeListener extends ListenerAdapter {

    private final Skoice plugin;

    public StatusChangeListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onStatusChange(StatusChangeEvent event) {
        this.plugin.getBot().updateStatus();
    }
}
