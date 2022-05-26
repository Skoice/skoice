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

package net.clementraynaud.skoice.listeners.message.guild;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.listeners.interaction.ButtonClickListener;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMessageReceivedListener extends ListenerAdapter {

    private final Skoice plugin;

    public GuildMessageReceivedListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String discordID = event.getAuthor().getId();
        if (discordID.equals(event.getJDA().getSelfUser().getApplicationId())) {
            if (!event.getMessage().isEphemeral()) {
                this.plugin.getConfigurationMenu().storeInConfig(event.getMessage());
            }
        } else if (ButtonClickListener.discordIDAxis.containsKey(event.getAuthor().getId())
                && event.getMessage().getContentRaw().length() <= 4
                && event.getMessage().getContentRaw().matches("[0-9]+")) {
            int value = Integer.parseInt(event.getMessage().getContentRaw());
            if (value >= 1 && value <= 1000) {
                event.getMessage().delete().queue();
                this.plugin.readConfig().getFile().set(ButtonClickListener.discordIDAxis.get(event.getAuthor().getId()), value);
                this.plugin.readConfig().saveFile();
                this.plugin.getConfigurationMenu().deleteMessage();
                if (ButtonClickListener.discordIDAxis.get(event.getAuthor().getId()).equals(ConfigField.HORIZONTAL_RADIUS.get())) {
                    event.getChannel().sendMessage(this.plugin.getBot().getMenus().get("horizontal-radius").toMessage()).queue();
                } else if (ButtonClickListener.discordIDAxis.get(event.getAuthor().getId()).equals(ConfigField.VERTICAL_RADIUS.get())) {
                    event.getChannel().sendMessage(this.plugin.getBot().getMenus().get("vertical-radius").toMessage()).queue();
                }
            }
        }
    }
}
