package com.adc.pollutionsource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.air.KqzlActivity;
import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.WryInfo;
import com.adc.data.WryRecordInfo;
import com.adc.hbj5.R;
import com.adc.hbj5.MainActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.hbj5.ZJBJMainActivity;
import com.adc.util.UIUtil;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;

import android.R.bool;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class ZdwryActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int LOAD_INFO_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 106;
	
	private Handler handler = null;
	private Button zdwry_goback;
	private MapView mapView;
	private AMap aMap;
	private ArrayList<Marker> markers_list;

	public static ArrayList<WryInfo> wry_list;

	private Bitmap bitmap1;
	private Bitmap bitmap2;
	
	private String serverURL;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			/*Intent intent;
			
			if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_WUHAN)){
				intent = new Intent(ZdwryActivity.this,ZJBJMainActivity.class);
			}else{
				intent = new Intent(ZdwryActivity.this, MainActivity.class);
			}
			
			startActivity(intent);*/
			finish();
			recycleMemory();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zdwry);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(ZdwryActivity.this);

		serverURL = LoginState.getIns().getServerURL();
		
		mapView = (MapView) findViewById(R.id.wry_map);
		mapView.onCreate(savedInstanceState);

		zdwry_goback = (Button) findViewById(R.id.zdwry_goback);
		zdwry_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*Intent intent;
				
				if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_WUHAN)){
					intent = new Intent(ZdwryActivity.this,ZJBJMainActivity.class);
				}else{
					intent = new Intent(ZdwryActivity.this, MainActivity.class);
				}
				
				startActivity(intent);*/
				recycleMemory();
				finish();
			}
		});

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					getWryList();
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
				case LOAD_INFO_SUCCEED:
					showMapInfo();
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

	protected void showNetworkError() {
		Toast toast = Toast.makeText(ZdwryActivity.this, "亲，你还没连接网络呢= =!",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void getWryList() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				wry_list = new ArrayList<WryInfo>();
				String query_path = serverURL+"pollutionSource";
				URL url;
				try {
					url = new URL(query_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(50 * 1000);
					conn.setRequestMethod("GET");
					int responseCode = conn.getResponseCode();
					if(responseCode == 200){
						InputStream is = conn.getInputStream();
						byte[] data = readStream(is);
						String json = new String(data);

						JSONArray jsonArray = new JSONArray(json);
						
						for(int i = 0;i < jsonArray.length();i++){
							JSONObject item = jsonArray.getJSONObject(i);
							
							WryInfo wryInfo = new WryInfo();
							wryInfo.setCompany_name(item.getString("name"));
							wryInfo.setLatitude(item.getString("latitude"));
							wryInfo.setLongitude(item.getString("longitude"));
							wryInfo.setPollution_source_id(item.getString("pollution_source_id"));
							
							JSONArray recordArray = item.getJSONArray("recordList");
							ArrayList<WryRecordInfo> wryRecordInfos = new ArrayList<WryRecordInfo>();
							for(int j = 0;j < recordArray.length();j++){
								WryRecordInfo wryRecordInfo = new WryRecordInfo();
								JSONObject recordItem = recordArray.getJSONObject(j);
								wryRecordInfo.setPollution_record_id(recordItem.getString("pollution_record_id"));
								wryRecordInfo.setPollution_source_id(recordItem.getString("pollution_source_id"));
								wryRecordInfo.setRecord_content(recordItem.getString("record_content"));
								wryRecordInfo.setRecord_passed(recordItem.getString("record_passed"));
								wryRecordInfo.setRecord_time(recordItem.getString("record_time"));
								wryRecordInfo.setRecord_type(recordItem.getString("record_type"));
								wryRecordInfos.add(wryRecordInfo);
							}
							wryInfo.setRecordList(wryRecordInfos);
							//这些其实没用，但是测试的时候还是先加上去
							wryInfo.setMajor_pollutant("hehehehehe");
							
							wry_list.add(wryInfo);
						}
						handler.sendEmptyMessage(LOAD_INFO_SUCCEED);
					}else{
						handler.sendEmptyMessage(URL_REQUEST_FAIL);
						handler.sendEmptyMessage(CANCEL_PROCESS);
					}
				} catch (Exception e) {
					// TODO: handle exception
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
					handler.sendEmptyMessage(CANCEL_PROCESS);
					e.printStackTrace();
				}
			}
		}.start();
	}

	protected void showMapInfo() {
		Log.i("heheda", "show start!");
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		markers_list = new ArrayList<Marker>();

		bitmap1 = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_excellent_2x);
		BitmapDescriptor wry_good = BitmapDescriptorFactory
				.fromBitmap(bitmap1);

		bitmap2 = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_extremely_polluted_2x);
		BitmapDescriptor wry_bad = BitmapDescriptorFactory
				.fromBitmap(bitmap2);
		
		/*
		 * bitmap = BitmapFactory.decodeResource(getResources(),
		 * R.drawable.air_good_2x); BitmapDescriptor air_good =
		 * BitmapDescriptorFactory.fromBitmap(bitmap);
		 * 
		 * bitmap = BitmapFactory.decodeResource(getResources(),
		 * R.drawable.air_slightly_polluted2x); BitmapDescriptor
		 * air_slightly_polluted = BitmapDescriptorFactory.fromBitmap(bitmap);
		 * 
		 * bitmap = BitmapFactory.decodeResource(getResources(),
		 * R.drawable.air_moderately_polluted_2x); BitmapDescriptor
		 * air_moderately_polluted = BitmapDescriptorFactory.fromBitmap(bitmap);
		 * 
		 * bitmap = BitmapFactory.decodeResource(getResources(),
		 * R.drawable.air_extremely_polluted_2x); BitmapDescriptor
		 * air_extremely_polluted = BitmapDescriptorFactory.fromBitmap(bitmap);
		 * 
		 * bitmap = BitmapFactory.decodeResource(getResources(),
		 * R.drawable.air_seriously_polluted_2x); BitmapDescriptor
		 * air_seriously_polluted = BitmapDescriptorFactory.fromBitmap(bitmap);
		 */

		for (int i = 0; i < wry_list.size(); i++) {
			// 误差已校正
			double latitude = Double.valueOf(wry_list.get(i).getLatitude());
			double longitude = Double.valueOf(wry_list.get(i).getLongitude());

			LatLng tempLatLng = new LatLng(latitude, longitude);
			if (i == 0) {
				aMap.moveCamera(CameraUpdateFactory
						.newCameraPosition(new CameraPosition(tempLatLng, 15,
								30, 0)));
			}

			MarkerOptions tempOption = new MarkerOptions();
			tempOption.position(tempLatLng);

			boolean flag = true;
			ArrayList<WryRecordInfo> recordList = wry_list.get(i).getRecordList();
			for(int j = 0;j <recordList.size();j++){
				if(Integer.parseInt(recordList.get(j).getRecord_passed()) != 1)	flag = false;
			}
			if(flag)	tempOption.icon(wry_good);
			else		tempOption.icon(wry_bad);

			Marker tempMarker = aMap.addMarker(tempOption);
			markers_list.add(tempMarker);
		}

		aMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				// TODO Auto-generated method stub
				Bundle dataBundle = new Bundle();
				for (int i = 0; i < markers_list.size(); i++) {
					if (marker.equals(markers_list.get(i))) {
						dataBundle.putInt("idx", i);
					}
				}
				Intent intent = new Intent(ZdwryActivity.this,
						WryDetailsActivity.class);
				intent.putExtras(dataBundle);
				startActivity(intent);
				return false;
			}
		});
		
		handler.sendEmptyMessage(CANCEL_PROCESS);
	}
	
	private byte[] readStream(InputStream is) throws IOException {
		// TODO Auto-generated method stub
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		while ((rc = is.read(buff, 0, 100)) > 0) {
			swapStream.write(buff, 0, rc);
		}
		byte[] in2b = swapStream.toByteArray();
		return in2b;
	}
	
	private void recycleMemory(){
		if(bitmap1 != null){
			bitmap1.recycle();
		}
		if(bitmap2 != null){
			bitmap2.recycle();
		}
		aMap.clear();
		mapView.onDestroy();
	}
	/**
	 * 提示服务器维护中，因url请求返回码不为200
	 */
	protected void showServerError() {
		Toast toast = Toast.makeText(ZdwryActivity.this, "服务器维护中，请稍后再试",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 污染源信息加载进度条
	 */
	private void showLoadProgress() {
		UIUtil.showProgressDialog(this, "污染源信息加载中......");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
}
