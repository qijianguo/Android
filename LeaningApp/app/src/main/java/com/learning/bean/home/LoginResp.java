package com.learning.bean.home;

import com.learning.bean.BaseResp;

/**
 * <功能详细描述>
 *
 * @author
 * @version
 * @see
 * @since
 */
public class LoginResp extends BaseResp {
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
