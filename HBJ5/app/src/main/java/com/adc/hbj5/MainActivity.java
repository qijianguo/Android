package com.adc.hbj5;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.PrivateCredentialPermission;

import com.adc.air.KqzlActivity;
import com.adc.consts.Constants;
import com.adc.consts.Constants.Login;
import com.adc.data.LoginState;
import com.adc.data.TempData;
import com.adc.hbj5.R;
import com.adc.pollutionsource.ZdwryActivity;
import com.adc.sewage.SewageTabActivity;
import com.adc.surfacewater.SurfaceWaterTabActivity;
import com.adc.util.GetSpotInfo;
import com.adc.util.UIUtil;
import com.hik.mcrsdk.MCRSDK;
import com.hik.mcrsdk.rtsp.RtspClient;
import com.hikvision.vmsnetsdk.LineInfo;
import com.hikvision.vmsnetsdk.ServInfo;
import com.hikvision.vmsnetsdk.VMSNetSDK;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.opengl.Visibility;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {

	private Handler handler = null;
	
	private RelativeLayout activity_main_layout;
	private LinearLayout chongqin_group_layout1;
	private LinearLayout chongqin_group_layout2;
	private LinearLayout jinan_group_layout;
	
	private Button bt_main_zaosheng;
	private Button bt_main_zaosheng_jn;
	private Button bt_main_yangchen;
	private Button bt_main_yangchen_jn;
	private Button bt_main_kongqizhiliang;
	private Button bt_main_kongqizhiliang_jn;
	private Button bt_main_zhongdianwuranyuan;
	private Button bt_main_dibiaoshui;
	private Button bt_main_wushuichulichang;
	private Button bt_back_to_login;
			
	private int new_activity_id;
	
	//public static int noise_or_pm10;

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int START_PROCESS = 102;
	private final int CANCEL_PROCESS = 103;
	private final int GET_SPOT_INFO_SUCCEED = 104;
	private final int START_NEW_ACTIVITY = 109;
	
	private final int ZAOSHENG_SELECTED = 200;
	private final int YANGCHEN_SELECTED = 201;
	private final int KONGQIZHILIANG_SELECTED = 202;
	private final int ZHONGDIANWURANYUAN_SELECTED = 203;
	private final int DIBIAOSHUI_SELECTED = 204;
	private final int WUSHUICHULICHANG_SELECTED = 205;
	
	private String serverURL;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			/*Intent intent = new Intent(MainActivity.this, LoginActivity.class);
			startActivity(intent);*/
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(MainActivity.this);

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
				case KONGQIZHILIANG_SELECTED:
					doKongqizhiliangService();
					break;
				case ZHONGDIANWURANYUAN_SELECTED:
					doZhongdianwuranyuanService();
					break;
				case DIBIAOSHUI_SELECTED:
					doDibiaoshuiService();
					break;
				case WUSHUICHULICHANG_SELECTED:
					doWushuichulichangService();
					break;
				case START_NEW_ACTIVITY:
					startNewActivity();
					break;
				default:
					break;
				}
			}
		};
		activity_main_layout = (RelativeLayout) findViewById(R.id.activity_main_layout);
		chongqin_group_layout1 = (LinearLayout) findViewById(R.id.chongqin_group_layout1);
		chongqin_group_layout2 = (LinearLayout) findViewById(R.id.chongqin_group_layout2);
		jinan_group_layout = (LinearLayout) findViewById(R.id.jinan_group_layout);
		
		if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
			jinan_group_layout.setVisibility(8);
			chongqin_group_layout1.setVisibility(8);
			chongqin_group_layout2.setVisibility(8);
			handler.sendEmptyMessage(YANGCHEN_SELECTED);
		}else if(Integer.valueOf(LoginState.getIns().getCityId()) == Constants.CITY_ID_CHONGQIN){
			jinan_group_layout.setVisibility(8);
		}else if(Integer.valueOf(LoginState.getIns().getCityId()) == Constants.CITY_ID_JINAN){
			activity_main_layout.setBackgroundResource(R.drawable.main_picture_jinan_3x);
			chongqin_group_layout1.setVisibility(8);
			chongqin_group_layout2.setVisibility(8);
		}else {
			activity_main_layout.setBackgroundResource(R.drawable.main_picture_jinan_3x);
			chongqin_group_layout1.setVisibility(8);
			chongqin_group_layout2.setVisibility(8);
		}
		
		serverURL = LoginState.getIns().getServerURL();
				
		new_activity_id = 0;
				
		bt_back_to_login = (Button) findViewById(R.id.bt_back_to_login);
		bt_back_to_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Intent intent = new Intent(MainActivity.this,
						LoginActivity.class);
				startActivity(intent);*/
				finish();
			}
		});

		bt_main_zaosheng = (Button) findViewById(R.id.bt_main_zaosheng);
		bt_main_zaosheng.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(ZAOSHENG_SELECTED);
			}
		});

		bt_main_zaosheng_jn = (Button) findViewById(R.id.bt_main_zaosheng_jn);
		bt_main_zaosheng_jn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(ZAOSHENG_SELECTED);
			}
		});
		
		bt_main_yangchen = (Button) findViewById(R.id.bt_main_yangchen);
		bt_main_yangchen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(YANGCHEN_SELECTED);
			}
		});
		
		bt_main_yangchen_jn = (Button) findViewById(R.id.bt_main_yangchen_jn);
		bt_main_yangchen_jn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(YANGCHEN_SELECTED);
			}
		});

		bt_main_kongqizhiliang = (Button) findViewById(R.id.bt_main_kongqizhiliang);
		bt_main_kongqizhiliang.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(KONGQIZHILIANG_SELECTED);
			}
		});
		
		bt_main_kongqizhiliang_jn = (Button) findViewById(R.id.bt_main_kongqizhiliang_jn);
		bt_main_kongqizhiliang_jn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(KONGQIZHILIANG_SELECTED);
			}
		});


		bt_main_zhongdianwuranyuan = (Button) findViewById(R.id.bt_main_zhongdianwuranyuan);
		bt_main_zhongdianwuranyuan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(ZHONGDIANWURANYUAN_SELECTED);
			}
		});
		
		bt_main_dibiaoshui = (Button) findViewById(R.id.bt_main_dibiaoshui);
		bt_main_dibiaoshui.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(DIBIAOSHUI_SELECTED);
			}
		});
		
		bt_main_wushuichulichang = (Button) findViewById(R.id.bt_main_wushuichulichang);
		bt_main_wushuichulichang.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handler.sendEmptyMessage(WUSHUICHULICHANG_SELECTED);
			}
		});
		
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
	
	/*protected void loginHikvisionServer(){
		handler.sendEmptyMessage(START_HIKVISION_PROCESS);
		//海康视频规定的初始化
		MCRSDK.init();
		MCRSDK.setPrint(1, null);
		RtspClient.initLib();
				
		new Thread() {
			@Override
			public void run() {
				
				String servAddr = Constants.servAddr;
				LineInfo lineInfo = null;
				ServInfo servInfo;
				
				List<LineInfo> lineInfoList = new ArrayList<LineInfo>();
				boolean ret = VMSNetSDK.getInstance().getLineList(servAddr,
						lineInfoList);
				if (ret) {
					Log.i("heheda", "获得线路列表成功" + lineInfoList.size());
					// 默认选择第一条线路
					lineInfo = lineInfoList.get(0);
				} else {
					Log.i("heheda", "获得线路列表失败");
					handler.sendEmptyMessage(LOGIN_HIKVISION_SERVER_FAIL);
					handler.sendEmptyMessage(CANCEL_HIKVISION_PROCESS);
					handler.sendEmptyMessage(START_NEW_ACTIVITY);
					return;
				}

				String userName = Constants.userName;
				String password = Constants.password;

				String macAddress = getMac();

				// 登录请求
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
					TempData.getIns().setLoginData(servInfo);
					handler.sendEmptyMessage(LOGIN_HIKVISION_SERVER_SUCCEED);
					handler.sendEmptyMessage(CANCEL_HIKVISION_PROCESS);
					handler.sendEmptyMessage(START_NEW_ACTIVITY);
				}else{
					handler.sendEmptyMessage(LOGIN_HIKVISION_SERVER_FAIL);
					handler.sendEmptyMessage(CANCEL_HIKVISION_PROCESS);
					handler.sendEmptyMessage(START_NEW_ACTIVITY);
				}
			};
		}.start();
	}*/
	
	protected void showNetworkError() {
		Toast toast = Toast.makeText(MainActivity.this, "网络不给力，加载信息失败",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showLoadSpotInfoSucceed() {
		Toast toast = Toast.makeText(MainActivity.this, "加载信息成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showLoginHikvisionSucceed() {
		Toast toast = Toast.makeText(MainActivity.this, "连接视频服务器成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showLoginHikvisionFail() {
		Toast toast = Toast.makeText(MainActivity.this, "连接视频服务器失败",
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
	
	private void doKongqizhiliangService() {
		Intent intent = new Intent(MainActivity.this,
				KqzlActivity.class);
		startActivity(intent);
		//finish();
	}
	
	private void doZhongdianwuranyuanService() {
		Intent intent = new Intent(MainActivity.this,
				ZdwryActivity.class);
		startActivity(intent);
		//finish();
	}
	
	private void doDibiaoshuiService() {
		new_activity_id = DIBIAOSHUI_SELECTED;
		checkConnect();
	}
	
	private void doWushuichulichangService() {
		new_activity_id = WUSHUICHULICHANG_SELECTED;
		checkConnect();
	}
	
	private void startNewActivity(){
		if(new_activity_id == ZAOSHENG_SELECTED){
			//noise_or_pm10 = 0;
			LoginState.getIns().setNoise_or_pm10(0);
			Intent intent = new Intent(MainActivity.this,
					MainTabActivity.class);
			startActivity(intent);
			//finish();
		}else if(new_activity_id == YANGCHEN_SELECTED){
			//noise_or_pm10 = 1;
			LoginState.getIns().setNoise_or_pm10(1);
			Intent intent = new Intent(MainActivity.this,
					MainTabActivity.class);
			startActivity(intent);
			//finish();
		}else if(new_activity_id == DIBIAOSHUI_SELECTED){
			Intent intent = new Intent(MainActivity.this,
					SurfaceWaterTabActivity.class);
			startActivity(intent);
			//finish();
		}else if(new_activity_id == WUSHUICHULICHANG_SELECTED){
			Intent intent = new Intent(MainActivity.this,
					SewageTabActivity.class);
			startActivity(intent);
			//finish();
		}
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

	
}
