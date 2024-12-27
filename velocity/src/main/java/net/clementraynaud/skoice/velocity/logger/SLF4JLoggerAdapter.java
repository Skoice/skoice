package net.clementraynaud.skoice.velocity.logger;

import net.clementraynaud.skoice.common.model.logger.SkoiceLogger;
import org.slf4j.Logger;

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
}