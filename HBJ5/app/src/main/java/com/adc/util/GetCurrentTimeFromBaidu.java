package com.adc.util;

import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GetCurrentTimeFromBaidu {
	private static Handler handler;
	private static Calendar calendar;
	private static Calendar calendar1;

	public static Calendar getCurrentTime() {
		calendar = Calendar.getInstance();

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 0:
					calendar = calendar1;
					break;

				default:
					break;
				}
			}
		};

		getBaiduTime();

		return calendar;
	}

	private static void getBaiduTime() {
		new Thread() {
			@Override
			public void run() {
				try {
					URL url;
					url = new URL("http://www.baidu.com");
					URLConnection uc = url.openConnection();// 生成连接对象
					uc.connect(); // 发出连接
					long ld = uc.getDate(); // 取得网站日期时间
					Date date = new Date(ld); // 转换为标准时间对象
					calendar1 = Calendar.getInstance();
					Log.e("heheda",
							"current time = " + date + ", "
									+ calendar1.get(Calendar.HOUR_OF_DAY) + "时"
									+ calendar1.get(Calendar.MINUTE) + "分"
									+ calendar1.get(Calendar.SECOND) + "秒");

					calendar1.setTime(date);
					// 分别取得时间中的小时，分钟和秒，并输出
					Log.e("heheda",
							"current time2 = " + date + ", "
									+ calendar1.get(Calendar.HOUR_OF_DAY) + "时"
									+ calendar1.get(Calendar.MINUTE) + "分"
									+ calendar1.get(Calendar.SECOND) + "秒");
					handler.sendEmptyMessage(0);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					handler.sendEmptyMessage(1);
				}
			}
		}.start();
	}

}