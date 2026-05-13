package software.frisby.core.concurrency;


import software.frisby.core.validation.Values;

final class ItemPostedManager<T> {
    private final EventSource eventSource;
    private final Object source;
    private final ItemPostedHandler<T> handler;

    ItemPostedManager(Object source,
                      EventSource eventSource,
                      ItemPostedHandler<T> handler) {
        Values.notNull("source", source);
        Values.notNull("eventSource", eventSource);

        this.source = source;
        this.eventSource = eventSource;
        this.handler = handler;
    }

    void sendOnPostedNotification(T input, boolean accepted) {
        if (null != this.handler) {

            try {
                this.handler.onPosted(this.source, input, accepted);
            } catch (Exception ex) {
                this.eventSource.createOnPostedNotificationErrorEvent(ex);
            }
        }
    }
}
