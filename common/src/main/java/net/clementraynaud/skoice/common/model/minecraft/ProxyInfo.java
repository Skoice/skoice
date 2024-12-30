package net.clementraynaud.skoice.common.model.minecraft;

import net.clementraynaud.skoice.common.model.JsonModel;

public class ProxyInfo extends JsonModel {

    private final String pluginVersion;

    public ProxyInfo(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }
}
