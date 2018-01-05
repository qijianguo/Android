package com.adc.playback;

/**
 * 回放中用到的常量
 * 
 * @author huangweifeng
 * @Data 2013-10-28
 */
public class ConstantPlayBack {
	private static final int ERR_BASE = 1000;
	/**
	 * 启动取流失败
	 * */
	public static final int START_RTSP_FAIL = ERR_BASE;
	/**
	 * 启动取流成功
	 * */
	public static final int START_RTSP_SUCCESS = ERR_BASE + 1;
	/**
	 * 暂停失败
	 * */
	public static final int PAUSE_FAIL = ERR_BASE + 2;
	/**
	 * 暂停成功
	 * */
	public static final int PAUSE_SUCCESS = ERR_BASE + 3;
	/**
	 * 恢复播放失败
	 * */
	public static final int RESUEM_FAIL = ERR_BASE + 4;
	/**
	 * 恢复播放成功
	 * */
	public static final int RESUEM_SUCCESS = ERR_BASE + 5;
	/**
	 * 启动播放失败
	 * */
	public static final int START_OPEN_FAILED = ERR_BASE + 6;
	/**
	 * 回放成功
	 * */
	public static final int PLAY_DISPLAY_SUCCESS = ERR_BASE + 7;
	/**
	 * SD卡不可用
	 * */
	public static final int SD_CARD_UN_USEABLE = ERR_BASE + 8;
	/**
	 * SD卡空间不�?
	 * */
	public static final int SD_CARD_SIZE_NOT_ENOUGH = ERR_BASE + 9;
	/**
	 * 非播放状态不能抓�?
	 */
	public static final int CAPTURE_FAILED_NPLAY_STATE = ERR_BASE + 10;
	/**
	 * 非播放状态不能暂�?
	 */
	public static final int PAUSE_FAIL_NPLAY_STATE = ERR_BASE + 11;
	/**
	 * 非暂停状态不�?要恢�?
	 */
	public static final int RESUEM_FAIL_NPAUSE_STATE = ERR_BASE + 12;
}
