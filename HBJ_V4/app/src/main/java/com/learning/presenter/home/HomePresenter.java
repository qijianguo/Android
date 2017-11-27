package com.learning.presenter.home;

import com.learning.bridge.BridgeFactory;
import com.learning.bridge.Bridges;
import com.learning.bridge.http.OkHttpManager;
import com.learning.bridge.security.SecurityManager;
import com.learning.presenter.BasePresenter;
import com.learning.presenter.noise.INoiseView;

/**
 * Description:
 * <p>
 * User: Administrator
 * Date: 2017-10-17
 * Time: 16:52
 */
public class HomePresenter extends BasePresenter<IHomeView> {

    public void getHomeData() {
        //网络层
        mvpView.showLoading();
        SecurityManager securityManager = BridgeFactory.getBridge(Bridges.SECURITY);
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);
        mvpView.onSuccess();

        /*httpManager.requestAsyncPostByTag(URLUtil.NOISE_LIST, getName(), new ITRequestResult<NoiseResp>() {
            @Override
            public void onCompleted() {
                mvpView.hideLoading();
            }

            @Override
            public void onSuccessful(NoiseResp entity) {
                mvpView.onSuccess();
            }

            @Override
            public void onFailure(String errorMsg) {
                mvpView.onError(errorMsg, "");
            }

        }, NoiseResp.class, new Param("cityId", cityId));*/
    }
}
