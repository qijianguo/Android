package com.adc.consts;

public final class Constants {
	private Constants() {
	}

	/**
	 * hikvision 服务器地址
	 */
	//public static String servAddr = "http://61.152.104.232:8090";
	
	/**
	 * hikvision 登录用户名
	 */
	public static String userName = "landfun";
	
	/**
	 * hikvision 登录密码
	 */
	public static String password = "landfun2012";

	/**
	 * 日志tag
	 */
	public static String LOG_TAG = "ivmsdemo";

	/**
	 * app list URL
	 */
	public static String applistURL = "http://121.41.83.162:8806/";
	
	/**
	 * 城市编号-重庆
	 */
	public static int CITY_ID_CHONGQIN = 35;
	
	/**
	 * 城市编号-济南
	 */
	public static int CITY_ID_JINAN = 21;
	
	/**
	 * 服务器地址-天津
	 */
	public static String SERVER_URL_TIANJIN = "http://tj.hbjk.com.cn:8081/restfulServer/rest/";
	
	/**
	 * 服务器地址-武汉
	 */
	public static String SERVER_URL_WUHAN = "http://221.232.152.206:8081/restfulServer/rest/";
	
	/**
	 * Intent key常量
	 */
	public static interface IntentKey {
		/**
		 * 控制中心id
		 */
		String CONTROL_UNIT_ID = "control_unit_id";
		/**
		 * 区域id
		 */
		String REGION_ID = "region_id";
		/**
		 * 监控点id
		 */
		String CAMERA_ID = "camera_id";
		/** 设备ID */
		String DEVICE_ID = "device_id";
	}

	public static interface Resource {
		/**
		 * 控制中心
		 */
		int TYPE_CTRL_UNIT = 1;
		/**
		 * 区域
		 */
		int TYPE_REGION = 2;
		/**
		 * 未知
		 */
		int TYPE_UNKNOWN = 3;
	}

	/**
	 * 登录逻辑相关常量
	 */
	public static interface Login {
		/**
		 * 获取线路成功
		 */
		int GET_LINE_SUCCESS = 0;
		/**
		 * 获取线路失败
		 */
		int GET_LINE_FAILED = 1;

		/**
		 * 显示进度
		 */
		int SHOW_LOGIN_PROGRESS = 2;
		/**
		 * 取消进度提示
		 */
		int CANCEL_LOGIN_PROGRESS = 3;

		/**
		 * 登录成功
		 */
		int LOGIN_SUCCESS = 4;
		/**
		 * 登录失败
		 */
		int LOGIN_FAILED = 5;
		/**
		 * 获取线路
		 */
		int GET_LINE_IN_PROCESS = 6;
	}
}
