package com.learning.ui.fragment.practice;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.learning.R;

/**
 * Description: 创建一个flagment: 继承Fragment, 重写onCreateView方法, 利用布局填充器inflater的inflate获得视图, 并返回
 * <p>
 * User: Administrator
 * Date: 2017-10-19
 * Time: 13:30
 */
public class FragmentDemo01 extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragment1 = inflater.inflate(R.layout.demo01_fragment, null);
        return fragment1;
    }
}
