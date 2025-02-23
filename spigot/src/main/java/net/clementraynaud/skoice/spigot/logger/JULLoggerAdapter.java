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

package net.clementraynaud.skoice.spigot.logger;

import net.clementraynaud.skoice.common.model.logger.SkoiceLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JULLoggerAdapter implements SkoiceLogger {

    private final Logger logger;

    public JULLoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        this.logger.info(message);
    }

    @Override
    public void debug(String message) {
        this.logger.fine(message);
    }

    @Override
    public void severe(String message) {
        this.logger.severe(message);
    }

    @Override
    public void warning(String message) {
        this.logger.warning(message);
    }

    @Override
    public void log(Level level, String message) {
        this.logger.log(level, message);
    }
}