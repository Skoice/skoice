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

package net.clementraynaud.skoice.common.lang;

public class DiscordLang extends Lang {

    public static final int MAX_SHORT_TEXT_INPUT_VALUE_LENGTH = 25;

    @Override
    protected String getPath(LangInfo langInfo) {
        return "discord/lang/" + langInfo + ".yml";
    }

    public String getMessage(String path, int maxLength) {
        String message = super.getMessage(path);
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength - 1) + "…";
    }

    @Override
    protected void loadFormatter() {
        super.formatter.set("spigotmc-url", "https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861");
        super.formatter.set("open-collective-url", "https://opencollective.com/skoice");
        super.formatter.set("github-url", "https://github.com/Skoice/skoice");
        super.formatter.set("crowdin-url", "https://crowdin.com/project/skoice");
        super.formatter.set("creation-guide-url", "https://github.com/Skoice/skoice/wiki/Creating-a-Discord-Bot-for-Skoice");

        super.formatter.set("link-minecraft-command", "/skoice link");
    }
}
