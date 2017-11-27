package com.learning.presenter.personcenter;

import com.learning.presenter.IMvpView;

/**
 * <Presenter逻辑与View视图交互的扩展接口>
 *
 * @author
 * @version
 * @see
 * @since
 */
public interface ILoginView extends IMvpView {

    /**
     * 登录成功后清除输入框内容
     */
    void clearEditContent();

}
