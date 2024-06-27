package net.clementraynaud.skoice.api.events.player;

import net.clementraynaud.skoice.api.events.SkoiceEventDiscord;

public class PlayerProximityConnectEvent extends SkoiceEventDiscord {

    public PlayerProximityConnectEvent(String minecraftId, String discordId) {
        super(minecraftId, discordId);
    }
}
