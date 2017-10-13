package com.learning.capabilities.http;

/**
 * <参数类>
 *
 * @author
 * @version
 * @see
 * @since
 */
public class Param {
    public Param() {
    }

    public Param(String key, String value) {
        this.key = key;
        this.value = value != null ? value : "";
    }

    public Param(String key, int value) {
        this.key = key;
        this.value = value + "";
    }

    public String key;

    public String value;
}