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

package net.clementraynaud.skoice.common.menus.selectors;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.menus.MenuEmoji;
import net.clementraynaud.skoice.common.system.ProximityChannel;
import net.clementraynaud.skoice.common.system.ProximityChannels;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VoiceChannelSelector extends Selector {

    public VoiceChannelSelector(Skoice plugin) {
        super(plugin);
    }

    @Override
    public SelectMenu get() {
        List<SelectOption> options = new ArrayList<>();

        options.add(SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.voice-channel.select-menu.select-option.new-voice-channel.label"), "new-voice-channel")
                .withDescription(super.plugin.getBot().getLang().getMessage("menu.voice-channel.select-menu.select-option.new-voice-channel.description"))
                .withEmoji(MenuEmoji.HEAVY_PLUS_SIGN.get()));

        int channelLimit = SelectMenu.OPTIONS_MAX_AMOUNT - 2;

        VoiceChannel selectedChannel = super.plugin.getConfigYamlFile().getVoiceChannel();
        if (selectedChannel != null && selectedChannel.getParentCategory() != null) {
            options.add(SelectOption.of(selectedChannel.getName(), selectedChannel.getId())
                    .withDescription(selectedChannel.getParentCategory().getName())
                    .withEmoji(MenuEmoji.SOUND.get()));
            channelLimit--;
        }

        Set<String> proximityChannels = ProximityChannels.getInitialized().stream()
                .map(ProximityChannel::getChannelId)
                .collect(Collectors.toSet());

        super.plugin.getBot().getGuild().getVoiceChannels().stream()
                .filter(voiceChannel -> voiceChannel.getParentCategory() != null)
                .filter(voiceChannel -> !voiceChannel.equals(selectedChannel))
                .filter(voiceChannel -> !proximityChannels.contains(voiceChannel.getId()))
                .limit(channelLimit)
                .forEach(voiceChannel -> options.add(SelectOption.of(voiceChannel.getName(), voiceChannel.getId())
                        .withDescription(voiceChannel.getParentCategory().getName())
                        .withEmoji(MenuEmoji.SOUND.get())));

        if (options.size() == SelectMenu.OPTIONS_MAX_AMOUNT - 1) {
            options.add(SelectOption.of(super.plugin.getBot().getLang().getMessage("select-option.too-many-options.label"), "refresh")
                    .withDescription(super.plugin.getBot().getLang().getMessage("select-option.too-many-options.description"))
                    .withEmoji(MenuEmoji.WARNING.get()));
        }

        if (selectedChannel != null) {
            return StringSelectMenu.create("voice-channel-selection")
                    .addOptions(options)
                    .setDefaultValues(selectedChannel.getId()).build();
        } else {
            return StringSelectMenu.create("voice-channel-selection")
                    .setPlaceholder(super.plugin.getBot().getLang().getMessage("menu.voice-channel.select-menu.placeholder"))
                    .addOptions(options).build();
        }
    }
}
