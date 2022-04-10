/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.config;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.Bot;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    private final Skoice plugin;
    private final FileConfiguration file;

    private ConfigReader reader;
    private ConfigUpdater updater;

    public Config(Skoice plugin, FileConfiguration file) {
        this.plugin = plugin;
        this.file = file;
    }

    public FileConfiguration getFile() {
        return this.file;
    }

    public void saveFile() {
        this.plugin.saveConfig();
    }

    public void initializeReader(Bot bot) {
        this.reader = new ConfigReader(this, bot);
    }

    public ConfigReader getReader() {
        return this.reader;
    }

    public void initializeUpdater() {
        this.updater = new ConfigUpdater(this);
    }

    public ConfigUpdater getUpdater() {
        return this.updater;
    }
}
