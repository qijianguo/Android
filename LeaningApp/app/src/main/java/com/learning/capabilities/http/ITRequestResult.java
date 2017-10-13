package com.learning.capabilities.http;

/**
 * <功能详细描述: 用于处理网络请求后的回调, 接口的回调在主线程中>
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
