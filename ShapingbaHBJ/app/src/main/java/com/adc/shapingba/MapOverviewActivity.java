package com.adc.shapingba;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.shapingba.R;
import com.adc.util.GetCurrentTimeFromBaidu;
import com.adc.util.GetSpotInfo;
import com.adc.util.IsNumericUtil;
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

import android.R.array;
import android.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressLint("HandlerLeak")
public class MapOverviewActivity extends Activity {

	private final int NETWORK_UNCONNECTED = 101;
	private final int GET_MARKER_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	
	private Handler handler = null;
	private Button dtgl_goback;
	private Button jcxq_list_bt;
	
	private RadioGroup map_overview_radioGroup;
	private RadioButton realtime_dataButton;
	private RadioButton hour_dataButton;
	private int noise_or_pm10;
	private int realtime_or_hour;
	private int current_position;
	private final int noise_checked = 0;
	private final int pm10_checked = 1;
	private final int realtime_checked = 0;
	private final int hour_checked = 1;

	private Bitmap bitmap1;
	private Bitmap bitmap2;
	private Bitmap bitmap3;
	
	private MapView mapView;
	private AMap aMap;
	private ArrayList<Marker> markersList;

	private ArrayList<SpotInfo> spotInfos;
	
	private String serverURL;
		
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//MainTabActivity.hxdb_tjfx_setup = 0;
			//Intent intent = new Intent(MapOverviewActivity.this,MainTabActivity.class);
			Intent intent = new Intent(MapOverviewActivity.this,WuhanMainActivity.class);
			startActivity(intent);
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_overview);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(MapOverviewActivity.this);

		serverURL = LoginState.getIns().getServerURL();
		
		spotInfos = new ArrayList<SpotInfo>();
		ArrayList<SpotInfo> original_infos = SpotInfoListInstance.getIns().getList();
		for(int i = 0;i < original_infos.size();i++){
			SpotInfo spotInfo = original_infos.get(i);
			int csite_type = Integer.valueOf(spotInfo.getCsite_type());
			if(csite_type == -1 || csite_type == -2)	continue;
			spotInfos.add(spotInfo);
		}
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case START_PROCESS:
					showLoadingProgress();
					break;
				case CANCEL_PROCESS:
					cancelProgress();
					break;
				case NETWORK_UNCONNECTED:
					showNetworkError();
					break;
				case GET_MARKER_SUCCEED:
					addMarkers();
				default:
					break;
				}
			}
		};

		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);

		dtgl_goback = (Button) findViewById(R.id.dtgl_goback);
		dtgl_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MapOverviewActivity.this,WuhanMainActivity.class);
				startActivity(intent);
				finish();
			}
		});

		jcxq_list_bt = (Button) findViewById(R.id.jcxq_list_bt);
		jcxq_list_bt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				//Bundle dataBundle = new Bundle();
				//dataBundle.putString("from_where", "map_overview");
				Intent intent = new Intent(MapOverviewActivity.this,JcxqActivity.class);
				//intent.putExtras(dataBundle);
				startActivity(intent);
				//recycleMemory();
				finish();
				*/
			}
		});
		
		map_overview_radioGroup = (RadioGroup) findViewById(R.id.map_overview_radio);
		realtime_dataButton = (RadioButton) findViewById(R.id.map_overview_realtime_data);
		hour_dataButton = (RadioButton) findViewById(R.id.map_overview_hour_data);
		realtime_or_hour = realtime_checked;
		current_position = -1;// 当前未选中任何监测点 current_position=-1

		//noise_or_pm10 = LoginState.getIns().getNoise_or_pm10();
		noise_or_pm10 = pm10_checked;
		map_overview_radioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						if (checkedId == realtime_dataButton.getId()) {
							realtime_or_hour = realtime_checked;
							addMarkers();
							if (current_position != -1) {
								Marker marker = markersList
										.get(current_position);
								if (noise_or_pm10 == noise_checked) {
									double val = Double
											.valueOf(spotInfos
													.get(current_position)
													.getRealtime_noise());
									marker.setTitle(spotInfos
											.get(current_position)
											.getCsite_name()
											+ "\n实时噪声：" + (int) val);
								} else if (noise_or_pm10 == pm10_checked) {
									double val = 0;
									if(IsNumericUtil.isDouble(spotInfos.get(current_position).getReltime_pm_10())){
										val = Double.valueOf(spotInfos.get(current_position).getReltime_pm_10());
									}
									String val_str = "/";
									if(val > 0){
										val_str = ""+(int)val;
									}
									marker.setTitle(spotInfos
											.get(current_position)
											.getCsite_name()
											+ "\n实时PM10：" + val_str);
								}
								marker.showInfoWindow();
							}
						} else if (checkedId == hour_dataButton.getId()) {
							realtime_or_hour = hour_checked;
							addMarkers();
							if (current_position != -1) {
								Marker marker = markersList
										.get(current_position);
								if (noise_or_pm10 == noise_checked) {
									double val = Double
											.valueOf(spotInfos
													.get(current_position)
													.getHour_noise());
									marker.setTitle(spotInfos
											.get(current_position)
											.getCsite_name()
											+ "\n小时噪声：" + (int) val);
								} else if (noise_or_pm10 == pm10_checked) {
									double val = 0;
									if(IsNumericUtil.isDouble(spotInfos.get(current_position).getHour_pm10())){
										val = Double.valueOf(spotInfos.get(current_position).getHour_pm10());
									}
									String val_str = "/";
									if(val > 0){
										val_str = ""+(int)val;
									}
									marker.setTitle(spotInfos
											.get(current_position)
											.getCsite_name()
											+ "\n小时PM10：" + val_str);
								}
								marker.showInfoWindow();
							}
						}
					}
				});
		
		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.e("heheda", "mapoverview destroy!!!!!!!!!!!!!!!");
		recycleMemory();
		super.onDestroy();
	}
	
	private void init() throws Exception {
		if (aMap == null) {
			aMap = mapView.getMap();
		}

		// 石门码头坐标测试
		/*
		 * aMap.addMarker(new MarkerOptions().position(new
		 * LatLng(29.569489-0.00605, 106.487615-0.0064)).icon(
		 * BitmapDescriptorFactory
		 * .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
		 */

		double latitude = Double.valueOf(spotInfos.get(0)
				.getLatitude());
		double longitude = Double.valueOf(spotInfos.get(0)
				.getLongitude());
		LatLng tempLatLng = new LatLng(latitude, longitude);

		aMap.moveCamera(CameraUpdateFactory
				.newCameraPosition(new CameraPosition(tempLatLng, 12, 30, 0)));

		refreshMap();

		aMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				// TODO Auto-generated method stub
				Bundle dataBundle = new Bundle();
				for (int i = 0; i < markersList.size(); i++) {
					if (marker.equals(markersList.get(i))) {
						dataBundle.putInt("idx", i);
					}
				}
				// Intent intent = new
				// Intent(MapOverviewActivity.this,SpotDetailsActivity.class);
				Intent intent = new Intent(MapOverviewActivity.this,
						NewSpotDetailsActivity.class);
				intent.putExtras(dataBundle);
				startActivity(intent);
				//recycleMemory();
				finish();
			}
		});

		aMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				// TODO Auto-generated method stub
				for (int i = 0; i < markersList.size(); i++) {
					if (marker.equals(markersList.get(i))) {
						current_position = i;
						String csite_name = spotInfos.get(i)
								.getCsite_name();
						double realtime_noise = Double
								.valueOf(spotInfos.get(i)
										.getRealtime_noise());
						double hour_noise = Double
								.valueOf(spotInfos.get(i)
										.getHour_noise());
						double realtime_pm10 = Double
								.valueOf(spotInfos.get(i)
										.getReltime_pm_10());
						double hour_pm10 = Double
								.valueOf(spotInfos.get(i)
										.getHour_pm10());
						if (realtime_or_hour == realtime_checked) {
							if (noise_or_pm10 == 0)
								marker.setTitle(csite_name + "\n实时噪声："
										+ (int) realtime_noise);
							else if (noise_or_pm10 == 1)
								marker.setTitle(csite_name + "\n实时PM10："
										+ (int) realtime_pm10);
						} else {
							if (noise_or_pm10 == 0)
								marker.setTitle(csite_name + "\n小时噪声："
										+ (int) hour_noise);
							else if (noise_or_pm10 == 1)
								marker.setTitle(csite_name + "\n小时PM10："
										+ (int) hour_pm10);
						}
					}
				}
				return false;
			}
		});
	}

	protected void addMarkers() {
		aMap.clear();

		markersList = new ArrayList<Marker>();

		bitmap1 = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_excellent_2x);
		BitmapDescriptor green_icon = BitmapDescriptorFactory
				.fromBitmap(bitmap1);

		bitmap2 = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_moderately_polluted_2x);
		BitmapDescriptor red_icon = BitmapDescriptorFactory.fromBitmap(bitmap2);

		bitmap3 = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_slightly_polluted2x);
		BitmapDescriptor yellow_icon = BitmapDescriptorFactory.fromBitmap(bitmap3);
		
		Calendar calendar = GetCurrentTimeFromBaidu.getCurrentTime();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		
		for (int i = 0; i < spotInfos.size(); i++) {
			//误差已校正
			String latitude_string = spotInfos.get(i).getLatitude();
			String longitude_string = spotInfos.get(i).getLongitude();
			if(latitude_string == null || latitude_string.equals("null"))	continue;
			if(longitude_string == null || longitude_string.equals("null"))	continue;
			double latitude = Double.valueOf(latitude_string);
			double longitude = Double.valueOf(longitude_string);
			LatLng tempLatLng = new LatLng(latitude, longitude);

			MarkerOptions tempOption = new MarkerOptions();
			tempOption.position(tempLatLng);

			Log.i("heheda", "csite_name=" +spotInfos.get(i).getCsite_name() + ",latitude="
			  + tempLatLng.latitude + ",longitude=" + tempLatLng.longitude);
			
			if (noise_or_pm10 == noise_checked) {
				/*Time t = new Time();
				t.setToNow();
				int hour = t.hour;*/
				Log.i("heheda", "hour=" + hour);
				if (realtime_or_hour == 0) {
					if (Double.valueOf(spotInfos.get(i)
							.getRealtime_noise()) <= 55) {
						tempOption.icon(green_icon);
					} else if (Double.valueOf(spotInfos.get(i)
							.getRealtime_noise()) <= 70
							&& hour >= 6
							&& hour <= 22) {
						tempOption.icon(green_icon);
					} else {
						tempOption.icon(red_icon);
					}
				} else {
					if (Double.valueOf(spotInfos.get(i)
							.getHour_noise()) <= 55) {
						tempOption.icon(green_icon);
					} else if (Double.valueOf(spotInfos.get(i)
							.getHour_noise()) <= 70 && hour >= 6 && hour <= 22) {
						tempOption.icon(green_icon);
					} else {
						tempOption.icon(red_icon);
					}
				}
			} else {
				/*if (realtime_or_hour == 0) {
					if (Double.valueOf(spotInfos.get(i)
							.getReltime_pm_10()) <= 200) {
						tempOption.icon(green_icon);
					} else {
						tempOption.icon(red_icon);
					}
				} else {
					if (Double.valueOf(spotInfos.get(i)
							.getHour_pm10()) <= 150) {
						tempOption.icon(green_icon);
					} else {
						tempOption.icon(red_icon);
					}
				}*/
				String pm10_string = null;
				if (realtime_or_hour == 0) {
					pm10_string = spotInfos.get(i).getReltime_pm_10();
				}else {
					pm10_string = spotInfos.get(i).getHour_pm10();
				}
				double pm10_val = IsNumericUtil.isDouble(pm10_string) ? Double.valueOf(pm10_string) : 0;
				if(pm10_val <= 120){
					tempOption.icon(green_icon);
				}else if (pm10_val <= 150) {
					tempOption.icon(yellow_icon);
				}else {
					tempOption.icon(red_icon);
				}
			}

			Marker tempMarker = aMap.addMarker(tempOption);
			markersList.add(tempMarker);
		}
	}

	protected void refreshMap() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				try {
					GetSpotInfo.getSpotInfo(serverURL);
					handler.sendEmptyMessage(CANCEL_PROCESS);
					handler.sendEmptyMessage(GET_MARKER_SUCCEED);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(CANCEL_PROCESS);
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
					handler.sendEmptyMessage(GET_MARKER_SUCCEED);
				}
			}
		}.start();
	}

	/**
	 * 释放内存
	 */
	private void recycleMemory(){
		if(bitmap1 != null){
			bitmap1.recycle();
			bitmap1 = null;
		}
		if(bitmap2 != null){
			bitmap2.recycle();
			bitmap2 = null;
		}
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
	 * 网络连接失败
	 */
	private void showNetworkError() {
		Toast toast = Toast.makeText(MapOverviewActivity.this,
				"网络连接失败，已显示缓存数据", Toast.LENGTH_SHORT);
		toast.show();
	}
}
