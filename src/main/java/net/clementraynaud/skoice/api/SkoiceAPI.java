package net.clementraynaud.skoice.api;

import net.clementraynaud.skoice.Skoice;

import java.util.Map;
import java.util.UUID;

public class SkoiceAPI {

    private final Skoice plugin;

    public SkoiceAPI(Skoice plugin) {
        this.plugin = plugin;
    }

    public Map<String, String> getLinkedAccounts() {
        return this.plugin.getLinksYamlFile().getLinks();
    }

    public boolean linkUser(UUID minecraftId, String discordId) {
        if (this.plugin.getLinksYamlFile().getLinks().containsKey(minecraftId.toString())) {
            return false;
        }
        this.plugin.getLinksYamlFile().linkUserDirectly(minecraftId.toString(), discordId);
        return true;
    }

    public boolean unlinkUser(UUID minecraftId) {
        if (this.plugin.getLinksYamlFile().getLinks().get(minecraftId.toString()) == null) {
            return false;
        }
        this.plugin.getLinksYamlFile().unlinkUserDirectly(minecraftId.toString());
        return true;
    }
}
