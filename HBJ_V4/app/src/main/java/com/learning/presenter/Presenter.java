package com.learning.presenter;

/**
 * <基础业务类>
 *
 * @author
 * @version
 * @see
 * @since
 */
public interface Presenter<V> {
    void attachView(V view);

    void detachView(V view);

    String getName();
}
