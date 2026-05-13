package software.frisby.core.concurrency;


import software.frisby.core.validation.Values;

final class ErrorOccurredManager<T> {
    private final EventSource eventSource;
    private final Object source;
    private final ErrorOccurredHandler<T> handler;

    ErrorOccurredManager(Object source,
                         EventSource eventSource,
                         ErrorOccurredHandler<T> handler) {
        Values.notNull("source", source);
        Values.notNull("eventSource", eventSource);

        this.source = source;
        this.eventSource = eventSource;
        this.handler = handler;
    }

    boolean hasHandler() {
        return null != this.handler;
    }

    void sendOnErrorNotification(Object target, T item, Throwable error) {
        this.eventSource.createErrorEvent(error);

        if (null != this.handler) {

            try {
                this.handler.onError(this.source, target, item, error);
            } catch (Exception ex) {
                this.eventSource.createOnErrorNotificationErrorEvent(ex);
            }
        }
    }
}
