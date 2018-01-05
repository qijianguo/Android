package com.adc.shapingba;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.data.LoginState;
import com.adc.statistics.NightNoiseActivity;
import com.adc.util.GetDistrictInfo;
import com.adc.util.GetSpotInfo;
import com.adc.util.IsNumericUtil;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;
import com.hik.mcrsdk.MCRSDK;
import com.hik.mcrsdk.rtsp.RtspClient;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WuhanMainActivity extends SlidingFragmentActivity implements OnClickListener{

	private Handler handler = null;
	
	private Button wuhan_main_bt_menu;
	private Button wuhan_main_bt_gongdi;
	private Button wuhan_main_bt_daolu;
	private Button wuhan_main_bt_refresh;
	private TextView wuhan_main_tv_update_time;
	private TextView wuhan_main_tv_aqi;
	private TextView wuhan_main_tv_aqi_rank;
	private TextView wuhan_main_tv_pm10;
	private TextView wuhan_main_tv_temperature;
	private TextView wuhan_main_tv_weather;
	private TextView wuhan_main_tv_date1;
	private TextView wuhan_main_tv_date2;
	private TextView wuhan_main_tv_latest_pm10;
	private TextView wuhan_main_tv_last_month_pm10;
	private ImageView wuhan_main_img_weather;
	
	private String aqi;
	private String pm10;
	private String temperature;
	private String time_day;//用来显示最新PM10的时间
	private String time;//首页最上方用来显示天气数据的时间
	private String weather_name;
	private String date1;
	private String date2;
	private String latest_pm10;
	private String last_month_pm10;
	
	private boolean refresh_press = false;
	
	private static final int NETWORK_CONNECTED_FOR_GONGDI = 300;
	private static final int NETWORK_CONNECTED_FOR_DAOLU = 301;
	private static final int NETWORK_CONNECTED_FOR_WEATHER = 302;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int START_PROCESS = 102;
	private static final int CANCEL_PROCESS = 103;
	private static final int GET_WEATHER_INFO_SUCCEED = 104;
	private static final int GET_DISTRICT_INFO_SUCCEED = 105;
	private static final int GET_SPOT_INFO_SUCCEED = 106;
	private static final int URL_REQUEST_FAIL = 107;
	private static final int REFRESH_WEATHER_INFO_SUCCEED = 108;
	private static final int START_NEW_ACTIVITY = 109;
	
	private static final int GONGDI_SELECTED = 200;
	private static final int DAOLU_SELECTED = 201;
	
	private String serverURL = LoginState.getIns().getServerURL();
	
	private int new_activity_id;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent(WuhanMainActivity.this, LoginActivity.class);
			startActivity(intent);
			finish();
			return false;
		}
		return false;
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wuhan_main);
		
		initRightMenu();
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(WuhanMainActivity.this);
		
		//海康视频规定的初始化
