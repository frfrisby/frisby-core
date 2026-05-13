package software.frisby.core.concurrency.log;

import software.frisby.core.validation.TrimmedStrings;
import software.frisby.core.validation.Values;

/**
 * Holds a logger name paired with the {@link System.Logger.Level} it should be set to for the
 * duration of a single test.  Used by {@link DefaultSystemLogVerifier} to temporarily override
 * the effective log level of a specific logger and restore it on {@link SystemLogVerifier#close()}.
 */
final class LoggerLevelConfig {
    private final String loggerName;
    private final System.Logger.Level level;

    LoggerLevelConfig(String loggerName, System.Logger.Level level) {
        this.loggerName = TrimmedStrings.notBlank("loggerName", loggerName);
        this.level = Values.notNull("level", level);
    }

    String loggerName() {
        return loggerName;
    }

    System.Logger.Level level() {
        return level;
    }
}

