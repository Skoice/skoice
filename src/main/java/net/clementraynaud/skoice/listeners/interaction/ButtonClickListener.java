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

package net.clementraynaud.skoice.listeners.interaction;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ButtonClickListener extends ListenerAdapter {

    private static final Map<String, String> discordIDAxis = new HashMap<>();

    private final Skoice plugin;

    public ButtonClickListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (event.getButton() != null && event.getButton().getId() != null) {
                String buttonID = event.getButton().getId();
                if (this.plugin.getConfigurationMenu().exists()
                        && this.plugin.getConfigurationMenu().getMessageId().equals(event.getMessage().getId())) {
                    if (buttonID.equals(Menu.CLOSE_BUTTON_ID)) {
                        event.getMessage().delete().queue();
                        if (!this.plugin.getBot().isReady()) {
                            event.reply(new Menu(this.plugin, "empty-configuration",
                                            Collections.singleton(this.plugin.getBot().getFields().get("incomplete-configuration-server-manager")),
                                            MenuType.ERROR)
                                            .toMessage())
                                    .setEphemeral(true).queue();
                        }
                    } else if (!this.plugin.getBot().isReady() && !"language".equals(buttonID)) {
                        event.editMessage(this.plugin.getConfigurationMenu().getMessage()).queue();
                    } else {
                        if ("mode".equals(buttonID)) {
                            ButtonClickListener.discordIDAxis.remove(member.getId());
                        } else if ("horizontal-radius".equals(buttonID)) {
                            ButtonClickListener.discordIDAxis.put(member.getId(), ConfigField.HORIZONTAL_RADIUS.get());
                        } else if ("vertical-radius".equals(buttonID)) {
                            ButtonClickListener.discordIDAxis.put(member.getId(), ConfigField.VERTICAL_RADIUS.get());
                        }
                        event.editMessage(this.plugin.getBot().getMenus().get(buttonID).toMessage()).queue();
                    }
                } else if (event.getMessage().getAuthor().equals(event.getJDA().getSelfUser())
                    && "resume-configuration".equals(event.getButton().getId())) {
                    event.reply(this.plugin.getConfigurationMenu().getMessage()).queue();
                }
            }
        } else {
            event.reply(new Menu(this.plugin, "error",
                    Collections.singleton(this.plugin.getBot().getFields().get("access-denied")))
                    .toMessage()).setEphemeral(true).queue();
        }
    }

    public static Map<String, String> getDiscordIDAxis() {
        return ButtonClickListener.discordIDAxis;
    }
}