//		MCRSDK.init();
//		MCRSDK.setPrint(1, null);
//		RtspClient.initLib();
				
		//初始化ImageLoader
		configImageLoader();
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED_FOR_WEATHER:
					getWeatherInfo();
					break;
				case NETWORK_CONNECTED_FOR_GONGDI:
					getDistrictInfo();
					break;
				case NETWORK_CONNECTED_FOR_DAOLU:
					//暂时未开放，直接跳到“敬请期待”
					startNewActivity();
					break;
				case NETWORK_UNCONNECTED:
					showNetworkError();
					break;
				case START_PROCESS:
					showLoadProgress();
					break;
				case CANCEL_PROCESS:
					cancelProgress();
					break;
				case URL_REQUEST_FAIL:
					showServerError();
					break;
				case GET_WEATHER_INFO_SUCCEED:
					showWeatherInfo();
					break;
				case GET_DISTRICT_INFO_SUCCEED:
					//先拿区域信息，再拿具体监测点信息
					getSpotInfo();
				case GET_SPOT_INFO_SUCCEED:
					showLoadInfoSucceed();
					break;
				case GONGDI_SELECTED:
					doGongdiService();
					break;
				case DAOLU_SELECTED:
					doDaoluService();
					break;
				case START_NEW_ACTIVITY:
					startNewActivity();
					break;
				case REFRESH_WEATHER_INFO_SUCCEED:
					showRefreshInfoSucceed();
					break;
				default:
					break;
				}
			}
		};
		
		wuhan_main_bt_menu = (Button) findViewById(R.id.wuhan_main_bt_menu);
		wuhan_main_bt_menu.setOnClickListener(this);
		if(!LoginState.getIns().getEdition_type().equals("1")){
			//如果没有专业版权限，侧边栏不可见
			wuhan_main_bt_menu.setVisibility(View.GONE);
		}
		
		wuhan_main_bt_gongdi = (Button) findViewById(R.id.wuhan_main_bt_gongdi);
		wuhan_main_bt_gongdi.setOnClickListener(this);
		
		wuhan_main_bt_daolu = (Button) findViewById(R.id.wuhan_main_bt_daolu);
		wuhan_main_bt_daolu.setOnClickListener(this);

		wuhan_main_bt_refresh = (Button) findViewById(R.id.wuhan_main_bt_refresh);
		wuhan_main_bt_refresh.setOnClickListener(this);
		
		wuhan_main_tv_update_time = (TextView) findViewById(R.id.wuhan_main_tv_update_time);
		wuhan_main_tv_aqi = (TextView) findViewById(R.id.wuhan_main_tv_aqi);
		wuhan_main_tv_aqi_rank = (TextView) findViewById(R.id.wuhan_main_tv_aqi_rank);
		wuhan_main_tv_pm10 = (TextView) findViewById(R.id.wuhan_main_tv_pm10);
		wuhan_main_tv_temperature = (TextView) findViewById(R.id.wuhan_main_tv_temperature);
		wuhan_main_tv_weather = (TextView) findViewById(R.id.wuhan_main_tv_weather);
		wuhan_main_img_weather = (ImageView) findViewById(R.id.wuhan_main_img_weather);
		
		wuhan_main_tv_date1 = (TextView) findViewById(R.id.wuhan_main_tv_date1);
		wuhan_main_tv_date2 = (TextView) findViewById(R.id.wuhan_main_tv_date2);
		wuhan_main_tv_latest_pm10 = (TextView) findViewById(R.id.wuhan_main_tv_latest_pm10);
		wuhan_main_tv_last_month_pm10 = (TextView) findViewById(R.id.wuhan_main_tv_last_month_pm10);
		
		checkConnect(NETWORK_CONNECTED_FOR_WEATHER);
	}
	
	/**
	 * 配置ImageLoder
	 */
	private void configImageLoader() {
		// 初始化ImageLoader
		@SuppressWarnings("deprecation")
		DisplayImageOptions options = new DisplayImageOptions.Builder().showStubImage(R.drawable.icon_stub) // 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.drawable.icon_empty) // 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.icon_error) // 设置图片加载或解码过程中发生错误显示的图片
				.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
				// .displayer(new RoundedBitmapDisplayer(20)) // 设置成圆角图片
				.build(); // 创建配置过得DisplayImageOption对象

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(options)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).build();
		ImageLoader.getInstance().init(config);		
	}
	
	protected void initRightMenu(){
		Fragment right_fragment = new WuhanMainRightMenuFragment();
		setBehindContentView(R.layout.right_menu_frame);
		getSupportFragmentManager().beginTransaction()
			.replace(R.id.id_right_menu_frame, right_fragment).commit();
		SlidingMenu menu = getSlidingMenu();
		menu.setMode(SlidingMenu.RIGHT);
		// 设置触摸屏幕的模式
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		//menu.setShadowDrawable(R.drawable.shadow);
		// 设置滑动菜单视图的宽度
		menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		// 设置渐入渐出效果
		menu.setFadeDegree(0.35f);
		
		if(!LoginState.getIns().getEdition_type().equals("1")){
			//如果没有专业版权限，侧边栏不可见
			menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wuhan_main_bt_menu:
			getSlidingMenu().showMenu();
			break;
		case R.id.wuhan_main_bt_refresh:
			refresh_press = true;
			checkConnect(NETWORK_CONNECTED_FOR_WEATHER);
			break;
		case R.id.wuhan_main_bt_gongdi:
			handler.sendEmptyMessage(GONGDI_SELECTED);
			break;
		case R.id.wuhan_main_bt_daolu:
			//handler.sendEmptyMessage(DAOLU_SELECTED);
			//宗老师说道路暂时和扬尘一样
			handler.sendEmptyMessage(GONGDI_SELECTED);
			break;
		default:
			break;
		}
	}
	
	protected void checkConnect(final int for_what) {
		new Thread() {
			@Override
			public void run() {
				ConnectivityManager con = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				boolean wifi = con
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
						.isConnectedOrConnecting();
				boolean internet = con.getNetworkInfo(
						ConnectivityManager.TYPE_MOBILE)
						.isConnectedOrConnecting();
				if (wifi | internet) {
					handler.sendEmptyMessage(for_what);
				} else {
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
				}
			}
		}.start();
	}
	
	protected void getWeatherInfo() {
		handler.sendEmptyMessage(START_PROCESS);
		new Thread(){
			public void run(){
				String json = null;
				String query_path = serverURL+"getWeatherDataByUserId?user_id="+LoginState.getIns().getUserId();
				URL url;
				try {
					//20161212临时加一个getspotinfo
					GetSpotInfo.getSpotInfo(serverURL);
					url = new URL(query_path);
					Log.i("heheda",query_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(50 * 1000);
					conn.setRequestMethod("GET");
					int responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = ReadStream.readStream(is);
						json = new String(data);

						JSONObject jsonObject = new JSONObject(json);
						Log.i("heheda", "0000000000000");
						temperature = jsonObject.getString("temp");
						pm10 = jsonObject.getString("pm10");
						aqi = jsonObject.getString("aqi");
						time = jsonObject.getString("create_time");
						//time_day = jsonObject.getString("time_day");
						weather_name = jsonObject.getString("weather_name");
						//latest_pm10 = jsonObject.getString("station_avg_day");
						//last_month_pm10 = jsonObject.getString("station_avg_month");
						Log.i("heheda", "111111111111");
						handler.sendEmptyMessage(GET_WEATHER_INFO_SUCCEED);
						if(refresh_press){
							handler.sendEmptyMessage(REFRESH_WEATHER_INFO_SUCCEED);
						}
					}else{
						handler.sendEmptyMessage(URL_REQUEST_FAIL);
					}
					handler.sendEmptyMessage(CANCEL_PROCESS);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);	
					handler.sendEmptyMessage(CANCEL_PROCESS);
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	protected void showWeatherInfo() {
		wuhan_main_tv_temperature.setText(temperature);
		wuhan_main_tv_pm10.setText(pm10);
		wuhan_main_tv_weather.setText(weather_name);
		if(weather_name.contains("晴")){
			wuhan_main_img_weather.setImageResource(R.drawable.weather_qin);
		}else if (weather_name.contains("多云")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_duoyun);
		}else if (weather_name.contains("阴")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_yin);
		}else if (weather_name.contains("特大暴雨")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_tedabaoyu);
		}else if (weather_name.contains("暴雪")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_baoxue);
		}else if (weather_name.contains("冰雹")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_bingbao);
		}else if (weather_name.contains("大雪")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_daxue);
		}else if (weather_name.contains("大雨")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_dayu);
		}else if (weather_name.contains("冻雨")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_dongyu);
		}else if (weather_name.contains("浮尘")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_fuchen);
		}else if (weather_name.contains("雷阵雨")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_leizhenyu);
		}else if (weather_name.contains("强沙尘暴")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_qiangshachenbao);
		}else if (weather_name.contains("沙尘暴")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_shachenbao);
		}else if (weather_name.contains("暴雨")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_baoyu);
		}else if (weather_name.contains("雾霾")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_wumai);
		}else if (weather_name.contains("雾")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_wu);
		}else if (weather_name.contains("小雪")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_xiaoxue);
		}else if (weather_name.contains("小雨")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_xiaoyu);
		}else if (weather_name.contains("扬尘")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_yangchen);
		}else if (weather_name.contains("雨夹雪")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_yujiaxue);
		}else if (weather_name.contains("阵雪")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_zhenxue);
		}else if (weather_name.contains("阵雨")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_zhenyu);
		}else if (weather_name.contains("中雪")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_zhongxue);
		}else if (weather_name.contains("中雨")) {
			wuhan_main_img_weather.setImageResource(R.drawable.weather_zhongyu);
		}else{
			wuhan_main_img_weather.setImageDrawable(null);
		}
		wuhan_main_tv_aqi.setText(aqi);
		double aqi_val = (IsNumericUtil.isDouble(aqi)) ? Double.valueOf(aqi) : -1; 
		if(aqi_val <= 50){
			wuhan_main_tv_aqi_rank.setBackgroundResource(R.drawable.aqi_50);
		}else if (aqi_val <= 100) {
			wuhan_main_tv_aqi_rank.setBackgroundResource(R.drawable.aqi_100);
		}else if (aqi_val <= 150) {
			wuhan_main_tv_aqi_rank.setBackgroundResource(R.drawable.aqi_150);
		}else if (aqi_val <= 200) {
			wuhan_main_tv_aqi_rank.setBackgroundResource(R.drawable.aqi_200);
		}else if (aqi_val <= 300) {
			wuhan_main_tv_aqi_rank.setBackgroundResource(R.drawable.aqi_300);
		}else {
			wuhan_main_tv_aqi_rank.setBackgroundResource(R.drawable.aqi_500);
		}
		
		wuhan_main_tv_update_time.setText(time);
		/*if(time.length() < 12){
			wuhan_main_tv_update_time.setText(time);
		}else{
			wuhan_main_tv_update_time.setText(time.substring(0, 4) + "-" + time.substring(4, 6) + "-" + time.substring(6, 8)
			+ " " + time.substring(8, 10) + ":" + time.substring(10, 12));
		}
		if(time_day.length() >= 8)	date1 = time_day.substring(0, 4) + "-" + time_day.substring(4, 6) + "-" + time_day.substring(6, 8);
		if(time_day.length() >= 6)	date2 = time_day.substring(0, 4) + "-" + time_day.substring(4, 6);
		wuhan_main_tv_date1.setText(date1);
		wuhan_main_tv_date2.setText(date2);
		Log.i("heheda", "fffffffffffffffffff");
		int yesterday_pm10_val = Integer.valueOf(latest_pm10);
		if(yesterday_pm10_val <= 0){
			latest_pm10 = "/";
		}
		int last_month_pm10_val = Integer.valueOf(last_month_pm10);
		if(last_month_pm10_val <= 0){
			last_month_pm10 = "/";
		}
		wuhan_main_tv_latest_pm10.setText(latest_pm10);
		wuhan_main_tv_last_month_pm10.setText(last_month_pm10);*/
	}
	
	protected void getDistrictInfo(){
		handler.sendEmptyMessage(START_PROCESS);
		new Thread(){
			@Override
			public void run(){
				try {
					GetDistrictInfo.getDistrictInfo(serverURL);
					handler.sendEmptyMessage(GET_DISTRICT_INFO_SUCCEED);
					handler.sendEmptyMessage(CANCEL_PROCESS);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
					handler.sendEmptyMessage(CANCEL_PROCESS);
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	protected void getSpotInfo(){
		handler.sendEmptyMessage(START_PROCESS);
		new Thread(){
			@Override
			public void run(){
				try {
					GetSpotInfo.getSpotInfo(serverURL);
					handler.sendEmptyMessage(GET_SPOT_INFO_SUCCEED);
					handler.sendEmptyMessage(START_NEW_ACTIVITY);
					handler.sendEmptyMessage(CANCEL_PROCESS);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
					handler.sendEmptyMessage(CANCEL_PROCESS);
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	protected void showNetworkError() {
		Toast toast = Toast.makeText(WuhanMainActivity.this, "网络不给力，加载信息失败",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showLoadInfoSucceed() {
		Toast toast = Toast.makeText(WuhanMainActivity.this, "加载信息成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showRefreshInfoSucceed() {
		Toast toast = Toast.makeText(WuhanMainActivity.this, "空气质量信息刷新成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showServerError() {
		Toast toast = Toast.makeText(WuhanMainActivity.this, "服务器维护中，请稍后再试",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 加载信息进度条
	 */
	private void showLoadProgress() {
		UIUtil.showProgressDialog(this, "正在加载信息......");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
	/**
	 * 加载信息进度条
	 */
	private void showLoadHikvisionProgress() {
		UIUtil.showProgressDialog(this, "正在加载视频信息......");
	}

	/**
	 * 取消进度条
	 */
	private void cancelHikvisionProgress() {
		UIUtil.cancelProgressDialog();
	}
	
	private void doGongdiService() {
		new_activity_id = GONGDI_SELECTED;
		checkConnect(NETWORK_CONNECTED_FOR_GONGDI);
	}
	
	private void doDaoluService() {
		new_activity_id = DAOLU_SELECTED;
		checkConnect(NETWORK_CONNECTED_FOR_DAOLU);
	}
	
	private void startNewActivity(){
		if(new_activity_id == GONGDI_SELECTED){
			LoginState.getIns().setNoise_or_pm10(0);
			Intent intent = new Intent(WuhanMainActivity.this,
					SpotRankActivity.class);
			startActivity(intent);
			finish();
		}else if(new_activity_id == DAOLU_SELECTED){
			LoginState.getIns().setNoise_or_pm10(1);
			Intent intent = new Intent(WuhanMainActivity.this,
					RoadRankActivity.class);
			startActivity(intent);
			finish();
		}
	}
}
