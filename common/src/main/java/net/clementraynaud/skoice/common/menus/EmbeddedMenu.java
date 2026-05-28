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

package net.clementraynaud.skoice.common.menus;

import net.clementraynaud.skoice.common.bot.Bot;
import net.clementraynaud.skoice.common.util.MapUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EmbeddedMenu {

    protected final Bot bot;
    protected String menuId;
    protected Map<String, String> args;
    protected String messageId;
    protected InteractionHook hook;
    protected String invokerUserId;

    public EmbeddedMenu(Bot bot) {
        this.bot = bot;
    }

    public EmbeddedMenu(Bot bot, String messageId) {
        this.bot = bot;
        this.messageId = messageId;
    }

    public EmbeddedMenu setContent(String menuId, Map<String, String> args) {
        this.menuId = menuId;
        this.args = args;
        return this;
    }

    public EmbeddedMenu setContent(String menuId) {
        return this.setContent(menuId, MapUtil.of());
    }

    public void message(User user) {
        this.invokerUserId = user.getId();
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(this.buildMessage()))
                .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER, ErrorResponse.OPEN_DM_TOO_FAST));
    }

    public void reply(IReplyCallback interaction) {
        this.hook = interaction.getHook();
        this.invokerUserId = interaction.getUser().getId();
        interaction.reply(this.buildMessage())
                .setEphemeral(true)
                .flatMap(InteractionHook::retrieveOriginal)
                .queue(message -> this.messageId = message.getId());
    }

    public void edit(IMessageEditCallback interaction) {
        this.hook = interaction.getHook();
        this.invokerUserId = interaction.getUser().getId();
        interaction.editMessage(MessageEditData.fromCreateData(this.buildMessage()))
                .queue(null, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, e -> this.forget()));
    }

    public void sendFollowup(InteractionHook hook) {
        this.hook = hook;
        if (this.invokerUserId == null) {
            this.invokerUserId = hook.getInteraction().getUser().getId();
        }
        hook.editOriginal(MessageEditData.fromCreateData(this.buildMessage()))
                .queue(message -> this.messageId = message.getId(),
                        new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.INVALID_WEBHOOK_TOKEN));
    }

    public void editFromHook() {
        if (!this.isHookValid()) {
            return;
        }

        this.hook.editOriginal(MessageEditData.fromCreateData(this.buildMessage()))
                .queue(null, new ErrorHandler().handle(EnumSet.of(
                        ErrorResponse.UNKNOWN_MESSAGE,
                        ErrorResponse.INVALID_WEBHOOK_TOKEN
                ), e -> this.forget()));
    }

    private MessageCreateData buildMessage() {
        Map<String, String> renderArgs = this.args != null ? new HashMap<>(this.args) : new HashMap<>();
        String ownerId = this.bot.getOwnerId();
        if (ownerId != null) {
            renderArgs.put("bot-owner-mention", "<@" + ownerId + ">");
        }
        renderArgs.put("invoker-is-bot-owner",
                String.valueOf(ownerId != null && ownerId.equals(this.invokerUserId)));
        return this.bot.getMenuFactory().getMenu(this.menuId).build(renderArgs);
    }

    protected boolean isHookValid() {
        return this.hook != null && !this.hook.isExpired();
    }

    public void delete(IMessageEditCallback interaction, Consumer<Void> success) {
        interaction.getHook().deleteOriginal()
                .queue(success.andThen(e -> this.forget()),
                        new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, e -> this.forget())
                );
    }

    protected void forget() {
    }

    public String getId() {
        return this.menuId;
    }
}
