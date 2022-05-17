/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.listeners.channel.voice.lobby.update;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.menus.ConfigurationMenu;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateParentEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class VoiceChannelUpdateParentListener extends ListenerAdapter {

    private final Skoice plugin;
    private final Config config;
    private final Lang lang;
    private final Bot bot;

    public VoiceChannelUpdateParentListener(Skoice plugin, Config config, Lang lang, Bot bot) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.bot = bot;
    }

    @Override
    public void onVoiceChannelUpdateParent(VoiceChannelUpdateParentEvent event) {
        if (event.getChannel().getId().equals(this.config.getFile().getString(ConfigField.LOBBY_ID.get()))) {
        //    new ConfigurationMenu(this.plugin, this.config, this.lang, this.bot).sendLobbyDeletedAlert(event.getGuild());
        }
    }
}
