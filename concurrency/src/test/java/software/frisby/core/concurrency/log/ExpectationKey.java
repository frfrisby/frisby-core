package software.frisby.core.concurrency.log;

import java.util.Objects;

/**
 * A composite key used to index {@link LogExpectation} instances by logger name and log level.
 */
final class ExpectationKey {
    private final String loggerName;
    private final System.Logger.Level level;

    ExpectationKey(String loggerName, System.Logger.Level level) {
        this.loggerName = loggerName;
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (null == o || getClass() != o.getClass()) {
            return false;
        }

        ExpectationKey that = (ExpectationKey) o;

        return Objects.equals(loggerName, that.loggerName) && level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(loggerName, level);
    }
}

