package net.clementraynaud.skoice.platforms.velocity.logger;

import net.clementraynaud.skoice.model.logger.SkoiceLogger;
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