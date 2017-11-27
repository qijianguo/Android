package com.learning.ui.noise;

import android.os.Bundle;

import com.learning.R;
import com.learning.presenter.noise.INoiseView;
import com.learning.presenter.noise.NoisePresenter;
import com.learning.ui.base.BaseActivity;

public class NoiseActivity extends BaseActivity implements INoiseView {

    private NoisePresenter noisePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_noise);
        super.onCreate(savedInstanceState);

        presenter = noisePresenter = new NoisePresenter();
        noisePresenter.attachView(this);
    }

    @Override
    public void initViews() {

    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void onError(String errorMsg, String code) {

    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void setHeader() {
        super.setHeader();
        title.setText("噪声模块");
    }
}
