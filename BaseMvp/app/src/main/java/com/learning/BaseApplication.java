package com.learning;

import android.app.Activity;
import android.app.Application;

import com.learning.bridge.BridgeFactory;
import com.learning.bridge.BridgeLifeCycleSetKeeper;
import com.learning.bridge.Bridges;
import com.learning.bridge.cache.localstorage.LocalFileStorageManager;
import com.learning.util.ToastUtil;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <应用初始化> <功能详细描述>
 *
 * @author
 * @version
 * @see
 * @since
 */
public class BaseApplication extends Application {
    /**
     * app实例
     */
    public static BaseApplication baseApplication = null;

    /**
     * 本地activity栈
     */
    public static List<Activity> activitys = new ArrayList<Activity>();

    /**
     * 当前avtivity名称
     */
    public static String currentActivityName = "";

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        baseApplication = this;
        BridgeFactory.init(this);
        BridgeLifeCycleSetKeeper.getInstance().initOnApplicationCreate(this);
        LocalFileStorageManager manager = BridgeFactory.getBridge(Bridges.LOCAL_FILE_STORAGE);
        Picasso picasso = new Picasso.Builder(this).downloader(
                new OkHttpDownloader(new File(manager.getCacheImgFilePath(this)))).build();
        Picasso.setSingletonInstance(picasso);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        onDestory();
    }

    /**
     * 退出应用，清理内存
     */
    private void onDestory() {
        BridgeLifeCycleSetKeeper.getInstance().clearOnApplicationQuit();
        ToastUtil.destory();
    }


    /**
     * <添加> <功能详细描述>
     *
     * @param activity
     * @see [类、类#方法、类#成员]
     */
    public void addActivity(Activity activity) {
        activitys.add(activity);
    }

    /**
     * <删除>
     * <功能详细描述>
     *
     * @param activity
     * @see [类、类#方法、类#成员]
     */
    public void deleteActivity(Activity activity) {
        if (activity != null) {
            activitys.remove(activity);
            activity.finish();
            activity = null;
        }
    }
}
