package software.frisby.core.concurrency;

import software.frisby.core.validation.Strings;

import java.lang.reflect.Array;

final class EventSource {
    private static final System.Logger LOG = System.getLogger(EventSource.class.getName());

    private final String source;

    EventSource(String source) {
        Strings.notBlank("source", source);
        this.source = source;
    }

    private static String formatItemName(Object item) {
        if (null == item) {
            return "null";
        } else if (item.getClass().isArray()) {
            String name = item.getClass().getSimpleName();
            int length = Array.getLength(item);

            StringBuilder sb = new StringBuilder(name);
            sb.append("{");

            for (int i = 0; i < length; i++) {
                if (sb.length() > 512) {
                    sb.append(", ...");
                    break;
                }

                if (i > 0) {
                    sb.append(", ");
                }

                sb.append(Array.get(item, i));
            }

            sb.append("}");

            return sb.toString();
        } else {
            return item.toString();
        }
    }

    String sourceName() {
        return this.source;
    }

    // -------------------------------------------------------------------------
    // Warning messages
    // -------------------------------------------------------------------------

    void createNoTargetLinkedWarningEvent() {
        if (LOG.isLoggable(System.Logger.Level.WARNING)) {
            LOG.log(
                    System.Logger.Level.WARNING,
                    String.format(
                            "The '%s' block received an item but has no downstream target linked.  Posting is blocked until linkTo() is called.",
                            this.source
                    )
            );
        }
    }

    // -------------------------------------------------------------------------
    // Error messages
    // -------------------------------------------------------------------------

    void createOnPostedNotificationErrorEvent(Exception ex) {
        if (LOG.isLoggable(System.Logger.Level.ERROR)) {
            LOG.log(
                    System.Logger.Level.ERROR,
                    String.format(
                            "An unexpected exception occurred in the %s while invoking the ItemPostedHandler.onPosted() method.",
                            this.source
                    ),
                    ex
            );
        }
    }

    void createOnDeliveredNotificationErrorEvent(Exception ex) {
        if (LOG.isLoggable(System.Logger.Level.ERROR)) {
            LOG.log(
                    System.Logger.Level.ERROR,
                    String.format(
                            "An unexpected exception occurred in the %s while invoking the ItemDeliveredHandler.onDelivered() method.",
                            this.source
                    ),
                    ex
            );
        }
    }

    void createTargetPredicateErrorEvent(Object item, int link, Exception ex) {
        if (LOG.isLoggable(System.Logger.Level.ERROR)) {
            LOG.log(
                    System.Logger.Level.ERROR,
                    String.format(
                            "An unhandled exception occurred in the %s while invoking a predicate to determine if a" +
                                    "posted item should be forwarded to the linked target %d. Item: %s",
                            this.source,
                            link,
                            formatItemName(item)
                    ),
                    ex
            );
        }
    }

    void createErrorEvent(Throwable error) {
        if (LOG.isLoggable(System.Logger.Level.ERROR)) {
            LOG.log(
                    System.Logger.Level.ERROR,
                    String.format("An unexpected exception occurred in the %s.", this.source),
                    error
            );
        }
    }

    void createOnErrorNotificationErrorEvent(Exception ex) {
        if (LOG.isLoggable(System.Logger.Level.ERROR)) {
            LOG.log(
                    System.Logger.Level.ERROR,
                    String.format(
                            "An unexpected exception occurred in the %s while invoking the ErrorOccurredHandler.onError() method.",
                            this.source
                    ),
                    ex
            );
        }
    }

}
