package com.learning.ui.personcenter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.learning.R;
import com.learning.presenter.personcenter.IUserLoginView;
import com.learning.presenter.personcenter.LoginPresenter;
import com.learning.constant.Event;
import com.learning.ui.base.BaseActivity;

public class LoginActivity extends BaseActivity implements IUserLoginView {

    private static final String TAG = "LoginActivity";
    
    /**
     * 用户名
     */
    private EditText userName;

    /**
     * 用户密码
     */
    private EditText password;

    /**
     * 登录
     */
    private Button login;

    private LoginPresenter mUserLoginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        presenter = mUserLoginPresenter = new LoginPresenter();
        mUserLoginPresenter.attachView(this);
    }

    @Override
    public void initViews() {
        Log.d(TAG, "initViews: ");
        userName = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.bt_login);
    }

    @Override
    public void initListeners() {
        Log.d(TAG, "initListeners: ");
        login.setOnClickListener(this);
    }

    @Override
    public void initData() {
        Log.d(TAG, "initData: ");
    }

    @Override
    public void setHeader() {
        super.setHeader();
        Log.d(TAG, "setHeader: ");
        title.setText("登录");
    }

    @Override
    // TODO: 接收通知
    public void onEventMainThread(Event event) {
        super.onEventMainThread(event);
        Log.d(TAG, "onEventMainThread: ");
        switch (event){
            case IMAGE_LOADER_SUCCESS:
                clearEditContent();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        switch (v.getId()) {
            case R.id.bt_login:
                //13914786934   123456  可以登录
                Log.d(TAG, "onClick: 点击登录了!用户名是:" +  userName.getText().toString());
                // 到presenter处理逻辑
                mUserLoginPresenter.login(userName.getText().toString(), password.getText().toString());
                break;
        }
        super.onClick(v);
    }

    @Override
    public void clearEditContent() {
        Log.d(TAG, "clearEditContent: ");
        userName.setText("");
        password.setText("");
    }

    @Override
    public void onError(String errorMsg, String code) {
        Log.d(TAG, "onError: ");
        showToast(errorMsg);
    }

    @Override
    public void onSuccess() {
        Log.d(TAG, "onSuccess: ");
        startActivity(HomeActivity.class,null);
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
