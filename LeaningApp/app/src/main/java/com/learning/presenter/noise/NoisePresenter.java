package com.learning.presenter.noise;

import com.learning.bean.noise.NoiseResp;
import com.learning.bridge.BridgeFactory;
import com.learning.bridge.Bridges;
import com.learning.bridge.http.OkHttpManager;
import com.learning.bridge.security.SecurityManager;
import com.learning.capabilities.http.ITRequestResult;
import com.learning.capabilities.http.Param;
import com.learning.constant.URLUtil;
import com.learning.presenter.BasePresenter;

/**
 * Description:
 * <p>
 * User: Administrator
 * Date: 2017-10-17
 * Time: 15:16
 */
public class NoisePresenter extends BasePresenter<INoiseView> {

    /**
     * 获取工地噪声信息
     *
     * @return
     */
    public void getNoiseData(int cityId) {
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
