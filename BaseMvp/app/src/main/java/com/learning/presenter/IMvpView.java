package com.learning.presenter;

/**
 * <Presenter逻辑与View视图交互的公共接口>
 *
 * @author
 * @version
 * @see
 * @since
 */
public interface IMvpView {
    /**
     * 失败
     *
     * @param errorMsg
     * @param code
     */
    void onError(String errorMsg, String code);

    /**
     * 成功
     */
    void onSuccess();

    /**
     * 加载动画
     */
    void showLoading();

    /**
     * 隐藏动画
     */
    void hideLoading();
}
