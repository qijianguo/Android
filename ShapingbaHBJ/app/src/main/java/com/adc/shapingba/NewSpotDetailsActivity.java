package com.adc.shapingba;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.data.TempData;
import com.adc.util.GetSpotInfo;
import com.adc.util.IsNumericUtil;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;
import com.hikvision.vmsnetsdk.LineInfo;
import com.hikvision.vmsnetsdk.ServInfo;
import com.hikvision.vmsnetsdk.VMSNetSDK;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class NewSpotDetailsActivity extends SlidingFragmentActivity {

	private static final int NETWORK_CONNECTED_FOR_DETAILS = 100;
	private static final int NETWORK_CONNECTED_FOR_MORE_INFO = 99;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int LOAD_INFO_SUCCEED = 102;
	private static final int START_PROCESS = 103;
	private static final int CANCEL_PROCESS = 104;
	private static final int URL_REQUEST_FAIL = 105;
	private static final int LOAD_MORE_INFO_SUCCEED = 106;
	private static final int SHOW_MORE_INFO_RESULT = 107;
	
	private static final int START_HIKVISION_PROCESS = 1005;
	private static final int CANCEL_HIKVISION_PROCESS = 1006;
	private static final int LOGIN_HIKVISION_SERVER_SUCCEED = 1007;
	private static final int LOGIN_HIKVISION_SERVER_FAIL = 1008;
	
	private Handler handler = null; 
	
	//spot_list里的第几个监测点
	public static int idx;
	
	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	private String camera_id;
	
	private String serverURL = LoginState.getIns().getServerURL();
	
	private Button new_spot_details_goback;
	private Button new_spot_details_video;
	private Button jcxq_bt_menu;
	private Button jcxq_bt_more;
	
	private TextView jcxq_tv_spot;
	private TextView jcxq_tv_address;
	private TextView jcxq_tv_latlng;
	private TextView jcxq_tv_tsp;
	private TextView jcxq_tv_pm2_5;
	private TextView jcxq_tv_pm10;
	private TextView jcxq_tv_noise;
	private TextView jcxq_tv_wind_direction;
	private TextView jcxq_tv_wind_speed;
	private TextView jcxq_tv_temperature;
	private TextView jcxq_tv_humidity;
	private TextView jcxq_tv_air_press;
	
	private ImageView jcxq_img_tsp;
	private ImageView jcxq_img_pm2_5;
	private ImageView jcxq_img_pm10;
	private ImageView jcxq_img_noise;
	private ImageView jcxq_img_wind_direction;
	private ImageView jcxq_img_wind_speed;
	private ImageView jcxq_img_temperature;
	private ImageView jcxq_img_humidity;
	private ImageView jcxq_img_air_press;
	
	private LinearLayout jcxq_layout_penlin;
	private Button jcxq_bt_penlin;
	
	private TextView jcxq_tv_time;
	
	//private TextView jcxq_tv_auto_exception;
	private TextView jcxq_tv_contacts_info;
	private TextView jcxq_tv_exception;
	private TextView jcxq_tv_sms;
	
	private ScrollView jcxq_scroll_view;
	//private LinearLayout jcxq_layout_auto_exception;
	private LinearLayout jcxq_layout_more_info;
	private LinearLayout jcxq_layout_exception;
	private LinearLayout jcxq_layout_sms;
	
	private String spot_string;
	private String address_string;
	private String tsp_string;
	private String pm2_5_string;
	private String pm10_string;
	private String noise_string;
	private String wind_direction_string;
	private String wind_speed_string;
	private String humidity_string;
	private String time_string;
	private String latlng_string;
	private String temperature_string;
	private String air_press_string;
	
	//private String auto_exception_string;
	private String contacts_info_string;
	private String exception_string;
	private String sms_string;
	private String data_type;
	
	private String penlin_status_string;
	private String csite_id;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			//Intent goback_intent = new Intent(NewSpotDetailsActivity.this,WuhanMainActivity.class);
			//startActivity(goback_intent);
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_spot_details);

		initRightMenu();

		new_spot_details_goback = (Button) findViewById(R.id.new_spot_details_goback);
		new_spot_details_video = (Button) findViewById(R.id.new_spot_details_video);
		jcxq_bt_menu = (Button) findViewById(R.id.jcxq_bt_menu);

		jcxq_scroll_view = (ScrollView) findViewById(R.id.jcxq_scroll_view);
		jcxq_bt_more = (Button) findViewById(R.id.jcxq_bt_more);

		//jcxq_layout_auto_exception = (LinearLayout) findViewById(R.id.jcxq_layout_auto_exception);

		jcxq_layout_more_info = (LinearLayout) findViewById(R.id.jcxq_layout_more_info);

		jcxq_layout_more_info.setVisibility(View.GONE);//"更多信息"初始不可见

		jcxq_layout_exception = (LinearLayout) findViewById(R.id.jcxq_layout_exception);
		jcxq_layout_exception.setVisibility(View.GONE);//"异常信息"只有在日均值进入才可见

		jcxq_layout_sms = (LinearLayout) findViewById(R.id.jcxq_layout_sms);
		jcxq_layout_sms.setVisibility(View.GONE);//"短信报警"只有在日均值进入才可见

		jcxq_tv_spot = (TextView) findViewById(R.id.jcxq_tv_spot);
		jcxq_tv_address = (TextView) findViewById(R.id.jcxq_tv_address);
		jcxq_tv_latlng = (TextView) findViewById(R.id.jcxq_tv_latlng);
		jcxq_tv_tsp = (TextView) findViewById(R.id.jcxq_tv_tsp);
		jcxq_tv_pm2_5 = (TextView) findViewById(R.id.jcxq_tv_pm2_5);
		jcxq_tv_pm10 = (TextView) findViewById(R.id.jcxq_tv_pm10);
		jcxq_tv_noise = (TextView) findViewById(R.id.jcxq_tv_noise);
		jcxq_tv_wind_direction = (TextView) findViewById(R.id.jcxq_tv_wind_direction);
		jcxq_tv_wind_speed = (TextView) findViewById(R.id.jcxq_tv_wind_speed);
		jcxq_tv_temperature = (TextView) findViewById(R.id.jcxq_tv_temperature);
		jcxq_tv_humidity = (TextView) findViewById(R.id.jcxq_tv_humidity);
		jcxq_tv_air_press = (TextView) findViewById(R.id.jcxq_tv_air_press);
		
		jcxq_img_tsp = (ImageView) findViewById(R.id.jcxq_img_tsp);
		jcxq_img_pm2_5 = (ImageView) findViewById(R.id.jcxq_img_pm2_5);
		jcxq_img_pm10 = (ImageView) findViewById(R.id.jcxq_img_pm10);
		jcxq_img_noise = (ImageView) findViewById(R.id.jcxq_img_noise);
		jcxq_img_wind_direction = (ImageView) findViewById(R.id.jcxq_img_wind_direction);
		jcxq_img_wind_speed = (ImageView) findViewById(R.id.jcxq_img_wind_speed);
		jcxq_img_temperature = (ImageView) findViewById(R.id.jcxq_img_temperature);
		jcxq_img_humidity = (ImageView) findViewById(R.id.jcxq_img_humidity);
		jcxq_img_air_press = (ImageView) findViewById(R.id.jcxq_img_air_press);
		
		jcxq_tv_time = (TextView) findViewById(R.id.jcxq_tv_time);

		//jcxq_tv_auto_exception = (TextView) findViewById(R.id.jcxq_tv_auto_exception);
		jcxq_tv_contacts_info = (TextView) findViewById(R.id.jcxq_tv_contacts_info);
		jcxq_tv_exception = (TextView) findViewById(R.id.jcxq_tv_exception);
		jcxq_tv_sms = (TextView) findViewById(R.id.jcxq_tv_sms);

		jcxq_layout_penlin = (LinearLayout) findViewById(R.id.jcxq_layout_penlin);
		jcxq_bt_penlin = (Button) findViewById(R.id.jcxq_bt_penlin);
		
		Intent intent = getIntent();
		Bundle dataBundle = intent.getExtras();
		idx = dataBundle.getInt("idx");
		csite_id = spotInfos.get(idx).getCsite_id();
		/*20160827 不需要显示自动生成的异常信息
		auto_exception_string = dataBundle.getString("auto_exception_string");
		Log.i("heheda", "ex info = "+auto_exception_string);

		if(auto_exception_string.length() == 0){
			//没有没有数据异常信，不显示“数据异常”
			jcxq_layout_auto_exception.setVisibility(View.GONE);
		}else {
			jcxq_tv_auto_exception.setText(auto_exception_string);
		}
		*/
		
		/**
		 * 2016-08-10
		 * 只有从排名界面日均值入口进入才可以看到异常情况和短信报警
		 */
		data_type = dataBundle.getString("data_type");
		
		jcxq_bt_menu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getSlidingMenu().showMenu();
			}
		});
		
		jcxq_bt_more.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//jcxq_layout_more_info.setVisibility(View.VISIBLE);
				//handler.sendEmptyMessage(SHOW_MORE_INFO_RESULT);
				checkConnect(NETWORK_CONNECTED_FOR_MORE_INFO);
			}
		});
		
		if(!LoginState.getIns().getEdition_type().equals("1")){
			//如果没有专业版权限，隐藏相应的按钮，数据异常不可见
			jcxq_bt_menu.setVisibility(View.GONE);
			jcxq_bt_more.setVisibility(View.GONE);
			
			//jcxq_layout_auto_exception.setVisibility(View.GONE);
		}
		
		new_spot_details_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Intent intent = new Intent(NewSpotDetailsActivity.this,WuhanMainActivity.class);
				//startActivity(intent);
				finish();
			}
		});
		
		//如果该监测点没有视频信息,视频按钮显示成灰色
		camera_id = spotInfos.get(idx).getCamera_id();
		if(camera_id.compareTo("null") == 0){
			//new_spot_details_video.setVisibility(View.GONE);
			new_spot_details_video.setBackgroundResource(R.drawable.new_spot_details_no_video);
		}
				
		new_spot_details_video.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(camera_id.compareTo("null") == 0){
					return;
				}
				Intent intent = new Intent(NewSpotDetailsActivity.this,SpotVideoTabActivity.class);
				startActivity(intent);
				//finish();
			}
		});
		
		jcxq_bt_penlin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(penlin_status_string.equals("-1")){
					return;
				}
				Intent intent = new Intent(NewSpotDetailsActivity.this,NewSpotPenlinActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("csite_id", csite_id);
				bundle.putString("spot_string", spot_string);
				bundle.putString("address_string", address_string);
				bundle.putString("latlng_string", latlng_string);
				//bundle.putString("penlin_status_string", penlin_status_string);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED_FOR_DETAILS:
					getSpotDetails();
					break;
				case NETWORK_CONNECTED_FOR_MORE_INFO:
					getWuhanSpotDetails();
					break;
				case NETWORK_UNCONNECTED:
					showNetworkError();
					break;
				case START_PROCESS:
					showLoadingProgress();
					break;
				case CANCEL_PROCESS:
					cancelProgress();
					break;
				case URL_REQUEST_FAIL:
					showServerError();
					break;
				case LOAD_INFO_SUCCEED:
					showResult();
					break;
				case LOAD_MORE_INFO_SUCCEED:
					showMoreInfoResult();
					break;
				case SHOW_MORE_INFO_RESULT:
					jcxq_scroll_view.fullScroll(ScrollView.FOCUS_DOWN);
					break;
				case START_HIKVISION_PROCESS:
					showLoadHikvisionProcess();
					break;
				case CANCEL_HIKVISION_PROCESS:
					cancelHikvisionProcess();
					break;
				case LOGIN_HIKVISION_SERVER_SUCCEED:
					showLoginHikvisionSucceed();
					break;
				case LOGIN_HIKVISION_SERVER_FAIL:
					showLoginHikvisionFail();
					break;
				default:
					break;
				}
			}
		};

		checkConnect(NETWORK_CONNECTED_FOR_DETAILS);
	}

	protected void initRightMenu(){
		Fragment right_fragment = new NewSpotDetailsRightMenuFragment();
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
	
	protected void getSpotDetails() {
		new Thread(){
			@Override
			public void run(){
				handler.sendEmptyMessage(START_PROCESS);
				String json = null;
				int responseCode = 0;
				String query_path = serverURL+"csiteDetail?csite_id="
						+ spotInfos.get(idx).getCsite_id();
				Log.i("heheda", query_path);
				URL url;
				try {
					url = new URL(query_path);
					Log.i("heheda", query_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(30 * 1000);
					conn.setRequestMethod("GET");
					responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = ReadStream.readStream(is);
						json = new String(data);

						JSONObject item = new JSONObject(json);

						spot_string = item.getString("csite_name");
						address_string = item.getString("addr");
						latlng_string = "东经"+item.getString("longitude")+" 北纬"+item.getString("latitude");
						time_string = item.getString("recv_time");
						tsp_string = item.getString("dust");//tsp对应的是dust�??
						pm2_5_string = item.getString("pm2_5");
						pm10_string = item.getString("pm10");
						
						if (IsNumericUtil.isDouble(item.getString("noise"))) {
							double val = Double.valueOf(item.getString("noise"));
							noise_string = ""+(int)val;
						}
						
						wind_direction_string = item.getString("wind_direct");

						wind_speed_string = item.getString("wind_speed");
						
						temperature_string = item.getString("temp");

						humidity_string = item.getString("humid");
							
						air_press_string = item.getString("air_pressure");
						
						penlin_status_string = item.getString("penlin_status");

						if(!spotInfos.get(idx).getCamera_id().equals("null")){
							String servAddr = "http://"+spotInfos.get(idx).getVideo_service_ip()+":8090";
							if(!servAddr.equals(TempData.getIns().getHikvisionServerAddr())){
								loginHikvisionServer(getMac());
							}
						}						
						handler.sendEmptyMessage(CANCEL_PROCESS);
						handler.sendEmptyMessage(LOAD_INFO_SUCCEED);
					}else{
						handler.sendEmptyMessage(URL_REQUEST_FAIL);
						handler.sendEmptyMessage(CANCEL_PROCESS);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
					handler.sendEmptyMessage(CANCEL_PROCESS);
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	protected void showResult() {
		if(spot_string.length() >= 17){
			spot_string = spot_string.substring(0,17)+"...";
		}
		jcxq_tv_spot.setText(spot_string);
		if(address_string.length() >= 17){
			address_string = address_string.substring(0,17)+"...";
		}
		jcxq_tv_address.setText(address_string);
		jcxq_tv_latlng.setText(latlng_string);
		jcxq_tv_time.setText(time_string);
		//20160930 tsp单位改为μg/m³
		if(IsNumericUtil.isDouble(tsp_string)){
			double tsp_val = Double.valueOf(tsp_string);
			if (tsp_val == 0) {
				jcxq_tv_tsp.setText("/");
				jcxq_img_tsp.setImageResource(R.drawable.jcxq_tsp_grey);
			}else {
				tsp_val = tsp_val*1000;
				jcxq_tv_tsp.setText(String.valueOf((int)tsp_val));
			}
		}
		
		if(IsNumericUtil.isDouble(pm2_5_string)){
			double pm2_5_val = Double.valueOf(pm2_5_string);
			if (pm2_5_val == 0) {
				pm2_5_string = "/";
				jcxq_img_pm2_5.setImageResource(R.drawable.jcxq_tsp_grey);
			}
		}
		jcxq_tv_pm2_5.setText(pm2_5_string);
		
		if(IsNumericUtil.isDouble(pm10_string)){
			double pm10_val = Double.valueOf(pm10_string);
			if (pm10_val == 0) {
				pm10_string = "/";
				jcxq_img_pm10.setImageResource(R.drawable.jcxq_tsp_grey);
			}
		}
		jcxq_tv_pm10.setText(pm10_string);
		
		if(IsNumericUtil.isDouble(noise_string)){
			double noise_val = Double.valueOf(noise_string);
			if (noise_val == 0) {
				noise_string = "/";
				jcxq_img_noise.setImageResource(R.drawable.jcxq_noise_grey);
			}
		}
		jcxq_tv_noise.setText(noise_string);
		
		jcxq_tv_wind_direction.setText(wind_direction_string);
		
		if(IsNumericUtil.isDouble(wind_speed_string)){
			double noise_val = Double.valueOf(wind_speed_string);
			if (noise_val == 0) {
				wind_speed_string = "/";
				jcxq_img_wind_speed.setImageResource(R.drawable.jcxq_wind_speed_grey);
			}
		}
		jcxq_tv_wind_speed.setText(wind_speed_string);
		
		if(IsNumericUtil.isDouble(temperature_string)){
			double temperature_val = Double.valueOf(temperature_string);
			if (temperature_val == 0) {
				temperature_string = "/";
				jcxq_img_temperature.setImageResource(R.drawable.jcxq_temperature_grey);
			}
		}
		jcxq_tv_temperature.setText(temperature_string);
		
		if(IsNumericUtil.isDouble(humidity_string)){
			double humidity_val = Double.valueOf(humidity_string);
			if (humidity_val == 0) {
				humidity_string = "/";
				jcxq_img_humidity.setImageResource(R.drawable.jcxq_humidity_grey);
			}
		}
		jcxq_tv_humidity.setText(humidity_string);
		
		if(IsNumericUtil.isDouble(air_press_string)){
			double air_press_val = Double.valueOf(air_press_string);
			if (air_press_val == 0) {
				air_press_string = "/";
				jcxq_img_air_press.setImageResource(R.drawable.jcxq_air_press_grey);
			}
		}
		jcxq_tv_air_press.setText(air_press_string);
		
		if(penlin_status_string.equals("-1")){
			jcxq_bt_penlin.setBackgroundResource(R.drawable.penlin_open_grey);
		}
	}

	/**
	 * 为武汉定制的接口，显示设备相关联系人、异常信息、短信报警信息
	 */
	protected void getWuhanSpotDetails() {
		//先清空信息
		contacts_info_string = "";
		exception_string = "";
		sms_string = "";
		new Thread(){
			@Override
			public void run(){
				handler.sendEmptyMessage(START_PROCESS);
				Log.i("heheda", "AAAAAAAAAAAAAAAAAAAAA");
				String json = null;
				int responseCode = 0;
				String query_path = serverURL+"csiteDetailWuhan?csite_id="
						+ spotInfos.get(idx).getCsite_id();
				URL url;
				try {
					url = new URL(query_path);
					Log.i("heheda", query_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(30 * 1000);
					conn.setRequestMethod("GET");
					responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = ReadStream.readStream(is);
						json = new String(data);
						Log.i("heheda", json);
						JSONObject item = new JSONObject(json);

						JSONArray contactList = item.getJSONArray("contactList");
						for(int i = 0;i < contactList.length();i++){
							JSONObject contact = contactList.getJSONObject(i);
							contacts_info_string += contact.getString("contactType")+" "+contact.getString("contactName")
								+" 联系电话："+contact.getString("contactPhone")+"\n";
						}

						JSONArray errorList = item.getJSONArray("errorList");
						for(int i = 0;i < errorList.length();i++){
							exception_string += errorList.getString(i)+"\n";
						}
						
						JSONArray smsList = item.getJSONArray("smsList");
						for(int i = 0;i < smsList.length();i++){
							JSONObject sms = smsList.getJSONObject(i);
							sms_string += sms.getString("content")+"\n";
						}
						Log.i("heheda", "bbbbbbbbbbbbbbbbbbbb");
						handler.sendEmptyMessage(CANCEL_PROCESS);
						handler.sendEmptyMessage(LOAD_MORE_INFO_SUCCEED);
					}else{
						handler.sendEmptyMessage(URL_REQUEST_FAIL);
						handler.sendEmptyMessage(CANCEL_PROCESS);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
					handler.sendEmptyMessage(CANCEL_PROCESS);
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	protected void showMoreInfoResult() {
		jcxq_tv_contacts_info.setText(contacts_info_string);
		jcxq_tv_exception.setText(exception_string);
		jcxq_tv_sms.setText(sms_string);
		jcxq_layout_more_info.setVisibility(View.VISIBLE);
		if(data_type != null && data_type.equals("day")){
			jcxq_layout_exception.setVisibility(View.VISIBLE);
			jcxq_layout_sms.setVisibility(View.VISIBLE);
		}else{
			jcxq_layout_exception.setVisibility(View.GONE);
			jcxq_layout_sms.setVisibility(View.GONE);
		}
		handler.sendEmptyMessage(SHOW_MORE_INFO_RESULT);
	}
	
	protected void loginHikvisionServer(final String macAddress){
		//海康视频规定的初始化
		/*MCRSDK.init();
		MCRSDK.setPrint(1, null);
		RtspClient.initLib();*/
				
		handler.sendEmptyMessage(START_HIKVISION_PROCESS);
		
		new Thread() {
			@Override
			public void run() {
		
				String servAddr = "http://"+spotInfos.get(idx).getVideo_service_ip()+":8090";
				Log.i("heheda", "servAddr="+servAddr);
				LineInfo lineInfo = null;
				ServInfo servInfo;
								/*String sessionID = TempData.getIns().getLoginData().sessionID;
				Log.i("heheda", "-------------session="+sessionID);
				if(sessionID != null){
					VMSNetSDK.getInstance().logout(servAddr, sessionID, null);
				}*/
				
				List<LineInfo> lineInfoList = new ArrayList<LineInfo>();
				boolean ret = VMSNetSDK.getInstance().getLineList(servAddr,
						lineInfoList);
				if (ret) {
					Log.i("heheda", "获得线路列表成功" + lineInfoList.size());
					// 默认选择第一条线�??
					lineInfo = lineInfoList.get(0);
				} else {
					Log.i("heheda", "获得线路列表失败");
					handler.sendEmptyMessage(CANCEL_HIKVISION_PROCESS);
					handler.sendEmptyMessage(LOGIN_HIKVISION_SERVER_FAIL);
					return;
				}

				String userName = Constants.userName;
				String password = Constants.password;

				// 登录请求
				servInfo = null;
				servInfo = new ServInfo();
				ret = VMSNetSDK.getInstance().login(servAddr, userName,
						password, lineInfo.lineID, macAddress, servInfo);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				if (servInfo != null) {
					// 打印出登录时返回的信息
					Log.i("heheda", "login ret : " + ret);
					Log.i("heheda", "login response info[" + "sessionID:"
							+ servInfo.sessionID + ",userID:" + servInfo.userID
							+ ",magInfo:" + servInfo.magInfo
							+ ",picServerInfo:" + servInfo.picServerInfo
							+ ",ptzProxyInfo:" + servInfo.ptzProxyInfo
							+ ",userCapability:" + servInfo.userCapability
							+ ",vmsList:" + servInfo.vmsList + ",vtduInfo:"
							+ servInfo.vtduInfo + ",webAppList:"
							+ servInfo.webAppList + "]");
				}
				if(ret){
					TempData.getIns().setHikvisionServerAddr(servAddr);
					//TempData.getIns().setLoginData(null);
					TempData.getIns().setLoginData(servInfo);
					handler.sendEmptyMessage(CANCEL_HIKVISION_PROCESS);
					handler.sendEmptyMessage(LOGIN_HIKVISION_SERVER_SUCCEED);
				}else {
					handler.sendEmptyMessage(CANCEL_HIKVISION_PROCESS);
					handler.sendEmptyMessage(LOGIN_HIKVISION_SERVER_FAIL);
				}
			};
		}.start();
	}

	/**
	 * 显示网络连接有问�?
	 */
	private void showNetworkError(){
		Toast toast = Toast.makeText(NewSpotDetailsActivity.this, "信息加载失败",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	/**
	 * 服务器故�? ，返回码不为200
	 */
	protected void showServerError() {
		Toast toast = Toast.makeText(NewSpotDetailsActivity.this, "服务器维护中，请稍后",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	/**
	 * 获取登录设备mac地址
	 * 
	 * @return
	 */
	protected String getMac() {
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		String mac = wm.getConnectionInfo().getMacAddress();
		return mac == null ? "" : mac;
	}
	
	/**
	 * 数据加载
	 */
	private void showLoadingProgress() {
		UIUtil.showProgressDialog(this, "数据加载中......");
	}

	/**
	 * 取消进度�??
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
	protected void showLoadHikvisionProcess() {
		UIUtil.showProgressDialog(this, "正在加载视频信息......");
	}

	protected void cancelHikvisionProcess() {
		UIUtil.cancelProgressDialog();
	}
	
	protected void showLoginHikvisionSucceed() {
		Toast toast = Toast.makeText(NewSpotDetailsActivity.this, "连接视频服务器成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showLoginHikvisionFail() {
		Toast toast = Toast.makeText(NewSpotDetailsActivity.this, "连接视频服务器失败",
				Toast.LENGTH_SHORT);
		toast.show();
	}
}
