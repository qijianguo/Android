
package com.learning.bridge.cache.sharePref;

import android.content.Context;

import com.learning.bridge.BridgeLifeCycleListener;


/**
 * <管理SharedPreference存储、读取>
 *
 * @author
 * @version
 * @see
 * @since
 */
public class EBSharedPrefManager implements BridgeLifeCycleListener {

    /** SharedPreference文件名列表 */
    private static final String PREF_NAME_USERINFO = "userinfo";

    private static final String PREF_NAME_SETTING = "setting";

    /**
     * 用户信息缓存
     */
    private static EBSharedPrefUser mEBSharedPrefUser;

    /**
     * 设置信息缓存
     */
    private EBSharedPrefSetting mEBSharedPrefSetting;

    private Context mApplicationContext;

    @Override
    public void initOnApplicationCreate(Context context) {
        mApplicationContext = context;
    }

    @Override
    public void clearOnApplicationQuit() {
    }


    /**
     * 用户信息缓存
     * @return
     */
    public EBSharedPrefUser getKDPreferenceUserInfo() {
        return mEBSharedPrefUser == null ? mEBSharedPrefUser = new EBSharedPrefUser(
                mApplicationContext, PREF_NAME_USERINFO) : mEBSharedPrefUser;
    }

    /**
     * 设置信息缓存
     */
    public EBSharedPrefSetting getKDPreferenceTestSetting() {
        return mEBSharedPrefSetting == null ? mEBSharedPrefSetting = new EBSharedPrefSetting(
                mApplicationContext, PREF_NAME_SETTING)
                : mEBSharedPrefSetting;
    }
}
