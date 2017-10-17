package com.learning.ui.personcenter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.learning.R;
import com.learning.constant.Event;
import com.learning.presenter.home.HomePresenter;
import com.learning.presenter.home.IHomeView;
import com.learning.presenter.noise.INoiseView;
import com.learning.presenter.noise.NoisePresenter;
import com.learning.ui.base.BaseActivity;
import com.learning.ui.noise.NoiseActivity;
import com.learning.util.ToastUtil;

public class HomeActivity extends BaseActivity implements IHomeView {
    private static final String TAG = "HomeActivity";
    /** 噪声 */
    private Button noise_button;

    private NoisePresenter noisePresenter;
    private HomePresenter homePresenter;

    // 模块id
    private int checked_R_Id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.home_activity);
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        presenter = homePresenter = new HomePresenter();
        homePresenter.attachView(this);
    }

    @Override
    public void initViews() {
        noise_button = findViewById(R.id.bt_main_noise);
    }

    @Override
    public void initListeners() {
        Log.d(TAG, "initListeners: ");
         noise_button.setOnClickListener(this);
    }

    @Override
    public void initData() {
        Log.d(TAG, "initData: ");
    }

    @Override
    public void onEventMainThread(Event event) {
        Log.d(TAG, "onEventMainThread: ");
        super.onEventMainThread(event);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        checked_R_Id = v.getId();
        homePresenter.getHomeData();
        switch (v.getId()) {
            case R.id.bt_main_noise:
                ToastUtil.makeText(this, "点击了噪声模块");
                break;
        }
        super.onClick(v);
    }

    @Override
    public void setHeader() {
        super.setHeader();
        // title.setText("环保千里眼");
    }

    @Override
    public void onError(String errorMsg, String code) {
        Log.d(TAG, "onError: ");
    }

    @Override
    public void onSuccess() {
        Log.d(TAG, "onSuccess: ");
        switch (checked_R_Id) {
            case R.id.bt_main_noise:
                startActivity(NoiseActivity.class, null);
                break;

        }
    }

    @Override
    public void showLoading() {
        Log.d(TAG, "showLoading: ");
    }

    @Override
    public void hideLoading() {
        Log.d(TAG, "hideLoading: ");
    }
}
