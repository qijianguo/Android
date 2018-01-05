package com.learning.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import com.learning.R;
import com.learning.presenter.personcenter.ILoginView;
import com.learning.ui.base.BaseActivity;
import com.learning.ui.personcenter.LoginActivity;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_welcome);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initViews() {
        Handler xHandler = new Handler();
        xHandler.postDelayed(new splashhandler(), 2000);
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

    }

    class splashhandler implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            startActivity(new Intent(getApplication(), LoginActivity.class));
            WelcomeActivity.this.finish();
        }

    }
}
