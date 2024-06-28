package net.clementraynaud.skoice.api.events.player;

import net.clementraynaud.skoice.api.events.SkoiceEventDiscord;

public class PlayerProximityDisconnectEvent extends SkoiceEventDiscord {

    public PlayerProximityDisconnectEvent(String minecraftId, String discordId) {
        super(minecraftId, discordId);
    }
}
