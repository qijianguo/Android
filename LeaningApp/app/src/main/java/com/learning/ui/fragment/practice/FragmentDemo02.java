package com.learning.ui.fragment.practice;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.learning.R;

/**
 * Description:
 * <p>
 * User: Administrator
 * Date: 2017-10-19
 * Time: 13:35
 */
public class FragmentDemo02 extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demo02_fragment, null);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
