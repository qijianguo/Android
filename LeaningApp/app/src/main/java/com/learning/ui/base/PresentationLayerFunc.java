package com.learning.ui.base;

import android.view.View;

/**
 * <页面交互封装>
 *
 * @author
 * @version
 * @see
 * @since
 */
public interface PresentationLayerFunc {
    /**
     * 弹出消息
     *
     * @param msg
     */
    public void showToast(String msg);

    /**
     * 网络请求加载框
     */
    public void showProgressDialog();

    /**
     * 隐藏网络请求加载框
     */
    public void hideProgressDialog();

    /**
     * 显示软键盘
     *
     * @param focusView
     */
    public void showSoftKeyboard(View focusView);

    /**
     * 隐藏软键盘
     */
    public void hideSoftKeyboard();
}
