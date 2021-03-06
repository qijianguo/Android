
package com.learning.bridge;

import android.content.Context;

import com.learning.bridge.cache.localstorage.LocalFileStorageManager;
import com.learning.bridge.cache.sharePref.EBSharedPrefManager;
import com.learning.bridge.http.OkHttpManager;
import com.learning.bridge.security.SecurityManager;
import java.util.HashMap;


/**
 * <中间连接层>
 * 用来统一管理基础功能,类似本地服务的实现原理
 * BridgeFactory里面实现了文件，网络，数据库，安全等等管理类的实现，并保存了各类管理类的引用。
 * 业务层或者上层调用底层实现时，一律通过BridgeFactory去访问，而不是直接的调用。
 *
 * @author
 * @version
 * @see
 * @since
 */
public class BridgeFactory {

    private static BridgeFactory model;

    private HashMap<String, Object> mBridges;

    private BridgeFactory() {
        mBridges = new HashMap<String, Object>();
    }

    public static void init(Context context) {
        model = new BridgeFactory();
        model.iniLocalFileStorageManager();
        model.initPreferenceManager();
        model.initSecurityManager();
        model.initUserSession();
        model.initCoreServiceManager(context);
        model.initOkHttpManager();
    }

    public static void destroy() {
        model.mBridges = null;
        model = null;
    }

    /**
     * 初始化本地存储路径管理类
     */
    private void iniLocalFileStorageManager() {
        LocalFileStorageManager localFileStorageManager = new LocalFileStorageManager();
        model.mBridges.put(Bridges.LOCAL_FILE_STORAGE, localFileStorageManager);
        BridgeLifeCycleSetKeeper.getInstance().trustBridgeLifeCycle(localFileStorageManager);
    }

    /**
     * 初始化SharedPreference管理类
     */
    private void initPreferenceManager() {
        EBSharedPrefManager ebSharedPrefManager = new EBSharedPrefManager();
        model.mBridges.put(Bridges.SHARED_PREFERENCE, ebSharedPrefManager);
        BridgeLifeCycleSetKeeper.getInstance().trustBridgeLifeCycle(ebSharedPrefManager);
    }

    /**
     * 网络请求管理类
     */
    private void initOkHttpManager() {
        OkHttpManager mOkHttpManager = new OkHttpManager();
        model.mBridges.put(Bridges.HTTP, mOkHttpManager);
        BridgeLifeCycleSetKeeper.getInstance().trustBridgeLifeCycle(mOkHttpManager);
    }

    /**
     * 初始化安全模块
     */
    private void initSecurityManager() {
        SecurityManager securityManager = new SecurityManager();
        model.mBridges.put(Bridges.SECURITY, securityManager);
        BridgeLifeCycleSetKeeper.getInstance().trustBridgeLifeCycle(securityManager);
    }

    /**
     * 初始化用户信息模块
     */
    private void initUserSession() {
    }

    /**
     * 初始化Tcp服务
     *
     * @param context
     */
    private void initCoreServiceManager(Context context) {
    }


    private void initDBManager() {
    }

    /**
     * 通过bridgeKey {@link Bridges}来获取对应的Bridge模块
     *
     * @param bridgeKey {@link Bridges}
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <V extends Object> V getBridge(String bridgeKey) {
        final Object bridge = model.mBridges.get(bridgeKey);
        if (bridge == null) {
            throw new NullPointerException("-no defined bridge-");
        }
        return (V) bridge;
    }
}
