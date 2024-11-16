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

package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.bot.Bot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.EnumSet;
import java.util.function.Consumer;

public class EmbeddedMenu {

    protected final Bot bot;
    protected String menuId;
    protected String[] args = new String[0];
    protected String messageId;
    protected InteractionHook hook;

    public EmbeddedMenu(Bot bot) {
        this.bot = bot;
    }

    public EmbeddedMenu(Bot bot, String messageId) {
        this.bot = bot;
        this.messageId = messageId;
    }

    public EmbeddedMenu setContent(String menuId, String... args) {
        this.menuId = menuId;
        this.args = args;
        return this;
    }

    public void message(User user) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(this.bot.getMenuFactory().getMenu(this.menuId).build(this.args)))
                .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
    }

    public void reply(IReplyCallback interaction) {
        this.hook = interaction.getHook();
        interaction.reply(this.bot.getMenuFactory().getMenu(this.menuId).build(this.args))
                .setEphemeral(true)
                .flatMap(InteractionHook::retrieveOriginal)
                .queue(message -> this.messageId = message.getId());
    }

    public void edit(IMessageEditCallback interaction) {
        this.hook = interaction.getHook();
        interaction.editMessage(MessageEditData.fromCreateData(this.bot.getMenuFactory().getMenu(this.menuId).build(this.args)))
                .queue(null, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, e -> this.forget()));
    }

    public void editFromHook() {
        if (!this.isHookValid()) {
            return;
        }

        this.hook.editOriginal(MessageEditData.fromCreateData(this.bot.getMenuFactory().getMenu(this.menuId).build(this.args)))
                .queue(null, new ErrorHandler().handle(EnumSet.of(
                        ErrorResponse.UNKNOWN_MESSAGE,
                        ErrorResponse.INVALID_WEBHOOK_TOKEN
                ), e -> this.forget()));
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
