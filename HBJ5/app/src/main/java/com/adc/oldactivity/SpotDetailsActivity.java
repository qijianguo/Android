package com.adc.oldactivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import com.adc.hbj5.R.id;
import com.adc.hbj5.R.layout;
import com.adc.hbj5.HxdbTabActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.hbj5.SpotInfoTabActivity;
import com.adc.hbj5.TableListViewAdapter;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;
import com.hikvision.vmsnetsdk.LineInfo;
import com.hikvision.vmsnetsdk.ServInfo;
import com.hikvision.vmsnetsdk.VMSNetSDK;

import android.R.string;
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
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SpotDetailsActivity extends Activity{
	
	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int LOAD_INFO_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int NO_PHOTO = 105;
	private final int URL_REQUEST_FAIL = 106;
	
	private final int START_HIKVISION_PROCESS = 1005;
	private final int CANCEL_HIKVISION_PROCESS = 1006;
	private final int LOGIN_HIKVISION_SERVER_SUCCEED = 1007;
	private final int LOGIN_HIKVISION_SERVER_FAIL = 1008;
	
	private Handler handler = null; 
    private ArrayList<String> list;
    private ArrayList<String> list1;
    private ArrayList<String> list2;
    private ArrayList<String> list3;
    private ArrayList<String> list4;
    private ArrayList<String> list5;
    private ArrayList<String> list6;
    private ArrayList<String> list7;
    private ArrayList<String> list8;
    private ArrayList<String> list9;
    private ArrayList<String> list10;
    private ArrayList<String> list11;
    private ArrayList<String> list12;
    
    private ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
    private ListView listView;
	private TextView textView;
	private Button spot_goback;
	private int from_map_or_jcxq;
	private int idx;
	
	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	
	private String serverURL;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			CapturePhotoActivity.recyclePhotoMemory();
			//MainTabActivity.hxdb_tjfx_setup = 1;
			Intent goback_intent = new Intent(SpotDetailsActivity.this,HxdbTabActivity.class);
			startActivity(goback_intent);
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_spot_details);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SpotDetailsActivity.this);
		
		serverURL = LoginState.getIns().getServerURL();
		
		//Intent intent = getIntent();
		//Bundle dataBundle = intent.getExtras();
		textView = (TextView)findViewById(R.id.spot_detail_title);
		
		spot_goback = (Button)findViewById(R.id.spot_goback);
		//int idx = dataBundle.getInt("idx");
		idx = SpotInfoTabActivity.spot_idx;
		Log.i("heheda", "now,idx="+idx);
		if(idx >= 100000){
			idx -= 100000;
			from_map_or_jcxq = 1;
		}else{
			from_map_or_jcxq = 0;
		}
		spot_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CapturePhotoActivity.recyclePhotoMemory();
				//MainTabActivity.hxdb_tjfx_setup = 0;
				Intent goback_intent = new Intent(SpotDetailsActivity.this,HxdbTabActivity.class);
				startActivity(goback_intent);
				finish();
			}
		});
		
		//final int realtime_checked = 0;
		//final int hour_checked = 1;
		textView.setText(spotInfos.get(idx).getCsite_name());
		
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
				case LOAD_INFO_SUCCEED:
					showList();
					break;
				case URL_REQUEST_FAIL:
					showServerError();
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
						+ spotInfos.get(idx).getCsite_id();
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

						listView = (ListView) findViewById(R.id.spot_details_list);
						list = new ArrayList<String>();
						list.add("名称");
						list.add("内容");
						lists.add(list);

						list1 = new ArrayList<String>();
						list1.add("监测点");
						list1.add(item.getString("csite_name"));
						lists.add(list1);
						
						list2 = new ArrayList<String>();
						list2.add("地址");
						list2.add(item.getString("addr"));
						lists.add(list2);
						
						list3 = new ArrayList<String>();
						list3.add("经度");
						list3.add(item.getString("longitude"));
						lists.add(list3);
						
						list4 = new ArrayList<String>();
						list4.add("纬度");
						list4.add(item.getString("latitude"));
						lists.add(list4);
						
						list5 = new ArrayList<String>();
						list5.add("时间");
						list5.add(item.getString("recv_time"));
						lists.add(list5);
						
						list6 = new ArrayList<String>();
						list6.add("pm2.5(μg/m³)");
						list6.add(item.getString("pm2_5"));
						lists.add(list6);
						
						list7 = new ArrayList<String>();
						list7.add("pm10(μg/m³)");
						list7.add(item.getString("pm10"));
						lists.add(list7);
						
						//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
						if(!LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
						
							list8 = new ArrayList<String>();
							list8.add("噪声(db)");
							double val = Double.valueOf(item.getString("noise"));
							list8.add(""+(int)val);
							lists.add(list8);
							
							list9 = new ArrayList<String>();
							list9.add("风向");
							list9.add(item.getString("wind_direct"));
							lists.add(list9);
							
							list10 = new ArrayList<String>();
							list10.add("风速(m/s)");
							list10.add(item.getString("wind_speed"));
							lists.add(list10);
							
							list11 = new ArrayList<String>();
							list11.add("温度(℃)");
							list11.add(item.getString("temp"));
							lists.add(list11);
							
							list12 = new ArrayList<String>();
							list12.add("湿度(%)");
							list12.add(item.getString("humid"));
							lists.add(list12);
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
		Toast toast = Toast.makeText(SpotDetailsActivity.this, "信息加载失败，请检查网络连接",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	/**
	 * 服务器故障 ，返回码不为200
	 */
	protected void showServerError() {
		Toast toast = Toast.makeText(SpotDetailsActivity.this, "服务器维护中，请稍后",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	private void showList(){
		TableListViewAdapter myAdapter = new TableListViewAdapter(SpotDetailsActivity.this, lists);
		listView.setAdapter(myAdapter);
		//handler.sendEmptyMessage(CANCEL_PROCESS);
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
		Toast toast = Toast.makeText(SpotDetailsActivity.this, "连接视频服务器成功",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	protected void showLoginHikvisionFail() {
		Toast toast = Toast.makeText(SpotDetailsActivity.this, "连接视频服务器失败",
				Toast.LENGTH_SHORT);
		toast.show();
	}
}
