package mou.com.promises;

/**
 *
 * @param <T> promised value type
 */
public interface PromiseResolve<T> {
    /**
     *
     * @param result the result of the promise
     */
    void run(T result);
}
