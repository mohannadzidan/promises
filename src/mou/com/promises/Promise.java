package mou.com.promises;

import javax.lang.model.type.NullType;
import java.util.List;

/**
 * @param <T> promised value type
 */
public class Promise<T> {

    ///////////////////////////////////////
    //////// CONSTRUCTORS & FIELDS ////////
    ///////////////////////////////////////

    private final PromiseDispatch<T> onDispatch;
    private PromiseResolve<T> onResolve;
    private PromiseErrorHandler onException;
    private Promise<?> next, previous;

    /**
     * @param onDispatch a callback that will be called when this promise has dispatched internally
     */
    public Promise(PromiseDispatch<T> onDispatch) {
        this.onDispatch = onDispatch;
    }

    ///////////////////////////////////////
    //////////////// PUBLIC ///////////////
    ///////////////////////////////////////
    /**
     * finds the root of this promise chain
     *
     * @param promise and promise in the chain
     * @return root
     */
    public static Promise<?> getRoot(Promise<?> promise) {
        var root = promise;
        while (root.previous != null) {
            root = root.previous;
        }
        return root;
    }

    /**
     * finds the end of this promise chain
     *
     * @param promise any promise in the chain
     * @return end
     */
    public static Promise<?> getEnd(Promise<?> promise) {
        var end = promise;
        while (end.next != null) {
            end = end.next;
        }
        return end;
    }

    /**
     * This returned promise will resolve when all of the input's promises have resolved, or if the input contains no promises.
     * It rejects immediately upon any of the input promises rejecting or non-promises throwing an error,
     * and will reject with this first rejection message / error.
     *
     * @param promises list of promises
     * @return single promise that resolves when all the input promises have resolved
     */
    public static Promise<NullType> all(List<Promise<?>> promises) {
        Promise<NullType> root = new Promise<>((resolve, errorHandler) -> {
            resolve.run(null);
        });
        if (promises == null || promises.size() == 0) return root;
        var promise = promises.get(0);
        for (int i = 1; i < promises.size(); i++) {
            promise = promise.then(promises.get(i));
        }
        promise.then(root);
        return root;
    }

    /**
     * will dispatch the first promise (the root) of the promise chain that this promise exists within
     */
    public void dispatch() {
        getRoot(this).dispatchInternally();
    }

    /**
     *
     * @param onError callback when promise resolve fail
     * @return this promise
     */
    public Promise<T> error(PromiseErrorHandler onError) {
        this.onException = onError;
        return this;
    }

    /**
     * @param onResolve callback when the promise resolved
     * @return this promise
     */
    public Promise<T> resolved(PromiseResolve<T> onResolve) {
        this.onResolve = onResolve;
        return this;
    }

    /**
     * appends a promise to the end of this promise chain
     * @param next next promise to dispatch
     * @return next promise
     */
    public Promise<?> then(Promise<?> next) {
        var nextRoot = getRoot(next);
        var thisEnd = getEnd(this);
        thisEnd.next = nextRoot;
        nextRoot.previous = thisEnd;
        return next;
    }
    ///////////////////////////////////////
    /////////////// PRIVATE ///////////////
    ///////////////////////////////////////
    private void dispatchInternally() {
        onDispatch.run(this::resolve, getChainHandler());
    }

    private PromiseErrorHandler getChainHandler() {
        Promise<?> end = this;
        PromiseErrorHandler nearestHandler = this.onException;
        while (end.next != null && nearestHandler == null) {
            end = end.next;
            nearestHandler = end.onException;
        }
        return nearestHandler;
    }

    private void resolve(T result) {
        if (onResolve != null) onResolve.run(result);
        if (next != null) next.dispatchInternally();
    }

}
