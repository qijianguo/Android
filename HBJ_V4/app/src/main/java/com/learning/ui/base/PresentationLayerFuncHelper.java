package com.learning.ui.base;

import android.content.Context;
import android.view.View;

import com.learning.util.ToastUtil;

/**
 * <页面基础公共功能实现: 页面交互的的具体实现>
 *
 * @author
 * @version
 * @see
 * @since
 */
public class PresentationLayerFuncHelper implements PresentationLayerFunc {

    private Context context;

    public PresentationLayerFuncHelper(Context context) {
        this.context = context;
    }

    @Override
    public void showToast(String msg) {
        ToastUtil.makeText(context, msg);
    }

    @Override
    public void showProgressDialog() {
    }

    @Override
    public void hideProgressDialog() {
    }

    @Override
    public void showSoftKeyboard(View focusView) {
    }

    @Override
    public void hideSoftKeyboard() {
    }
}
