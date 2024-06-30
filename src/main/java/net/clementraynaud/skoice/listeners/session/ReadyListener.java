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

package net.clementraynaud.skoice.listeners.session;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.tasks.UpdateNetworksTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

public class ReadyListener extends ListenerAdapter {

    private final Skoice plugin;

    public ReadyListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onReady(ReadyEvent event) {

        Player tokenManager = this.plugin.getBot().getTokenManager();

        this.plugin.getBot().getJDA().retrieveApplicationInfo().queue(applicationInfo -> {
            if (applicationInfo.isBotPublic()) {
                this.handlePublicBot(tokenManager);
                return;
            }

            this.plugin.getLogger().info(this.plugin.getLang().getMessage("logger.info.bot-connected"));
            this.setup();

            if (tokenManager == null) {
                return;
            }

            if (this.plugin.getBot().getStatus() == BotStatus.READY) {
                tokenManager.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.bot-connected"));
            } else if (this.plugin.getBot().getStatus() == BotStatus.NO_GUILD) {
                this.plugin.getBot().sendNoGuildAlert(tokenManager);
            } else {
                tokenManager.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.bot-connected-incomplete-configuration-discord"));
            }
        });
    }

    private void handlePublicBot(Player tokenManager) {
        this.plugin.getConfigYamlFile().remove(ConfigField.TOKEN.toString());
        String botId = this.plugin.getBot().getJDA().getSelfUser().getApplicationId();
        this.plugin.getBot().getJDA().shutdown();
        this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.public-bot", "https://discord.com/developers/applications/" + botId + "/bot"));

        if (tokenManager == null) {
            return;
        }

        if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
            this.plugin.adventure().sender(tokenManager).sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.public-bot-interactive", this.plugin.getLang().getComponentMessage("minecraft.interaction.this-page")
                            .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("minecraft.interaction.link", "https://discord.com/developers/applications/" + botId + "/bot")))
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl("https://discord.com/developers/applications/" + botId + "/bot"))
                    )
            );
        } else {
            tokenManager.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.public-bot", "https://discord.com/developers/applications/" + botId + "/bot"));
        }
    }

    private void setup() {
        this.plugin.getBot().setDefaultAvatar();
        this.plugin.getBot().updateGuild();

        this.plugin.getBotCommands().clearGuildCommands();
        this.plugin.getBotCommands().register();
        this.plugin.getBot().getJDA().getGuilds().forEach(guild -> {
            if (guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
                guild.getPublicRole().getManager().givePermissions(Permission.USE_APPLICATION_COMMANDS).queue();
            }
        });

        this.plugin.getListenerManager().registerPermanentBotListeners();
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
                        this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                                this.plugin,
                                new UpdateNetworksTask(this.plugin)::run,
                                0,
                                10
                        ),
                0
        );

        this.plugin.getConfigYamlFile().removeInvalidVoiceChannelId();
        this.plugin.getLinksYamlFile().refreshOnlineLinkedPlayers();
        this.plugin.getListenerManager().update();

        this.plugin.getBot().retrieveNetworks();
        this.plugin.getBot().getBotVoiceChannel().setStatus();
        this.plugin.getBot().updateVoiceState();
        this.plugin.getBot().getBotVoiceChannel().muteMembers();
    }
}
