package com.learning.bridge.cache.sharePref;

import android.content.Context;

import com.learning.capabilities.cache.BaseSharedPreference;

import java.util.List;

/**
 * Description: 噪声的缓存类
 * <p>
 * User: Administrator
 * Date: 2017-10-17
 * Time: 15:48
 */
public class EBSharedPrefNoise extends BaseSharedPreference {

    /**
     * 登录名
     */
    public static final String USER_NAME = "user_name";

    public EBSharedPrefNoise(Context context, String fileName) {
        super(context,fileName);
    }
}
