package net.clementraynaud.skoice.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SkoiceEvent extends Event {

    protected static final HandlerList HANDLERS = new HandlerList();

    private final String minecraftId;

    public SkoiceEvent(String minecraftId) {
        this.minecraftId = minecraftId;
    }

    public static HandlerList getHandlerList() {
        return SkoiceEvent.HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return SkoiceEvent.HANDLERS;
    }

    public String getMinecraftId() {
        return this.minecraftId;
    }
}
