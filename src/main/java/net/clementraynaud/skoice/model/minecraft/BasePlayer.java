package net.clementraynaud.skoice.model.minecraft;

import net.clementraynaud.skoice.util.ComponentUtil;
import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.UUID;

public abstract class BasePlayer extends SkoiceCommandSender {

    protected UUID id;

    protected BasePlayer(UUID id) {
        this.id = id;
    }

    public UUID getUniqueId() {
        return this.id;
    }

    public abstract void sendActionBar(Component message);

    public void sendActionBar(String message) {
        this.sendActionBar(ComponentUtil.translateAlternateColorCodes(message));
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof BasePlayer that)) {
            return false;
        }

        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {
        return "BasePlayer{" +
                "id=" + this.id +
                '}';
    }
}
