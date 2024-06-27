package net.clementraynaud.skoice.api.events.account;

import net.clementraynaud.skoice.api.events.SkoiceEventDiscord;

public class AccountLinkEvent extends SkoiceEventDiscord {

    public AccountLinkEvent(String minecraftId, String discordId) {
        super(minecraftId, discordId);
    }
}
