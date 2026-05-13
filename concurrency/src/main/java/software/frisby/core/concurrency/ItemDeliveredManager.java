package software.frisby.core.concurrency;

import software.frisby.core.validation.Values;

final class ItemDeliveredManager<T> {
    private final EventSource eventSource;
    private final Object source;
    private final ItemDeliveredHandler<T> handler;

    ItemDeliveredManager(Object source,
                         EventSource eventSource,
                         ItemDeliveredHandler<T> handler) {
        Values.notNull("source", source);
        Values.notNull("eventSource", eventSource);

        this.source = source;
        this.eventSource = eventSource;
        this.handler = handler;
    }

    void sendOnDeliveredNotification(Object target, T output) {
        if (null != this.handler) {

            try {
                this.handler.onDelivered(this.source, target, output);
            } catch (Exception ex) {
                this.eventSource.createOnDeliveredNotificationErrorEvent(ex);
            }
        }
    }
}
