/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.listeners.guild.override;

import net.clementraynaud.skoice.Skoice;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.override.GenericPermissionOverrideEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GenericPermissionOverrideListener extends ListenerAdapter {

    private final Skoice plugin;

    public GenericPermissionOverrideListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGenericPermissionOverride(GenericPermissionOverrideEvent event) {
        VoiceChannel mainVoiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();
        if (mainVoiceChannel == null) {
            return;
        }
        if (event.getChannel().equals(mainVoiceChannel)) {
            this.plugin.getBot().getVoiceChannel().updatePermissions();
        }
    }
}
