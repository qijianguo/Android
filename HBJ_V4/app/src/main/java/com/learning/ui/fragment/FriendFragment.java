package com.learning.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.learning.R;

/**
 * Description:
 * <p>
 * User: Administrator
 * Date: 2017-10-19
 * Time: 10:15
 */

public class FriendFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.friend_fragment, container, false);
    }

}