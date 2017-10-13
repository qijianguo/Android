
package com.learning.bridge;

import android.content.Context;

/**
 * 实现各个底层功能管理类的统一初始化跟销毁工作，保持跟app的生命周期一致
 *
 * 如果Bridge层的生命周期和App的生命周期相关（在Application
 * onCreate的时候初始化，在用户双击back键退出），则实现此接口，届时统一初始化和销毁
 */
public interface BridgeLifeCycleListener {

    public void initOnApplicationCreate(Context context);

    public void clearOnApplicationQuit();
}
