package com.adc.shapingba;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.data.LoginState;
import com.adc.util.IsNumericUtil;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.OnInfoWindowClickListener;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class WuhanStationMapActivity extends Activity {

	private static final int NETWORK_CONNECTED = 100;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int LOAD_INFO_SUCCEED = 102;
	private static final int START_PROCESS = 103;
	private static final int CANCEL_PROCESS = 104;
	private static final int URL_REQUEST_FAIL = 105;
	
	private MapView mapView;
	private AMap aMap;
	
	private String serverURL = LoginState.getIns().getServerURL();
	private String title_string;
	private Button wuhan_station_map_bt_goback;
	private TextView wuhan_station_map_tv_title;
	
	private ArrayList<Double> lo_list = new ArrayList<Double>();
	private ArrayList<Double> la_list = new ArrayList<Double>();
	private ArrayList<Double> pm10_list = new ArrayList<Double>();
	private ArrayList<String> id_list = new ArrayList<String>();
	private ArrayList<String> name_list = new ArrayList<String>();
	private ArrayList<Marker> marker_list = new ArrayList<Marker>();
	
	/*private Bitmap pm10_green_bitmap = BitmapFactory.decodeResource(getResources(),
			R.drawable.pm10_green);
	private BitmapDescriptor pm10_green_icon = BitmapDescriptorFactory
			.fromBitmap(pm10_green_bitmap);
	private Bitmap pm10_yellow_bitmap = BitmapFactory.decodeResource(getResources(),
			R.drawable.pm10_yellow);
	private BitmapDescriptor pm10_yellow_icon = BitmapDescriptorFactory
			.fromBitmap(pm10_yellow_bitmap);
	private Bitmap pm10_red_bitmap = BitmapFactory.decodeResource(getResources(),
			R.drawable.pm10_red);
	private BitmapDescriptor pm10_red_icon = BitmapDescriptorFactory
			.fromBitmap(pm10_red_bitmap);*/
	
	private Handler handler = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	protected void onDestroy() {
		recycleMemory();
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("heheda", "0000000000000000000000");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wuhan_station_map);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(WuhanStationMapActivity.this);
		
		Log.i("heheda", "1111111111111111111111");
		mapView = (MapView) findViewById(R.id.wuhan_station_map);
		mapView.onCreate(savedInstanceState);
		
		wuhan_station_map_bt_goback = (Button) findViewById(R.id.wuhan_station_map_bt_goback);
		wuhan_station_map_bt_goback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		wuhan_station_map_tv_title = (TextView) findViewById(R.id.wuhan_station_map_tv_title);
		Log.i("heheda", "22222222222222222222222");
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					getWuhanstationMap();
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
					addMarkers();
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
	
	protected void getWuhanstationMap() {
		new Thread(){
			@Override
			public void run(){
				handler.sendEmptyMessage(START_PROCESS);
				String json = null;
				int responseCode = 0;
				String query_path = serverURL+"stationList?user_id="+LoginState.getIns().getUserId();
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

						JSONArray jsonArray = new JSONArray(json);
						Log.i("heheda", "len="+jsonArray.length());
						for(int i = 0;i < jsonArray.length();i++){
							Log.i("heheda", "i="+i);
							JSONObject item = jsonArray.getJSONObject(i);
							String id = item.getString("station_code");
							String station_name = item.getString("station_name");
							String longitude = item.getString("longitude");
							String latitude = item.getString("latitude");
							String pm10 = item.getString("pm10");
							
							if(i == 0)	title_string = item.getString("time");
							if(!IsNumericUtil.isDouble(longitude) || !IsNumericUtil.isDouble(latitude)){
								continue;
							}
							
							double lo = Double.valueOf(longitude);
							double la = Double.valueOf(latitude);
							Log.i("heheda", "lo="+lo+",la="+la);
							double pm10_val = Double.valueOf(pm10);
							
							la_list.add(la);
							lo_list.add(lo);
							pm10_list.add(pm10_val);
							name_list.add(station_name);
							id_list.add(id);
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
	
	protected void addMarkers() {
		Log.i("heheda", "33333333333333333333333");
		
		if(title_string != null && title_string.length() >= 21){
			title_string = title_string.substring(0,19);
		}
		wuhan_station_map_tv_title.setText(title_string);

		Bitmap pm10_green_bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.pm10_green);
		BitmapDescriptor pm10_green_icon = BitmapDescriptorFactory
				.fromBitmap(pm10_green_bitmap);
		Bitmap pm10_yellow_bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.pm10_yellow);
		BitmapDescriptor pm10_yellow_icon = BitmapDescriptorFactory
				.fromBitmap(pm10_yellow_bitmap);
		Bitmap pm10_red_bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.pm10_red);
		BitmapDescriptor pm10_red_icon = BitmapDescriptorFactory
				.fromBitmap(pm10_red_bitmap);
		Bitmap pm10_grey_bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.pm10_grey);
		BitmapDescriptor pm10_grey_icon = BitmapDescriptorFactory
				.fromBitmap(pm10_grey_bitmap);
		Log.i("heheda", "44444444444444444");
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		Log.i("heheda", "555555555555555555");
		if(la_list.size() == 0) return;
		
		double la = la_list.get(0);
		double lo = lo_list.get(0);
		
		LatLng tempLatLng = new LatLng(la, lo);
		LatLng centerLatLng = new LatLng(29.55, 106.57);
		aMap.moveCamera(CameraUpdateFactory
				.newCameraPosition(new CameraPosition(centerLatLng, 10, 30, 0)));
		Log.i("heheda", "6666666666666666");
		aMap.clear();
		Log.i("heheda", "777777777777777");
		for(int i = 0;i < la_list.size();i++){
			la = la_list.get(i);
			lo = lo_list.get(i);
			tempLatLng = new LatLng(la, lo);
			
			MarkerOptions tempOption = new MarkerOptions();
			tempOption.position(tempLatLng);
			Log.i("heheda", "88888888888888 idx="+i);
			double pm10_val = pm10_list.get(i);
			if(pm10_val <= 50){
				if(pm10_val <= 0)	tempOption.icon(pm10_grey_icon);
				else				tempOption.icon(pm10_green_icon);
			}else if (pm10_val <= 150) {
				tempOption.icon(pm10_yellow_icon);
			}else {
				tempOption.icon(pm10_red_icon);
			}
			
			Marker tempMarker = aMap.addMarker(tempOption);
			marker_list.add(tempMarker);
		}
		
		aMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			
			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO Auto-generated method stub
				Bundle dataBundle = new Bundle();
				for (int i = 0; i < marker_list.size(); i++) {
					if (marker.equals(marker_list.get(i))) {
						dataBundle.putString("stationWh_id", id_list.get(i));
						dataBundle.putString("station_name", name_list.get(i));
					
						Intent intent = new Intent(WuhanStationMapActivity.this,
								ZurveChangeActivity.class);
						intent.putExtras(dataBundle);
						startActivity(intent);
						//finish();
					}
				}
			}
		});
		
		aMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				for(int i = 0;i < marker_list.size();i++){
					if(marker.equals(marker_list.get(i))){
						String pm10_string;
						double pm10_val = pm10_list.get(i);
						if(pm10_val <= 0){
							pm10_string = name_list.get(i)+"\nPM10:/";
						}else{
							pm10_string = name_list.get(i)+"\nPM10:"+pm10_list.get(i);
						}
						marker.setTitle(pm10_string);
					}
				}
				return false;
			}
		});
	}
	
	/**
	 * 释放内存
	 */
	private void recycleMemory(){
		/*if(pm10_green_bitmap != null){
			pm10_green_bitmap.recycle();
			pm10_green_bitmap = null;
		}
		if(pm10_yellow_bitmap != null){
			pm10_yellow_bitmap.recycle();
			pm10_yellow_bitmap = null;
		}
		if(pm10_red_bitmap != null){
			pm10_red_bitmap.recycle();
			pm10_red_bitmap = null;
		}*/
		if(aMap != null){
			aMap.clear();
		}
		if(mapView != null){
			mapView.onDestroy();
		}
	}
	
	/**
	 * 数据加载中
	 */
	private void showLoadingProgress() {
		UIUtil.showProgressDialog(this, "数据加载中");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
	/**
	 * 网络错误
	 */
	private void showNetworkError() {
		Toast toast = Toast.makeText(WuhanStationMapActivity.this,
				"网络错误", Toast.LENGTH_SHORT);
		toast.show();
	}
	
	/**
	 * URL返回错误
	 */
	private void showServerError() {
		Toast toast = Toast.makeText(WuhanStationMapActivity.this,
				"网络错误", Toast.LENGTH_SHORT);
		toast.show();
	}
}
