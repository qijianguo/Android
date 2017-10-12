package com.learning.capabilities.http;

/**
 * <功能详细描述>
 *
 * @author
 * @version
 * @see
 * @since
 */
public interface ITRequestResult<T> {

    public void onSuccessful(T entity);

    public void onFailure(String errorMsg);

    public void onCompleted();
}
