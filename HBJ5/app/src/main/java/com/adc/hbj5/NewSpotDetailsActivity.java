package com.adc.hbj5;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.data.TempData;
import com.adc.hbj5.R;
import com.adc.oldactivity.CapturePhotoActivity;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;
import com.hikvision.vmsnetsdk.LineInfo;
import com.hikvision.vmsnetsdk.ServInfo;
import com.hikvision.vmsnetsdk.VMSNetSDK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewSpotDetailsActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int LOAD_INFO_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 106;
	
	private final int START_HIKVISION_PROCESS = 1005;
	private final int CANCEL_HIKVISION_PROCESS = 1006;
	private final int LOGIN_HIKVISION_SERVER_SUCCEED = 1007;
	private final int LOGIN_HIKVISION_SERVER_FAIL = 1008;
	
	private Handler handler = null; 
	
	private int idx;
	
	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	
	private String serverURL = LoginState.getIns().getServerURL();
	
	private Button new_spot_details_goback;
	private TextView jcxq_tv_spot;
	private TextView jcxq_tv_address;
	private TextView jcxq_tv_latlng;
	private TextView jcxq_tv_pm2_5;
	private TextView jcxq_tv_pm10;
	private TextView jcxq_tv_noise;
	private TextView jcxq_tv_wind_direction;
	private TextView jcxq_tv_wind_speed;
	private TextView jcxq_tv_temperature;
	private TextView jcxq_tv_humidity;
	private TextView jcxq_tv_time;
	private TextView jcxq_tv_tsp;
	
	private LinearLayout jcxq_layout_penlin;
	private Button jcxq_bt_penlin;
	
	private String spot_string;
	private String address_string;
	private String pm2_5_string;
	private String pm10_string;
	private String noise_string;
	private String wind_direction_string;
	private String wind_speed_string;
	private String humidity_string;
	private String time_string;
	private String latlng_string;
	private String temperature_string;
	private String tsp_string;
	private String penlin_status_string;
	
	private String csite_id;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			/*Intent goback_intent = new Intent(NewSpotDetailsActivity.this,MainTabActivity.class);
			startActivity(goback_intent);*/
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_spot_details);
		
		new_spot_details_goback = (Button) findViewById(R.id.new_spot_details_goback);
		
		jcxq_tv_spot = (TextView) findViewById(R.id.jcxq_tv_spot);
		jcxq_tv_address = (TextView) findViewById(R.id.jcxq_tv_address);
		jcxq_tv_latlng = (TextView) findViewById(R.id.jcxq_tv_latlng);
		jcxq_tv_pm2_5 = (TextView) findViewById(R.id.jcxq_tv_pm2_5);
		jcxq_tv_pm10 = (TextView) findViewById(R.id.jcxq_tv_pm10);
		jcxq_tv_noise = (TextView) findViewById(R.id.jcxq_tv_noise);
		jcxq_tv_wind_direction = (TextView) findViewById(R.id.jcxq_tv_wind_direction);
		jcxq_tv_wind_speed = (TextView) findViewById(R.id.jcxq_tv_wind_speed);
		jcxq_tv_temperature = (TextView) findViewById(R.id.jcxq_tv_temperature);
		jcxq_tv_humidity = (TextView) findViewById(R.id.jcxq_tv_humidity);
		jcxq_tv_time = (TextView) findViewById(R.id.jcxq_tv_time);
		jcxq_tv_tsp = (TextView) findViewById(R.id.jcxq_tv_tsp);
		
		jcxq_layout_penlin = (LinearLayout) findViewById(R.id.jcxq_layout_penlin);
		jcxq_bt_penlin = (Button) findViewById(R.id.jcxq_bt_penlin);
		
		if(!LoginState.getIns().getUi_type().equals("4")){
			//非中建八局用户不显示喷淋设备信息
			jcxq_layout_penlin.setVisibility(View.GONE);
		}
		idx = SpotInfoTabActivity.spot_idx;
		csite_id = spotInfos.get(idx).getCsite_id();
		
		new_spot_details_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*Intent intent = new Intent(NewSpotDetailsActivity.this,MainTabActivity.class);
				startActivity(intent);*/
				finish();
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
				bundle.putString("penlin_status_strinfg", penlin_status_string);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					getSpotDetails();
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
		
		checkConnect();
	}
	
	protected void checkConnect() {
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
					handler.sendEmptyMessage(NETWORK_CONNECTED);
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
				String login_path = serverURL+"csiteDetail?csite_id="
						+ csite_id;
				URL url;
				try {
					url = new URL(login_path);
					Log.i("heheda", login_path);
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
						if(item.has("csite_name"))	spot_string = item.getString("csite_name");
						if(item.has("addr"))	address_string = item.getString("addr");
						if(item.has("longitude")&&item.has("latitude"))	latlng_string = "东经"+item.getString("longitude")+" 北纬"+item.getString("latitude");
						if(item.has("recv_time"))	time_string = item.getString("recv_time");
						if(item.has("pm2_5"))	pm2_5_string = item.getString("pm2_5");
						if(item.has("pm10"))	pm10_string = item.getString("pm10");
						if(item.has("dust"))	tsp_string = item.getString("dust");//tsp?
						//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
						if(!LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
							
							if(item.has("noise") && item.getString("noise") != null && !item.getString("noise").equals("")){
								double val = Double.valueOf(item.getString("noise"));
								noise_string = ""+(int)val;
							}
							
							if(item.has("wind_direct")){
								wind_direction_string = item.getString("wind_direct");
							}
							
							if(item.has("wind_speed")){
								wind_speed_string = item.getString("wind_speed");
							}
							
							if(item.has("temp")){
								temperature_string = item.getString("temp");
							}
							
							if(item.has("humid")){
								humidity_string = item.getString("humid");
							}
							
							if(item.has("penlin_status")){
								penlin_status_string = item.getString("penlin_status");
							}
						}
						handler.sendEmptyMessage(CANCEL_PROCESS);
						handler.sendEmptyMessage(LOAD_INFO_SUCCEED);
						if(!spotInfos.get(idx).getCamera_id().equals("null")){
							String servAddr = "http://"+spotInfos.get(idx).getVideo_service_ip()+":8090";
							if(!servAddr.equals(TempData.getIns().getHikvisionServerAddr())){
								loginHikvisionServer(getMac());
							}
						}
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
		jcxq_tv_spot.setText(spot_string);
		jcxq_tv_address.setText(address_string);
		jcxq_tv_latlng.setText(latlng_string);
		jcxq_tv_time.setText(time_string);
		jcxq_tv_pm2_5.setText(pm2_5_string);
		jcxq_tv_pm10.setText(pm10_string);
		jcxq_tv_noise.setText(noise_string);
		jcxq_tv_wind_direction.setText(wind_direction_string);
		jcxq_tv_wind_speed.setText(wind_speed_string);
		jcxq_tv_temperature.setText(temperature_string);
		jcxq_tv_humidity.setText(humidity_string);
		jcxq_tv_tsp.setText(tsp_string);
		//中建八局喷淋设备的控制
		if(penlin_status_string != null && penlin_status_string.equals("-1")){
			jcxq_bt_penlin.setBackgroundResource(R.drawable.penlin_open_grey);
		}
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
				LineInfo lineInfo = null;
				ServInfo servInfo;
				
				//先退出?
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
					// 默认选择第一条线路
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
	 * 显示网络连接有问题
	 */
	private void showNetworkError(){
		Toast toast = Toast.makeText(NewSpotDetailsActivity.this, "信息加载失败，请检查网络连接",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	/**
	 * 服务器故障 ，返回码不为200
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
	 * 取消进度条
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
