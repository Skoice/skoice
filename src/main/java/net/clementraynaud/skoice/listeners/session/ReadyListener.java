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
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.bukkit.entity.Player;

import java.time.OffsetDateTime;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ReadyListener extends ListenerAdapter {

    private final Skoice plugin;

    public ReadyListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onReady(ReadyEvent event) {

        Player tokenManager = this.plugin.getBot().getTokenManager();

        event.getJDA().retrieveApplicationInfo().queue(applicationInfo -> {
                    if (applicationInfo.isBotPublic()) {
                        this.handlePublicBot(tokenManager);
                        return;
                    }

                    this.plugin.log(Level.INFO, "chat.configuration.bot-connected");

                    if (tokenManager != null
                            && event.getJDA().getSelfUser().getTimeCreated().isBefore(OffsetDateTime.now().minusDays(1))) {
                        this.plugin.log(Level.WARNING, "chat.configuration.old-bot");
                        tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.old-bot"));
                    }

                    applicationInfo.setRequiredScopes("applications.commands");
                    this.plugin.getBot().setInviteUrl(applicationInfo.getInviteUrl(Permission.ADMINISTRATOR));

                    this.setup(tokenManager);
                }, new ErrorHandler().handle(ErrorResponse.fromCode(-1), e ->
                        this.handleParsingException(tokenManager))
        );

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
                } else if (error.getErrorResponse() == ErrorResponse.INTERACTION_ALREADY_ACKNOWLEDGED
                        || error.getErrorResponse() == ErrorResponse.UNKNOWN_INTERACTION) {
                    this.plugin.log(Level.WARNING, "logger.warning.shared-bot");
                }
            } else if (throwable instanceof PermissionException) {
                this.plugin.getListenerManager().update();
                return;
            }

            Skoice.analyticManager().getBugsnag().notify(throwable, Severity.ERROR);
            defaultFailure.accept(throwable);
        });
    }

    private void handlePublicBot(Player tokenManager) {
        this.plugin.getBot().acknowledgeStatus();
        this.plugin.getConfigYamlFile().remove(ConfigField.TOKEN.toString());
        String botId = this.plugin.getBot().getJDA().getSelfUser().getApplicationId();
        this.plugin.getLang().getFormatter().set("bot-page-url", "https://discord.com/developers/applications/" + botId + "/bot");
        this.plugin.getBot().getJDA().shutdown();
        this.plugin.getListenerManager().update();
        this.plugin.log(Level.WARNING, "chat.configuration.public-bot");

        if (tokenManager == null) {
            return;
        }

        if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
            this.plugin.adventure().sender(tokenManager).sendMessage(this.plugin.getLang()
                    .getInteractiveMessage("chat.configuration.public-bot-interactive"));
        } else {
            tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.public-bot"));
        }
    }

    private void handleParsingException(Player tokenManager) {
        this.plugin.getBot().acknowledgeStatus();
        this.plugin.getConfigYamlFile().remove(ConfigField.TOKEN.toString());
        this.plugin.getBot().getJDA().shutdown();
        this.plugin.getListenerManager().update();
        this.plugin.log(Level.WARNING, "logger.error.invalid-bot");

        if (tokenManager != null) {
            tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.invalid-bot"));
        }
    }

    private void setup(Player tokenManager) {
        this.plugin.getConfigYamlFile().removeInvalidVoiceChannelId();

        this.plugin.getBot().setDefaultAvatar();
        this.plugin.getBot().retrieveMutedUsers();
        this.plugin.getBot().getMenuFactory().loadAll(this.plugin);

        this.plugin.getBot().getJDA().getGuilds()
                .forEach(guild -> this.plugin.getBot().allowApplicationCommands(guild));
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
