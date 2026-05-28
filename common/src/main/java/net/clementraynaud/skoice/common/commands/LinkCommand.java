/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.commands;


import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.menus.EmbeddedMenu;
import net.clementraynaud.skoice.common.util.MapUtil;
import net.clementraynaud.skoice.common.util.MojangUsernameCache;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LinkCommand extends Command {

    private static final Map<String, String> discordIdCode = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    public LinkCommand(Skoice plugin, CommandExecutor executor, SlashCommandInteraction interaction) {
        super(plugin, executor, CommandInfo.LINK.isServerManagerRequired(), CommandInfo.LINK.isBotReadyRequired(), interaction);
    }

    public static Map<String, String> getDiscordIdCode() {

        return LinkCommand.discordIdCode;
    }

    @Override
    public void run() {

        String discordId = super.executor.getUser().getId();
        String linkedMinecraftId = super.plugin.getLinksYamlFile().getMinecraftIdFromDiscordId(discordId);
        if (linkedMinecraftId != null) {
            String fallback = linkedMinecraftId;
            UUID minecraftUuid = null;
            if (linkedMinecraftId != null) {
                try {
                    minecraftUuid = UUID.fromString(linkedMinecraftId);
                } catch (IllegalArgumentException ignored) {
                }
            }
            UUID uuidForLookup = minecraftUuid;
            super.interaction.deferReply(true).queue(hook ->
                    MojangUsernameCache.retrieve(super.plugin, uuidForLookup, name ->
                            new EmbeddedMenu(this.plugin.getBot())
                                    .setContent("account-already-linked",
                                            MapUtil.of("minecraft-username",
                                                    name != null ? name : fallback))
                                    .sendFollowup(hook)));
            return;
        }

        LinkCommand.discordIdCode.remove(super.executor.getUser().getId());

        String code;
        do {
            int number = LinkCommand.random.nextInt(1000000);
            code = String.format("%06d", number);
        } while (LinkCommand.discordIdCode.putIfAbsent(super.executor.getUser().getId(), code) != null);

        new EmbeddedMenu(this.plugin.getBot()).setContent("verification-code",
                        MapUtil.of("code", code))
                .reply(super.interaction);
    }
}
