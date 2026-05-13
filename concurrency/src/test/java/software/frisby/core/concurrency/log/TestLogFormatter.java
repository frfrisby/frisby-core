package software.frisby.core.concurrency.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A compact JUL {@link Formatter} for test console output.
 *
 * <p>Translates raw JUL level names to their {@link System.Logger.Level} equivalents so that
 * the console output matches the level vocabulary used throughout the test suite:
 *
 * <pre>
 *   SEVERE  → ERROR
 *   WARNING → WARNING
 *   INFO    → INFO
 *   FINE    → DEBUG
 *   FINER, FINEST, and all lower values → TRACE
 * </pre>
 *
 * <p>Records from logger names that begin with {@code "java."}, {@code "javax."}, or
 * {@code "sun."} are suppressed; they originate inside the JVM and are not relevant to
 * unit test output (e.g. the {@code Runtime.exit()} record emitted at shutdown).
 *
 * <p>Output format:
 * <pre>
 *   HH:mm:ss.SSS [LEVEL  ] logger.name - message
 *   optional stack trace
 * </pre>
 */
public final class TestLogFormatter extends Formatter {
    private static final String[] INTERNAL_PREFIXES = {"java.", "javax.", "sun.", "jdk.", "org.junit."};

    private static boolean isJvmInternal(String loggerName) {
        if (null == loggerName) {
            return false;
        }

        for (String prefix : INTERNAL_PREFIXES) {
            if (loggerName.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    private static String toSystemLevel(java.util.logging.Level julLevel) {
        if (null == julLevel) {
            return "UNKNOWN";
        }

        int value = julLevel.intValue();

        if (value >= java.util.logging.Level.SEVERE.intValue()) {
            return "ERROR";
        }

        if (value >= java.util.logging.Level.WARNING.intValue()) {
            return "WARNING";
        }

        if (value >= java.util.logging.Level.INFO.intValue()) {
            return "INFO";
        }

        if (value >= java.util.logging.Level.FINE.intValue()) {
            return "DEBUG";
        }

        return "TRACE";
    }

    private static String formatThrown(LogRecord record) {
        Throwable thrown = record.getThrown();

        if (null == thrown) {
            return "";
        }

        StringWriter sw = new StringWriter();
        sw.append(System.lineSeparator());
        thrown.printStackTrace(new PrintWriter(sw));

        return sw.toString();
    }

    /**
     * Formats a {@link LogRecord} for test console output.
     *
     * @param record The log record to format.
     * @return The formatted log line, or an empty string for JVM-internal records.
     */
    @Override
    public String format(LogRecord record) {
        if (isJvmInternal(record.getLoggerName())) {
            return "";
        }

        ZonedDateTime time = ZonedDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault());
        String level = toSystemLevel(record.getLevel());
        String message = formatMessage(record);
        String thrown = formatThrown(record);

        return String.format(
                "%1$tH:%1$tM:%1$tS.%1$tL [%2$-7s] %3$s - %4$s%5$s%n",
                time,
                level,
                record.getLoggerName(),
                message,
                thrown
        );
    }
}


