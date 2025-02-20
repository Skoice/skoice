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

package net.clementraynaud.skoice.common.listeners.session;

import com.bugsnag.Severity;
import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.kyori.adventure.text.event.HoverEvent;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

public class ReadyListener extends ListenerAdapter {

    public static final String DISCORD_APPLICATIONS_PANEL = "https://discord.com/developers/applications/";
    private final Skoice plugin;

    public ReadyListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onReady(ReadyEvent event) {

        BasePlayer tokenManager = this.plugin.getBot().getTokenManager();

        event.getJDA().retrieveApplicationInfo().queue(applicationInfo -> {
                    if (applicationInfo.isBotPublic()) {
                        this.handlePublicBot(tokenManager);
                        return;
                    }

                    this.plugin.getLogger().info(this.plugin.getLang().getMessage("logger.info.bot-connected"));

                    if (tokenManager != null
                            && event.getJDA().getSelfUser().getTimeCreated().isBefore(OffsetDateTime.now().minusDays(1))) {
                        this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.old-bot"));
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
                }
                if (error.getErrorCode() == ErrorResponse.INTERACTION_ALREADY_ACKNOWLEDGED.getCode() || (error.getErrorCode() == ErrorResponse.UNKNOWN_INTERACTION.getCode() && error.getMessage().startsWith("10062: Failed to acknowledge this interaction, this can be due to 2 reasons"))) {
                    this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.shared-bot", "https://github.com/Skoice/skoice/wiki/Creating-a-Discord-Bot-for-Skoice"));
                    return;
                }
            } else if (throwable instanceof PermissionException) {
                this.plugin.getListenerManager().update();
                return;
            }

            Skoice.analyticManager().getBugsnag().notify(throwable, Severity.ERROR);
            defaultFailure.accept(throwable);
        });
    }

    private void handlePublicBot(BasePlayer tokenManager) {
        this.plugin.getBot().acknowledgeStatus();
        this.plugin.getConfigYamlFile().remove(ConfigField.TOKEN.toString());
        String botId = this.plugin.getBot().getJDA().getSelfUser().getApplicationId();
        this.plugin.getBot().getJDA().shutdown();
        this.plugin.getListenerManager().update();
        this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.error.public-bot", DISCORD_APPLICATIONS_PANEL + botId + "/bot"));

        if (tokenManager == null) {
            return;
        }

        if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
            tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.public-bot-interactive", this.plugin.getLang().getComponentMessage("interaction.this-page")
                            .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("interaction.link", DISCORD_APPLICATIONS_PANEL + botId + "/bot")))
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl(DISCORD_APPLICATIONS_PANEL + botId + "/bot"))
                    )
            );
        } else {
            tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.public-bot", DISCORD_APPLICATIONS_PANEL + botId + "/bot"));
        }
    }

    private void handleParsingException(BasePlayer tokenManager) {
        this.plugin.getBot().acknowledgeStatus();
        this.plugin.getConfigYamlFile().remove(ConfigField.TOKEN.toString());
        this.plugin.getBot().getJDA().shutdown();
        this.plugin.getListenerManager().update();
        this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.error.invalid-bot"));

        if (tokenManager != null) {
            tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.invalid-bot"));
        }
    }

    private void setup(BasePlayer tokenManager) {
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
