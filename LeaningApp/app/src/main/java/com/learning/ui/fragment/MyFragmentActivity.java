package com.learning.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.learning.R;

public class MyFragmentActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MyFragmentActivity";

    private Button mTabWeixin;
    private Button mTabFriend;

    private ContentFragment mWeixin;
    private FriendFragment mFriend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_fragment);
        Log.d(TAG, "onCreate: aaaaaaaaaaaaaaaaaaa");

        // 初始化UI
        initUI();

        // 设置默认fragment
        setDefaultFragment();
    }

    private void setDefaultFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        mWeixin = new ContentFragment();
        transaction.replace(R.id.id_content, mWeixin);
        transaction.commit();
    }


    private void initUI() {
        // 初始化控件和声明事件
        mTabWeixin = findViewById(R.id.tab_bottom_weixin);
        mTabFriend = findViewById(R.id.tab_bottom_friend);
        mTabWeixin.setOnClickListener(this);
        mTabFriend.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        FragmentManager fm = getFragmentManager();
        // 开启Fragment事务
        FragmentTransaction transaction = fm.beginTransaction();

        switch (view.getId()) {
            case R.id.tab_bottom_weixin:
                if (mWeixin == null) {
                    mWeixin = new ContentFragment();
                }
                // 使用当前Fragment的布局替代id_content的控件
                transaction.replace(R.id.id_content, mWeixin);
                break;
            case R.id.tab_bottom_friend:
                if (mFriend == null) {
                    mFriend = new FriendFragment();
                }
                transaction.replace(R.id.id_content, mFriend);
                break;
        }
        // transaction.addToBackStack();
        // 事务提交
        transaction.commit();
    }
}
