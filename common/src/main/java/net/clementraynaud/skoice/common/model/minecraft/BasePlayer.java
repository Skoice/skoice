/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 *
 * This file is part of Skoice.
 *
 * Skoice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skoice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skoice.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.clementraynaud.skoice.common.model.minecraft;

import net.clementraynaud.skoice.common.util.ComponentUtil;
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
        if (!(o instanceof BasePlayer)) {
            return false;
        }

        BasePlayer that = (BasePlayer) o;
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
