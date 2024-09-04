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

import com.bugsnag.Severity;
import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

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

            applicationInfo.setRequiredScopes("applications.commands");
            this.plugin.getBot().setInviteUrl(applicationInfo.getInviteUrl(Permission.ADMINISTRATOR));

            this.setup(tokenManager);
        });

        this.setDefaultFailure();
    }

    private void setDefaultFailure() {
        Consumer<? super Throwable> defaultFailure = RestAction.getDefaultFailure();
        RestAction.setDefaultFailure(throwable -> {
            if (throwable instanceof ErrorResponseException) {
                ErrorResponseException error = (ErrorResponseException) throwable;
                if (error.getErrorCode() == ErrorResponse.MISSING_PERMISSIONS.getCode()
                        || error.getErrorCode() == ErrorResponse.MISSING_ACCESS.getCode()
                        || error.getErrorCode() == ErrorResponse.MFA_NOT_ENABLED.getCode()) {
                    this.plugin.getListenerManager().update();
                    return;
                }
            } else if (throwable instanceof PermissionException) {
                this.plugin.getListenerManager().update();
                return;
            }

            this.plugin.getAnalyticManager().getBugsnag().notify(throwable, Severity.ERROR);
            defaultFailure.accept(throwable);
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
            this.plugin.adventure().sender(tokenManager).sendMessage(this.plugin.getLang().getMessage("chat.configuration.public-bot-interactive", this.plugin.getLang().getComponentMessage("interaction.this-page")
                            .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("interaction.link", "https://discord.com/developers/applications/" + botId + "/bot")))
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl("https://discord.com/developers/applications/" + botId + "/bot"))
                    )
            );
        } else {
            tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.public-bot", "https://discord.com/developers/applications/" + botId + "/bot"));
        }
    }

    private void setup(Player tokenManager) {
        this.plugin.getConfigYamlFile().removeInvalidVoiceChannelId();

        this.plugin.getBot().setDefaultAvatar();
        this.plugin.getBot().retrieveMutedUsers();
        this.plugin.getBot().getMenuFactory().loadAll(this.plugin);

        this.plugin.getBot().getJDA().getGuilds().forEach(guild -> {
            if (guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR)
                    && (guild.getRequiredMFALevel() != Guild.MFALevel.TWO_FACTOR_AUTH
                    || this.plugin.getBot().getJDA().getSelfUser().isMfaEnabled())) {
                guild.getPublicRole().getManager().givePermissions(Permission.USE_APPLICATION_COMMANDS).queue();
            }
        });
        this.plugin.getBot().getCommands().clearGuildCommands();
        this.plugin.getBot().getCommands().register()
                .thenRun(() -> {
                    this.plugin.getListenerManager().registerPermanentBotListeners();
                    this.plugin.getListenerManager().update();

                    this.plugin.getBot().retrieveProximityChannels();

                    if (tokenManager == null) {
                        return;
                    }

                    if (this.plugin.getBot().getStatus() == BotStatus.READY) {
                        tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.bot-connected"));
                    } else if (this.plugin.getBot().getStatus() == BotStatus.NO_GUILD) {
                        this.plugin.getBot().sendNoGuildAlert(tokenManager);
                    } else {
                        tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.bot-connected-incomplete-configuration-discord"));
                    }
                });
    }
}
