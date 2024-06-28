package net.clementraynaud.skoice.api.events;

public class SkoiceEventDiscord extends SkoiceEvent {

    private final String discordId;

    public SkoiceEventDiscord(String minecraftId, String discordId) {
        super(minecraftId);
        this.discordId = discordId;
    }

    public String getDiscordId() {
        return this.discordId;
    }
}
