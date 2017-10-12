package com.learning.presenter.personcenter;

import com.learning.bean.home.LoginResp;
import com.learning.presenter.BasePresenter;
import com.learning.bridge.BridgeFactory;
import com.learning.bridge.Bridges;
import com.learning.bridge.cache.sharePref.EBSharedPrefManager;
import com.learning.bridge.cache.sharePref.EBSharedPrefUser;
import com.learning.bridge.http.OkHttpManager;
import com.learning.bridge.security.SecurityManager;
import com.learning.capabilities.http.ITRequestResult;
import com.learning.capabilities.http.Param;
import com.learning.constant.URLUtil;

/**
 * <功能详细描述>
 *
 * @author
 * @version
 * @see
 * @since
 */
public class LoginPresenter extends BasePresenter<IUserLoginView> {

    public LoginPresenter() {

    }

    public void login(String useName, String password) {
        //网络层
        mvpView.showLoading();
        SecurityManager securityManager = BridgeFactory.getBridge(Bridges.SECURITY);
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);

        httpManager.requestAsyncPostByTag(URLUtil.USER_LOGIN, getName(), new ITRequestResult<LoginResp>() {
                    @Override
                    public void onCompleted() {
                        mvpView.hideLoading();
                    }

                    @Override
                    public void onSuccessful(LoginResp entity) {
                        mvpView.onSuccess();
                        EBSharedPrefManager manager = BridgeFactory.getBridge(Bridges.SHARED_PREFERENCE);
                        manager.getKDPreferenceUserInfo().saveString(EBSharedPrefUser.USER_NAME, "abc");
                    }

                    @Override
                    public void onFailure(String errorMsg) {
                        mvpView.onError(errorMsg, "");
                    }

                }, LoginResp.class, new Param("username", useName),
                new Param("pas", securityManager.get32MD5Str(password)));
    }
}
