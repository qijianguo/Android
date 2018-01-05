package com.adc.shapingba;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.TempData;
import com.adc.util.IsNumericUtil;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;
import com.amap.api.maps2d.model.Text;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class NewSpotPenlinActivity extends Activity implements OnClickListener{

	private static final int NETWORK_CONNECTED_FOR_GET_STATUS = 200;
	private static final int NETWORK_CONNECTED_FOR_OPERATION = 201;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int LOAD_STATUS_INFO_SUCCEED = 102;
	private static final int AUTO_CONTROL_PENLIN_SUCCEED = 105;
	private static final int MANUAL_CONTROL_PENLIN_SUCCEED = 115;
	private static final int AUTO_CONTROL_PENLIN_FAIL = 130;
	private static final int MANUAL_CONTROL_PENLIN_FAIL = 131;
	private static final int CONTRAL_STATUS_BY_TIME_SUCCEED = 107;
	private static final int CONTRAL_STATUS_BY_TIME_FAIL = 108;
	private static final int START_PROCESS = 103;
	private static final int CANCEL_PROCESS = 104;
	private static final int URL_REQUEST_FAIL = 106;
	
	private static final int HAND_STATUS_CLOSE = 0;
	private static final int HAND_STATUS_OPEN = 1;
	private static final int HAND_STATUS_NULL = -1;
	
	private Button penlin_goback;
	
	private TextView penlin_tv_spot;
	private TextView penlin_tv_address;
	private TextView penlin_tv_latlng;
	
	private LinearLayout[] penlin_layout;
	private Button[] penlin_bt_manual;
	private Button[] penlin_bt_automatic;
	
	private TextView[] penlin_tv_device_name;
	private TextView[] penlin_tv_start_time;
	private TextView[] penlin_tv_end_time;
	private TextView[] penlin_tv_time_length;
	
	private String csite_id;
	private int[] status;	//每个喷淋设备的状态
	private int[] foggun_id;	//每个喷淋设备的id
	private String[] penlin_device_name;	//喷淋设备名称
	private String[] penlin_start_time;		//喷淋开始时间
	private String[] penlin_end_time;		//喷淋结束时间
	private String[] penlin_time_length;	//喷淋时长
	private String[] timi_sta_time;			//上次定时喷淋的开始时间
	private String[] timi_end_time;			//上次定时喷淋的结束时间
	private String[] cycle_time;			//上次定时喷淋的周期
	private String[] long_time;				//上次定时喷淋的时长
	private String[] level_value;			//上次手动喷淋的阈值
	
	private int penlin_cnt = 0;	//喷淋设备的个数，目前最多4个
	private int manual_id = -1;	//用户点击手动控制喷淋设备的序号:第0,1,2,3个?
	private int automatic_id = -1;	//用户点击关闭喷淋设备的序号:第0,1,2,3个?
	private int layout_id = -1; //用户点击定时控制喷淋设备的序号:第0,1,2,3个?
	
	private String serverURL = LoginState.getIns().getServerURL();
	private String penlin_status_string;
	private Handler handler = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_spot_penlin);
		
		Intent intent = getIntent();
		Bundle dataBundle = intent.getExtras();
		
		csite_id = dataBundle.getString("csite_id");
		//penlin_status_string = dataBundle.getString("penlin_status_string");
		//status = Integer.valueOf(penlin_status_string);
		
		penlin_tv_spot = (TextView) findViewById(R.id.penlin_tv_spot);
		penlin_tv_spot.setText(dataBundle.getString("spot_string"));
		
		penlin_tv_address = (TextView) findViewById(R.id.penlin_tv_address);
		penlin_tv_address.setText(dataBundle.getString("address_string"));
		
		penlin_tv_latlng = (TextView) findViewById(R.id.penlin_tv_latlng);
		penlin_tv_latlng.setText(dataBundle.getString("latlng_string"));
		
		status = new int[4];	//每个喷淋设备的状态
		foggun_id = new int[4];	//每个喷淋设备的id
		penlin_device_name = new String[4];	//每个喷淋设备的名称
		penlin_start_time = new String[4];	//喷淋开始时间
		penlin_end_time = new String[4];		//喷淋结束时间
		penlin_time_length = new String[4];		//喷淋时长
		timi_sta_time = new String[4];			//上次定时喷淋的开始时间
		timi_end_time = new String[4];			//上次定时喷淋的结束时间
		cycle_time = new String[4];				//上次定时喷淋的周期
		long_time = new String[4];				//上次定时喷淋的时长
		level_value = new String[4];			//上次手动喷淋的阈值
		
		penlin_layout = new LinearLayout[4];
		penlin_tv_device_name = new TextView[4];
		penlin_tv_start_time = new TextView[4];
		penlin_tv_end_time = new TextView[4];
		penlin_tv_time_length = new TextView[4];
		
		penlin_tv_device_name[0] = (TextView) findViewById(R.id.penlin_tv_device_name1);
		penlin_tv_device_name[1] = (TextView) findViewById(R.id.penlin_tv_device_name2);
		penlin_tv_device_name[2] = (TextView) findViewById(R.id.penlin_tv_device_name3);
		penlin_tv_device_name[3] = (TextView) findViewById(R.id.penlin_tv_device_name4);
		
		penlin_layout[0] = (LinearLayout) findViewById(R.id.penlin_layout_1);
		penlin_layout[1] = (LinearLayout) findViewById(R.id.penlin_layout_2);
		penlin_layout[2] = (LinearLayout) findViewById(R.id.penlin_layout_3);
		penlin_layout[3] = (LinearLayout) findViewById(R.id.penlin_layout_4);
		
		penlin_layout[0].setOnClickListener(this);
		penlin_layout[1].setOnClickListener(this);
		penlin_layout[2].setOnClickListener(this);
		penlin_layout[3].setOnClickListener(this);
		
		penlin_tv_start_time[0] = (TextView) findViewById(R.id.penlin_tv_start_time1);
		penlin_tv_start_time[1] = (TextView) findViewById(R.id.penlin_tv_start_time2);
		penlin_tv_start_time[2] = (TextView) findViewById(R.id.penlin_tv_start_time3);
		penlin_tv_start_time[3] = (TextView) findViewById(R.id.penlin_tv_start_time4);

		penlin_tv_end_time[0] = (TextView) findViewById(R.id.penlin_tv_end_time1);
		penlin_tv_end_time[1] = (TextView) findViewById(R.id.penlin_tv_end_time2);
		penlin_tv_end_time[2] = (TextView) findViewById(R.id.penlin_tv_end_time3);
		penlin_tv_end_time[3] = (TextView) findViewById(R.id.penlin_tv_end_time4);
		
		penlin_tv_time_length[0] = (TextView) findViewById(R.id.penlin_tv_time_length1);
		penlin_tv_time_length[1] = (TextView) findViewById(R.id.penlin_tv_time_length2);
		penlin_tv_time_length[2] = (TextView) findViewById(R.id.penlin_tv_time_length3);
		penlin_tv_time_length[3] = (TextView) findViewById(R.id.penlin_tv_time_length4);
		
		penlin_goback = (Button) findViewById(R.id.penlin_goback);
		penlin_goback.setOnClickListener(this);
		
		penlin_bt_manual = new Button[4];
		penlin_bt_automatic = new Button[4];
		
		penlin_bt_manual[0] = (Button) findViewById(R.id.penlin_bt_manual1);
		penlin_bt_manual[1] = (Button) findViewById(R.id.penlin_bt_manual2);
		penlin_bt_manual[2] = (Button) findViewById(R.id.penlin_bt_manual3);
		penlin_bt_manual[3] = (Button) findViewById(R.id.penlin_bt_manual4);
		penlin_bt_manual[0].setOnClickListener(this);
		penlin_bt_manual[1].setOnClickListener(this);
		penlin_bt_manual[2].setOnClickListener(this);
		penlin_bt_manual[3].setOnClickListener(this);
		
		penlin_bt_automatic[0] = (Button) findViewById(R.id.penlin_bt_automatic1);
		penlin_bt_automatic[1] = (Button) findViewById(R.id.penlin_bt_automatic2);
		penlin_bt_automatic[2] = (Button) findViewById(R.id.penlin_bt_automatic3);
		penlin_bt_automatic[3] = (Button) findViewById(R.id.penlin_bt_automatic4);
		penlin_bt_automatic[0].setOnClickListener(this);
		penlin_bt_automatic[1].setOnClickListener(this);
		penlin_bt_automatic[2].setOnClickListener(this);
		penlin_bt_automatic[3].setOnClickListener(this);
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED_FOR_GET_STATUS:
					getHandStatusData();
					break;
				case NETWORK_CONNECTED_FOR_OPERATION:
					manuModeControlPenLin();
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
					showNetworkError();
					break;
				case LOAD_STATUS_INFO_SUCCEED:
					showLoadInfoSucceed();
					refreshData();
					break;
				case MANUAL_CONTROL_PENLIN_SUCCEED:
					showControlSucceed();
					refreshData();
					break;
				case AUTO_CONTROL_PENLIN_SUCCEED:
					showControlSucceed();
					//refreshData();
					break;
				case MANUAL_CONTROL_PENLIN_FAIL:
					showControlFail();
					break;
				case AUTO_CONTROL_PENLIN_FAIL:
					showControlFail();
					break;
				case CONTRAL_STATUS_BY_TIME_SUCCEED:
					showControlStatusByTimeSucceed();
					//refreshData();
					break;
				case CONTRAL_STATUS_BY_TIME_FAIL:
					showControlStatusByTimeFail();
					break;
				default:
					break;
				}
			}
		};
		
		//喷淋设备有几个显示几个，初始都不可见
		penlin_layout[0].setVisibility(View.GONE);
		penlin_layout[1].setVisibility(View.GONE);
		penlin_layout[2].setVisibility(View.GONE);
		penlin_layout[3].setVisibility(View.GONE);
		
		//先获得当前喷淋设备的状态
		checkConnect(NETWORK_CONNECTED_FOR_GET_STATUS);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.penlin_goback:
			finish();
			break;
		case R.id.penlin_bt_automatic1:
			layout_id = -1;
			manual_id = -1;
			automatic_id = 0;
			break;
		case R.id.penlin_bt_automatic2:
			layout_id = -1;
			manual_id = -1;
			automatic_id = 1;
			break;
		case R.id.penlin_bt_automatic3:
			layout_id = -1;
			manual_id = -1;
			automatic_id = 2;
			break;
		case R.id.penlin_bt_automatic4:
			layout_id = -1;
			manual_id = -1;
			automatic_id = 3;
			break;
		case R.id.penlin_bt_manual1:
			layout_id = -1;
			automatic_id = -1;
			manual_id = 0;
			break;
		case R.id.penlin_bt_manual2:
			layout_id = -1;
			automatic_id = -1;
			manual_id = 1;
			break;
		case R.id.penlin_bt_manual3:
			layout_id = -1;
			automatic_id = -1;
			manual_id = 2;
			break;
		case R.id.penlin_bt_manual4:
			layout_id = -1;
			automatic_id = -1;
			manual_id = 3;
			break;
		case R.id.penlin_layout_1:
			layout_id = 0;
			break;
		case R.id.penlin_layout_2:
			layout_id = 1;
			break;
		case R.id.penlin_layout_3:
			layout_id = 2;
			break;
		case R.id.penlin_layout_4:
			layout_id = 3;
			break;
		default:
			layout_id = -1;
			automatic_id = -1;
			manual_id = -1;
			break;
		}
		if(layout_id != -1){
			showControlPenLinStatusByTimeDialog();
		}else{
			if(automatic_id == -1){
				/*//用户要求将某设备开启
				if(status[manual_id] == HAND_STATUS_OPEN){
					Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "设备已处于开启状态",
							Toast.LENGTH_SHORT);
					toast.show();
				}else {
					checkConnect(NETWORK_CONNECTED_FOR_OPERATION);
				}*/
				showControlPenLinStatusManuallyDialog();
			}else if(manual_id == -1){
				/*//用户要求将某设备关闭
				if(status[automatic_id] == HAND_STATUS_CLOSE){
					Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "设备已处于关闭状态",
							Toast.LENGTH_SHORT);
					toast.show();
				}else {
					checkConnect(NETWORK_CONNECTED_FOR_OPERATION);
				}*/
				showControlPenLinStatusAutomaticallyDialog();
			}
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
	
	protected void getHandStatusData() {
		new Thread(){
			@Override
			public void run(){
				handler.sendEmptyMessage(START_PROCESS);
				String json = null;
				int responseCode = 0;
				String query_path = serverURL+"getPenLinByCsiteId?csite_id="
						+ csite_id;
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
						JSONArray jsonArray = new JSONArray(json);
						
						penlin_cnt = jsonArray.length();
						for(int i = 0;i < penlin_cnt;i++){
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							if(jsonObject.has("penlin_name")){
								penlin_device_name[i] = jsonObject.getString("penlin_name");
							}
							if (jsonObject.has("timi_sta_time")) {
								timi_sta_time[i] = jsonObject.getString("timi_sta_time");
							}
							if (jsonObject.has("timi_end_time")) {
								timi_end_time[i] = jsonObject.getString("timi_end_time");
							}
							if (jsonObject.has("cycle_time")) {
								cycle_time[i] = jsonObject.getString("cycle_time");
							}
							if (jsonObject.has("long_time")) {
								long_time[i] = jsonObject.getString("long_time");
							}
							if (jsonObject.has("level_value")) {
								level_value[i] = jsonObject.getString("level_value");
							}
							status[i] = jsonObject.getInt("status");
							foggun_id[i] = jsonObject.getInt("foggun_id");
							penlin_start_time[i] = jsonObject.getString("sta_time");
							penlin_end_time[i] = jsonObject.getString("end_time");
							penlin_time_length[i] = jsonObject.getString("len_time");
						}

						handler.sendEmptyMessage(CANCEL_PROCESS);
						handler.sendEmptyMessage(LOAD_STATUS_INFO_SUCCEED);
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
	
	protected void manuModeControlPenLin() {
		new Thread(){
			@Override
			public void run(){
				handler.sendEmptyMessage(START_PROCESS);
				String json = null;
				int responseCode = 0;
				int i = manual_id;
				//原先的status如果是1则边0，如果是0则变1
				int new_status = status[i] == 0 ? 1 : 0;
				String query_path = serverURL+"manuModeControlPenLin?status="
						+ new_status
						+ "&foggun_id="
						+ foggun_id[i];
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
						JSONObject jsonObject = new JSONObject(json);
						
						status[i] = jsonObject.getInt("status");
						//foggun_id[i] = jsonObject.getInt("foggun_id");
						penlin_start_time[i] = jsonObject.getString("sta_time");
						penlin_end_time[i] = jsonObject.getString("end_time");
						penlin_time_length[i] = jsonObject.getString("len_time");
						
						handler.sendEmptyMessage(CANCEL_PROCESS);
						handler.sendEmptyMessage(MANUAL_CONTROL_PENLIN_SUCCEED);
					}else{
						handler.sendEmptyMessage(CANCEL_PROCESS);
						handler.sendEmptyMessage(MANUAL_CONTROL_PENLIN_FAIL);
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
	
	protected void autoModeControlPenLin(final int status,final String val) {
		new Thread(){
			@Override
			public void run(){
				handler.sendEmptyMessage(START_PROCESS);
				String json = null;
				int responseCode = 0;
				int i = automatic_id;
				String query_path = serverURL+"autoModeControlPenLin?status="
						+ status
						+ "&foggun_id="
						+ foggun_id[i]
						+ "&level_value="
						+ val;
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
						/*JSONObject jsonObject = new JSONObject(json);
						
						NewSpotPenlinActivity.this.status[i] = jsonObject.getInt("status");
						//foggun_id[i] = jsonObject.getInt("foggun_id");
						penlin_start_time[i] = jsonObject.getString("sta_time");
						penlin_end_time[i] = jsonObject.getString("end_time");
						penlin_time_length[i] = jsonObject.getString("len_time");*/
						
						handler.sendEmptyMessage(CANCEL_PROCESS);
						handler.sendEmptyMessage(AUTO_CONTROL_PENLIN_SUCCEED);
					}else{
						handler.sendEmptyMessage(CANCEL_PROCESS);
						handler.sendEmptyMessage(AUTO_CONTROL_PENLIN_FAIL);
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
	
	protected void controlPenLinStatusByTime(final int foggun_id,final int status,final String start_time,final String end_time,final String period,final String time_length) {
		new Thread(){
			@Override
			public void run(){
				handler.sendEmptyMessage(START_PROCESS);
				String json = null;
				int responseCode = 0;
				String query_path = serverURL+"timiModeControlPenLin?foggun_id="
						+ foggun_id
						+ "&start_time="
						+ start_time
						+ "&end_time="
						+ end_time
						+ "&cycle_time="
						+ period
						+ "&long_time="
						+ time_length
						+ "&active="
						+ status;
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
						//JSONObject jsonObject = new JSONObject(json);
						//NewSpotPenlinActivity.this.status[layout_id] = jsonObject.getInt("status");
						//penlin_start_time[layout_id] = jsonObject.getString("sta_time");
						//penlin_end_time[layout_id] = jsonObject.getString("end_time");
						//penlin_time_length[layout_id] = jsonObject.getString("len_time");
						
						handler.sendEmptyMessage(CANCEL_PROCESS);
						handler.sendEmptyMessage(CONTRAL_STATUS_BY_TIME_SUCCEED);
					}else{
						handler.sendEmptyMessage(CANCEL_PROCESS);
						handler.sendEmptyMessage(CONTRAL_STATUS_BY_TIME_FAIL);
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
	
	protected void refreshData() {
		for(int i = 0;i < penlin_cnt;i++){
			penlin_layout[i].setVisibility(View.VISIBLE);
			/*if(status[i] == HAND_STATUS_CLOSE){
				penlin_bt_manual[i].setBackgroundResource(R.drawable.penlin_open);
			}else if(status[i] == HAND_STATUS_OPEN){
				penlin_bt_manual[i].setBackgroundResource(R.drawable.penlin_open_grey);
			}*/
			
			if(penlin_device_name[i] != null && penlin_device_name[i].length() >= 0){
				penlin_tv_device_name[i].setText(penlin_device_name[i]);
			}
			if(penlin_start_time[i].length() >= 21){
				penlin_start_time[i] = penlin_start_time[i].substring(11, 19);
			}
			if(penlin_end_time[i].length() >= 21){
				penlin_end_time[i] = penlin_end_time[i].substring(11, 19);
			}
			penlin_tv_start_time[i].setText("开始时间："+penlin_start_time[i]);
			penlin_tv_end_time[i].setText("结束时间："+penlin_end_time[i]);
			penlin_tv_time_length[i].setText("喷淋时长：\n"+penlin_time_length[i]);
		}
		handler.sendEmptyMessage(CANCEL_PROCESS);
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
	
	/**
	 * 显示网络连接有问题
	 */
	private void showNetworkError(){
		Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "网络错误",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showLoadInfoSucceed() {
		Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "信息加载成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showControlSucceed() {
		Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "操作成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showControlFail() {
		Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "操作失败(可能是参数设置有误)",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showControlStatusByTimeSucceed() {
		Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "设置定时开关喷淋设备信息成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showControlStatusByTimeFail() {
		Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "设置定时开关喷淋设备信息失败",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	private void showControlPenLinStatusManuallyDialog() {
		int i = 0;
		if(automatic_id == -1){
			i = manual_id;
		}else{
			i = automatic_id;
		}
		final int idx = i;
		AlertDialog.Builder builder = new AlertDialog.Builder(NewSpotPenlinActivity.this);
		
        builder.setTitle("手动控制："+penlin_device_name[i]);
        //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
        View view = LayoutInflater.from(NewSpotPenlinActivity.this).inflate(R.layout.dialog_penlin_manual, null);
        //    设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view);
        
        Button penlin_dialog_bt_open = (Button) view.findViewById(R.id.penlin_dialog_bt_open);
        Button penlin_dialog_bt_close = (Button) view.findViewById(R.id.penlin_dialog_bt_close);
        if(status[idx] == HAND_STATUS_OPEN){
        	penlin_dialog_bt_open.setBackgroundResource(R.drawable.penlin_open_grey);
        }
        
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                
            }
        });
        
        final AlertDialog mAlertDialog = builder.show();
        penlin_dialog_bt_open.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(status[idx] == HAND_STATUS_OPEN){
					Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "设备已开启",
							Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				handler.sendEmptyMessage(NETWORK_CONNECTED_FOR_OPERATION);
				mAlertDialog.dismiss();
			}
		});
        
        penlin_dialog_bt_close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(status[idx] == HAND_STATUS_CLOSE){
					Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "设备已关闭",
							Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				handler.sendEmptyMessage(NETWORK_CONNECTED_FOR_OPERATION);
				mAlertDialog.dismiss();
			}
		});
        
	}
	
	private void showControlPenLinStatusAutomaticallyDialog() {
		int i = 0;
		if(automatic_id == -1){
			i = manual_id;
		}else{
			i = automatic_id;
		}
		final int idx = i;
		AlertDialog.Builder builder = new AlertDialog.Builder(NewSpotPenlinActivity.this);
		
        builder.setTitle("自动控制："+penlin_device_name[i]);
        //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
        View view = LayoutInflater.from(NewSpotPenlinActivity.this).inflate(R.layout.dialog_penlin_automatic, null);
        //    设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view);
        
        final RadioGroup penlin_dialog_automatic_radiogp = (RadioGroup) view.findViewById(R.id.penlin_dialog_automatic_radiogp);
        final RadioButton penlin_dialog_automatic_radiobt_open = (RadioButton) view.findViewById(R.id.penlin_dialog_automatic_radiobt_open);
        final RadioButton penlin_dialog_automatic_radiobt_close = (RadioButton) view.findViewById(R.id.penlin_dialog_automatic_radiobt_close);
        final EditText penlin_dialog_automatic_threshold = (EditText) view.findViewById(R.id.penlin_dialog_automatic_threshold);
        if (level_value[i] != null && !level_value[i].equals("null")) {
        	penlin_dialog_automatic_threshold.setText(level_value[i]);
		}
        //TODO post request
        
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            	int status = 0;
                if(penlin_dialog_automatic_radiogp.getCheckedRadioButtonId() == penlin_dialog_automatic_radiobt_open.getId()){
                	status = 1;
                }else{
                	status = 0;
                }
                String threshold = penlin_dialog_automatic_threshold.getText().toString();
                if(IsNumericUtil.isDouble(threshold)){
                	if(Double.valueOf(threshold) > 0){
                		autoModeControlPenLin(status, threshold);
                		return;
                	}
                }
                Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "提交失败，阈值输入须为一个大于0的数",
						Toast.LENGTH_SHORT);
				toast.show();
            }
        });
        
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                
            }
        });
        
        final AlertDialog mAlertDialog = builder.show();
        
	}
	
	private void showControlPenLinStatusByTimeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(NewSpotPenlinActivity.this);
        builder.setTitle("请输入喷淋设备控制信息");
        //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
        View view = LayoutInflater.from(NewSpotPenlinActivity.this).inflate(R.layout.dialog_penlin_control, null);
        //    设置我们自己定义的布局文件作为弹出框的Content
        builder.setView(view);
        
        Calendar calendar = Calendar.getInstance();
        //final String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(calendar.getTime());
        String time = new SimpleDateFormat("HH:mm").format(calendar.getTime());
        final EditText penlin_dialog_et_start_time = (EditText)view.findViewById(R.id.penlin_dialog_et_start_time);
        final String start_time = timi_sta_time[layout_id] != null && !timi_sta_time[layout_id].equals("null")? timi_sta_time[layout_id] : time;
        penlin_dialog_et_start_time.setText(start_time);
        penlin_dialog_et_start_time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//DateTimePickDialogUtil dataTimePickDialog = new DateTimePickDialogUtil(
				//		NewSpotPenlinActivity.this, time);
				TimePickDialogUtil dataTimePickDialog = new TimePickDialogUtil(
						NewSpotPenlinActivity.this, start_time);
				dataTimePickDialog
						.dateTimePicKDialog(penlin_dialog_et_start_time);
			}
		});
		
        final EditText penlin_dialog_et_end_time = (EditText)view.findViewById(R.id.penlin_dialog_et_end_time);
        final String end_time = timi_end_time[layout_id] != null && !timi_end_time[layout_id].equals("null")? timi_end_time[layout_id] : time;
        penlin_dialog_et_end_time.setText(end_time);
        penlin_dialog_et_end_time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//DateTimePickDialogUtil dataTimePickDialog = new DateTimePickDialogUtil(
				//		NewSpotPenlinActivity.this, time);
				TimePickDialogUtil dataTimePickDialog = new TimePickDialogUtil(
						NewSpotPenlinActivity.this, end_time);
				dataTimePickDialog
						.dateTimePicKDialog(penlin_dialog_et_end_time);
			}
		});
        
        //开关RadioGroup
        final RadioGroup penlin_dialog_radiogp = (RadioGroup) view.findViewById(R.id.penlin_dialog_radiogp);
        final RadioButton penlin_dialog_radiobt_open = (RadioButton) view.findViewById(R.id.penlin_dialog_radiobt_open);
        final RadioButton penlin_dialog_radiobt_close = (RadioButton) view.findViewById(R.id.penlin_dialog_radiobt_close);
        
        //周期
        final EditText penlin_dialog_et_period = (EditText)view.findViewById(R.id.penlin_dialog_et_period);
        if (cycle_time[layout_id] != null) {
        	penlin_dialog_et_period.setText(cycle_time[layout_id]);
		}
        //时长
        final EditText penlin_dialog_et_time_length = (EditText)view.findViewById(R.id.penlin_dialog_et_time_length);
        if(long_time[layout_id] != null){
        	penlin_dialog_et_time_length.setText(long_time[layout_id]);
        }
        
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String start_time = penlin_dialog_et_start_time.getText().toString();
                /*if (start_time.length() >= 16) {
					start_time = start_time.substring(0,10)+"%20"+start_time.substring(11,16)+":00";
				}*/
                String end_time = penlin_dialog_et_end_time.getText().toString();
                /*if (end_time.length() >= 16) {
                	end_time = start_time.substring(0,10)+"%20"+end_time.substring(11,16)+":00";
				}*/
                int status = 0;
                if(penlin_dialog_radiogp.getCheckedRadioButtonId() == penlin_dialog_radiobt_open.getId()){
                	status = 1;
                }else{
                	status = 0;
                }
                final String period = penlin_dialog_et_period.getText().toString();
                final String time_length = penlin_dialog_et_time_length.getText().toString();
                controlPenLinStatusByTime(foggun_id[layout_id], status, start_time, end_time, period, time_length);
            } 
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                
            }
        });
        builder.show();
	}
	
	
}
