package com.learning.ui.coolweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.learning.R;
import com.learning.ui.base.BaseActivity;

public class CWMainActivity extends BaseActivity {

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cwmain);
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_cwmain);
        super.onCreate(savedInstanceState);

        /*presenter = mUserLoginPresenter = new LoginPresenter();
        mUserLoginPresenter.attachView(this);*/
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
}
