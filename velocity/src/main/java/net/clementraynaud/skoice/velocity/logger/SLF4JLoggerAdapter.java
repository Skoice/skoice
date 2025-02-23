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

package net.clementraynaud.skoice.velocity.logger;

import net.clementraynaud.skoice.common.model.logger.SkoiceLogger;
import org.slf4j.Logger;

import java.util.logging.Level;

public class SLF4JLoggerAdapter implements SkoiceLogger {

    private final Logger logger;

    public SLF4JLoggerAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        this.logger.info(message);
    }

    @Override
    public void debug(String message) {
        this.logger.debug(message);
    }

    @Override
    public void severe(String message) {
        this.logger.error(message);
    }

    @Override
    public void warning(String message) {
        this.logger.warn(message);
    }

    @Override
    public void log(Level level, String message) {
        if (level == Level.SEVERE) {
            this.logger.error(message);
        } else if (level == Level.WARNING) {
            this.logger.warn(message);
        } else if (level == Level.INFO) {
            this.logger.info(message);
        } else if (level == Level.CONFIG || level == Level.FINE) {
            this.logger.debug(message);
        } else if (level == Level.FINER || level == Level.FINEST) {
            this.logger.trace(message);
        } else {
            this.logger.info(message);
        }
    }
}