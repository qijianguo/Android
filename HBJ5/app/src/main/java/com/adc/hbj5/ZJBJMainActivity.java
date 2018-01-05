package com.adc.hbj5;

import com.adc.air.KqzlActivity;
import com.adc.data.LoginState;
import com.adc.hbj5.R;
import com.adc.sewage.SewageTabActivity;
import com.adc.surfacewater.SurfaceWaterTabActivity;
import com.adc.util.GetSpotInfo;
import com.adc.util.UIUtil;
import com.hik.mcrsdk.MCRSDK;
import com.hik.mcrsdk.rtsp.RtspClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ZJBJMainActivity extends Activity implements OnClickListener{

	private Handler handler = null;
	
	private Button zjbj_main_bt_goback;
	private Button zjbj_main_bt_yangchen;
	private Button zjbj_main_bt_zaosheng;
	
	private static final int NETWORK_CONNECTED = 100;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int START_PROCESS = 102;
	private static final int CANCEL_PROCESS = 103;
	private static final int GET_SPOT_INFO_SUCCEED = 104;
	private static final int START_NEW_ACTIVITY = 109;
	
	private static final int ZAOSHENG_SELECTED = 200;
	private static final int YANGCHEN_SELECTED = 201;
	
	private String serverURL = LoginState.getIns().getServerURL();
	
	private int new_activity_id;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent(ZJBJMainActivity.this, LoginActivity.class);
			startActivity(intent);
			finish();
			return false;
		}
		return false;
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zjbj_main);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(ZJBJMainActivity.this);
		
		//海康视频规定的初始化
		MCRSDK.init();
		MCRSDK.setPrint(1, null);
		RtspClient.initLib();
				
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					getSpotInfoFirst();
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
				case GET_SPOT_INFO_SUCCEED:
					showLoadSpotInfoSucceed();
					break;
				case ZAOSHENG_SELECTED:
					doZaoshengService();
					break;
				case YANGCHEN_SELECTED:
					doYangchenService();
					break;
				case START_NEW_ACTIVITY:
					startNewActivity();
					break;
				default:
					break;
				}
			}
		};
		
		zjbj_main_bt_goback = (Button) findViewById(R.id.zjbj_main_bt_goback);
		zjbj_main_bt_goback.setOnClickListener(this);
		
		zjbj_main_bt_yangchen = (Button) findViewById(R.id.zjbj_main_bt_yangchen);
		zjbj_main_bt_yangchen.setOnClickListener(this);

		zjbj_main_bt_zaosheng = (Button) findViewById(R.id.zjbj_main_bt_zaosheng);
		zjbj_main_bt_zaosheng.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.zjbj_main_bt_goback:
			Intent intent = new Intent(ZJBJMainActivity.this,
					LoginActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.zjbj_main_bt_yangchen:
			handler.sendEmptyMessage(YANGCHEN_SELECTED);
			break;
		case R.id.zjbj_main_bt_zaosheng:
			handler.sendEmptyMessage(ZAOSHENG_SELECTED);
			break;
		}
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
	
	protected void getSpotInfoFirst(){
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
		Toast toast = Toast.makeText(ZJBJMainActivity.this, "网络不给力，加载信息失败",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showLoadSpotInfoSucceed() {
		Toast toast = Toast.makeText(ZJBJMainActivity.this, "加载信息成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showLoginHikvisionSucceed() {
		Toast toast = Toast.makeText(ZJBJMainActivity.this, "连接视频服务器成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showLoginHikvisionFail() {
		Toast toast = Toast.makeText(ZJBJMainActivity.this, "连接视频服务器失败",
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
	
	private void doZaoshengService() {
		new_activity_id = ZAOSHENG_SELECTED;
		checkConnect();
	}
	
	private void doYangchenService() {
		new_activity_id = YANGCHEN_SELECTED;
		checkConnect();
	}
	
	private void startNewActivity(){
		if(new_activity_id == ZAOSHENG_SELECTED){
			//noise_or_pm10 = 0;
			LoginState.getIns().setNoise_or_pm10(0);
			Intent intent = new Intent(ZJBJMainActivity.this,
					MainTabActivity.class);
			startActivity(intent);
			finish();
		}else if(new_activity_id == YANGCHEN_SELECTED){
			//noise_or_pm10 = 1;
			LoginState.getIns().setNoise_or_pm10(1);
			Intent intent = new Intent(ZJBJMainActivity.this,
					MainTabActivity.class);
			startActivity(intent);
			finish();
		}
	}
}
