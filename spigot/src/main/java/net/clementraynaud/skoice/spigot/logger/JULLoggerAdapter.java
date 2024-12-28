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