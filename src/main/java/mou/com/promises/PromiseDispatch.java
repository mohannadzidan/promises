package mou.com.promises;

public interface PromiseDispatch<T> {
    /**
     *
     * @param resolve      callback that must be called when the promise has resolved (Promise.resolved())
     * @param errorHandler callback that must be called when an error occur (Promise.error())
     */
    void run(PromiseResolve<T> resolve, PromiseErrorHandler errorHandler);
}
