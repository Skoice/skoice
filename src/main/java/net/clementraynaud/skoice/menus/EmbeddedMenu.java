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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EmbeddedMenu {

    protected final Bot bot;
    protected String menuId;
    protected String[] args = new String[0];
    protected InteractionHook hook;

    public EmbeddedMenu(Bot bot) {
        this.bot = bot;
    }

    public EmbeddedMenu setContent(String menuId, String... args) {
        this.menuId = menuId;
        this.args = args;
        return this;
    }

    public void message(User user) {
        user.openPrivateChannel().queue(channel ->
                channel.sendMessage(this.bot.getMenuFactory().getMenu(this.menuId)
                                .build(this.args))
                        .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER))
        );
    }

    public void reply(IReplyCallback interaction) {
        this.hook = interaction.getHook();
        interaction.reply(this.bot.getMenuFactory().getMenu(this.menuId).build(this.args))
                .setEphemeral(true).queue();
    }

    public void edit(IMessageEditCallback interaction) {
        interaction.editMessage(MessageEditData.fromCreateData(this.bot.getMenuFactory().getMenu(this.menuId).build(this.args))).queue();
    }

    public void editFromHook() {
        if (this.hook == null || this.hook.isExpired()) {
            return;
        }

        this.hook.editOriginal(MessageEditData.fromCreateData(this.bot.getMenuFactory().getMenu(this.menuId).build(this.args))).queue();
    }

    public void deleteFromHook(Consumer<Void> success) {
        if (this.hook == null || this.hook.isExpired()) {
            return;
        }

        this.hook.deleteOriginal().queue(success, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
        this.hook = null;
    }

    public String getId() {
        return this.menuId;
    }
}
