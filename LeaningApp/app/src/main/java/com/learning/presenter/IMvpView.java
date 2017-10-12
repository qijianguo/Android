package com.learning.presenter;

/**
 * <功能详细描述>
 *
 * @author
 * @version
 * @see
 * @since
 */
public interface IMvpView {
    void onError(String errorMsg, String code);

    void onSuccess();

    void showLoading();

    void hideLoading();
}
