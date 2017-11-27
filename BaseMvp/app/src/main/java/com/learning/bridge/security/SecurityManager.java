package com.learning.bridge.security;

import android.content.Context;

import com.learning.bridge.BridgeLifeCycleListener;
import com.learning.capabilities.security.SecurityUtils;

/**
 * <加解密管理类>
 *
 * @author
 * @version
 * @see
 * @since
 */
public class SecurityManager  implements BridgeLifeCycleListener {
    @Override
    public void initOnApplicationCreate(Context context) {

    }

    @Override
    public void clearOnApplicationQuit() {

    }

    /**
     * md5 加密
     * @param str
     * @return
     */
    public String get32MD5Str(String str){
        return SecurityUtils.get32MD5Str(str);
    }

}
