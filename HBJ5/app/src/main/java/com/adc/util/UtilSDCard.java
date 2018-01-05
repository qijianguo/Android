package com.adc.util;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

/**
 * SDCard�?测类
 * 
 * @author huangweifeng
 * @Data 2013-10-23
 */
public class UtilSDCard {

	/**
	 * 获取SDCard 路径
	 * 
	 * @return SDCard 路径
	 * @since V1.0
	 */
	public static File getSDCardPath() {
		return android.os.Environment.getExternalStorageDirectory();
	}

	/**
	 * 获取SDCard剩下的大�?
	 * 
	 * @return SDCard剩下的大�?
	 * @since V1.0
	 */
	public static long getSDCardRemainSize() {
		StatFs statfs = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		long blockSize = statfs.getBlockSize();
		long availableBlocks = statfs.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获取SDCard的状�?
	 * 
	 * @return SDCard 可用的状�?
	 */
	public static boolean isSDCardUsable() {
		boolean SDCardMounted = false;
		String sDStateString = android.os.Environment.getExternalStorageState();
		if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			SDCardMounted = true;
		}

		// 是否正在�?测SD�?
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_CHECKING)
				|| Environment.getExternalStorageState().equals(
						Environment.MEDIA_NOFS)) {
			SDCardMounted = false;
		}

		// �?测是否插有SD�?
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_REMOVED)
				|| Environment.getExternalStorageState().equals(
						Environment.MEDIA_UNMOUNTED)) {
			SDCardMounted = false;
		}

		// �?测SD卡是否连接电脑共�?
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_SHARED)) {
			SDCardMounted = false;
		}

		return SDCardMounted;
	}
}