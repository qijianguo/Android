package com.learning.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.learning.BaseApplication;
import com.learning.R;
import com.learning.bridge.BridgeFactory;
import com.learning.bridge.Bridges;
import com.learning.bridge.http.OkHttpManager;
import com.learning.constant.Event;
import com.learning.presenter.BasePresenter;
import com.learning.presenter.IMvpView;

import de.greenrobot.event.EventBus;

/**
 * <基础activity>
 *
 *
 * @author
 * @see
 */
public abstract class BaseActivity extends Activity implements CreateInit, PublishActivityCallBack, PresentationLayerFunc, IMvpView, OnClickListener {

    private PresentationLayerFuncHelper presentationLayerFuncHelper;

    /**
     * 返回按钮
     */
    private LinearLayout back;

    /**
     * 标题，右边字符
     */
    protected TextView title, right;

    public BasePresenter presenter;

    public final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 页面交互初始化
        presentationLayerFuncHelper = new PresentationLayerFuncHelper(this);

        initViews();
        initListeners();
        initData();
        setHeader();
        BaseApplication baseApplication = BaseApplication.baseApplication;
        baseApplication.addActivity(this);
        EventBus.getDefault().register(this);       // 接收消息注册
    }

    @Override
    public void setHeader() {
        back = (LinearLayout) findViewById(R.id.ll_back);
        title = (TextView) findViewById(R.id.tv_title);
        right = (TextView) findViewById(R.id.tv_right);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                finish();
                break;
        }
    }

    /**
     * EventBus使用: 在接受消息的页面实现此方法
     * 接收页面的Event的值跟EventBus.getDefault().post(Event.IMAGE_LOADER_SUCCESS);的发送的值相同，
     * 则此页面就接收到通知，如果多个页面都有同一个event，则这几个页面都可以收到通知。
     *
     * @param event
     */
    public void onEventMainThread(Event event) {

    }

    @Override
    protected void onResume() {
        BaseApplication.baseApplication.currentActivityName = this.getClass().getName();
        super.onResume();
    }

    @Override
    public void startActivity(Class<?> openClass, Bundle bundle) {
        Intent intent = new Intent(this, openClass);
        if (null != bundle)
            intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void openActivityForResult(Class<?> openClass, int requestCode, Bundle bundle) {
        Intent intent = new Intent(this, openClass);
        if (null != bundle)
            intent.putExtras(bundle);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void setResultOk(Bundle bundle) {
        Intent intent = new Intent();
        if (bundle != null) ;
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void showToast(String msg) {
        presentationLayerFuncHelper.showToast(msg);
    }

    @Override
    public void showProgressDialog() {
        presentationLayerFuncHelper.showProgressDialog();
    }

    @Override
    public void hideProgressDialog() {
        presentationLayerFuncHelper.hideProgressDialog();
    }

    @Override
    public void showSoftKeyboard(View focusView) {
        presentationLayerFuncHelper.showSoftKeyboard(focusView);
    }

    @Override
    public void hideSoftKeyboard() {
        presentationLayerFuncHelper.hideSoftKeyboard();
    }

    @Override
    protected void onDestroy() {
        BaseApplication.baseApplication.deleteActivity(this);
        EventBus.getDefault().unregister(this);             // 退出时解绑
        if (presenter != null) {
            presenter.detachView(this);
        }
        OkHttpManager httpManager = BridgeFactory.getBridge(Bridges.HTTP);
        httpManager.cancelActivityRequest(TAG);
        super.onDestroy();
    }

}
