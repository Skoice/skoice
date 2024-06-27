package net.clementraynaud.skoice.api.events.account;

import net.clementraynaud.skoice.api.events.SkoiceEvent;

public class AccountUnlinkEvent extends SkoiceEvent {

    public AccountUnlinkEvent(String minecraftId) {
        super(minecraftId);
    }
}
