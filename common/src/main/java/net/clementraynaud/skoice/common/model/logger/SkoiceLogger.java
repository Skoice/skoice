package net.clementraynaud.skoice.common.model.logger;

public interface SkoiceLogger {

    void info(String message);

    void debug(String message);

    void severe(String message);

    void warning(String message);
}
