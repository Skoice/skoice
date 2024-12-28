package net.clementraynaud.skoice.common.model.logger;

import java.util.logging.Level;

public interface SkoiceLogger {

    void info(String message);

    void debug(String message);

    void severe(String message);

    void warning(String message);

    void log(Level level, String message);
}
