package com.adc.hbj5;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.TempData;
import com.adc.hbj5.R;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
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
import android.widget.TextView;
import android.widget.Toast;

public class NewSpotPenlinActivity extends Activity {

	private static final int NETWORK_CONNECTED_FOR_GET_STATUS = 200;
	private static final int NETWORK_CONNECTED_FOR_OPERATION = 201;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int LOAD_STATUS_INFO_SUCCEED = 102;
	private static final int OPERATOR_STATUS_INFO_SUCCEED = 105;
	private static final int START_PROCESS = 103;
	private static final int CANCEL_PROCESS = 104;
	private static final int URL_REQUEST_FAIL = 106;
	
	private static final int HAND_STATUS_CLOSE = 0;
	private static final int HAND_STATUS_OPEN = 1;
	private static final int HAND_STATUS_NULL = -1;
	
	private Button penlin_goback;
	private Button penlin_bt_open;
	private Button penlin_bt_close;
	
	private TextView penlin_tv_spot;
	private TextView penlin_tv_address;
	private TextView penlin_tv_latlng;
	private TextView penlin_tv_start_time;
	private TextView penlin_tv_end_time;
	private TextView penlin_tv_time_length;
	
	private String csite_id;
	private int status;
	private String penlin_start_time;	//喷淋开始时间
	private String penlin_end_time;		//喷淋结束时间
	private String penlin_time_length;	//喷淋时长
	
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
		penlin_status_string = dataBundle.getString("penlin_status_string");
		status = Integer.valueOf(penlin_status_string);
		
		penlin_tv_spot = (TextView) findViewById(R.id.penlin_tv_spot);
		penlin_tv_spot.setText(dataBundle.getString("spot_string"));
		
		penlin_tv_address = (TextView) findViewById(R.id.penlin_tv_address);
		penlin_tv_address.setText(dataBundle.getString("address_string"));
		
		penlin_tv_latlng = (TextView) findViewById(R.id.penlin_tv_latlng);
		penlin_tv_latlng.setText(dataBundle.getString("latlng_string"));
		
		penlin_tv_start_time = (TextView) findViewById(R.id.penlin_tv_start_time);
		penlin_tv_end_time = (TextView) findViewById(R.id.penlin_tv_end_time);
		penlin_tv_time_length = (TextView) findViewById(R.id.penlin_tv_time_length);

		penlin_goback = (Button) findViewById(R.id.penlin_goback);
		penlin_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		penlin_bt_open = (Button) findViewById(R.id.penlin_bt_open);
		penlin_bt_open.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(status == HAND_STATUS_OPEN){
					Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "设备处于开启状态",
							Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				checkConnect(NETWORK_CONNECTED_FOR_OPERATION);
			}
		});
		
		penlin_bt_close = (Button) findViewById(R.id.penlin_bt_close);
		penlin_bt_close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(status == HAND_STATUS_CLOSE){
					Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "设备处于关闭状态",
							Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				checkConnect(NETWORK_CONNECTED_FOR_OPERATION);
			}
		});
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED_FOR_GET_STATUS:
					getHandStatusData();
					break;
				case NETWORK_CONNECTED_FOR_OPERATION:
					operateHandStatusData();
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
				case OPERATOR_STATUS_INFO_SUCCEED:
					showDoOperatorSucceed();
					refreshData();
				default:
					break;
				}
			}
		};
		
		//先获得当前喷淋设备的状态
		checkConnect(NETWORK_CONNECTED_FOR_GET_STATUS);
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
				String query_path = serverURL+"getHandStatusData?csite_id="
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
						JSONObject jsonObject = new JSONObject(json);
						
						status = jsonObject.getInt("status");
						penlin_start_time = jsonObject.getString("sta_time");
						penlin_end_time = jsonObject.getString("end_time");
						penlin_time_length = jsonObject.getString("len_time");
						
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
	
	protected void operateHandStatusData() {
		new Thread(){
			@Override
			public void run(){
				handler.sendEmptyMessage(START_PROCESS);
				String json = null;
				int responseCode = 0;
				String query_path = serverURL+"getOpenStatusData?handopen_status="
						+ status
						+ "&csite_id="
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
						JSONObject jsonObject = new JSONObject(json);
						
						status = jsonObject.getInt("status");
						
						penlin_start_time = jsonObject.getString("sta_time");
						penlin_end_time = jsonObject.getString("end_time");
						penlin_time_length = jsonObject.getString("len_time");
						
						handler.sendEmptyMessage(CANCEL_PROCESS);
						handler.sendEmptyMessage(OPERATOR_STATUS_INFO_SUCCEED);
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
		if(status == HAND_STATUS_CLOSE){
			penlin_bt_open.setBackgroundResource(R.drawable.penlin_open);
		}else if(status == HAND_STATUS_OPEN){
			penlin_bt_open.setBackgroundResource(R.drawable.penlin_open_grey);
		}
		
		penlin_tv_start_time.setText("开始时间："+penlin_start_time);
		penlin_tv_end_time.setText("结束时间："+penlin_end_time);
		penlin_tv_time_length.setText("喷淋时长："+penlin_time_length);
		
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
	
	protected void showDoOperatorSucceed() {
		Toast toast = Toast.makeText(NewSpotPenlinActivity.this, "操作成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}

}
