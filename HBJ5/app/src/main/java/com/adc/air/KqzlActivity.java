package com.adc.air;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.AirQualityInfo;
import com.adc.data.LoginState;
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
import android.text.StaticLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class KqzlActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int QUERY_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 106;
	
	private Bitmap bitmap;
	
	private Handler handler = null;
	private Button kqzl_goback;
	private MapView mapView;
	private AMap aMap;
	private ArrayList<Marker> markers_list;

	public static ArrayList<AirQualityInfo> station_list;
	
	private String serverURL;

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			/*Intent intent;
			if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_WUHAN)){
				intent = new Intent(KqzlActivity.this,ZJBJMainActivity.class);
			}else{
				intent = new Intent(KqzlActivity.this, MainActivity.class);
			}
			
			startActivity(intent);*/
			recycleMemory();
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kqzl);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(KqzlActivity.this);

		serverURL = LoginState.getIns().getServerURL();
		
		mapView = (MapView) findViewById(R.id.air_map);
		mapView.onCreate(savedInstanceState);

		kqzl_goback = (Button) findViewById(R.id.kqzl_goback);
		kqzl_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*Intent intent;
				
				if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_WUHAN)){
					intent = new Intent(KqzlActivity.this,ZJBJMainActivity.class);
				}else{
					intent = new Intent(KqzlActivity.this, MainActivity.class);
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
					getStationList();
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
				case QUERY_SUCCEED:
					loadMapInfo();
					break;
				case URL_REQUEST_FAIL:
					showServerError();
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

	protected void getStationList() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				station_list = new ArrayList<AirQualityInfo>();
				String user_id = LoginState.getIns().getUserId();
				String spot_path = serverURL+"stationList?user_id="
						+ user_id;
				Log.i("heheda", spot_path);
				URL url;
				try {
					url = new URL(spot_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(30 * 1000);
					conn.setRequestMethod("GET");
					int responsecode = conn.getResponseCode();
					if (responsecode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = readStream(is);
						String json = new String(data);
						Log.i("heheda", json);
						JSONArray jsonArray = new JSONArray(json);

						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							//调试阶段，经纬度设为了null
							if(item.getString("longitude").compareTo("null") == 0)	continue;
							AirQualityInfo airQualityInfo = new AirQualityInfo();
							airQualityInfo.setTime(item.getString("time"));
							airQualityInfo.setSource(item.getString("source"));
							airQualityInfo.setLongitude(item
									.getString("longitude"));
							airQualityInfo.setLatitude(item
									.getString("latitude"));
							airQualityInfo.setAir_index(item
									.getString("air_index"));
							airQualityInfo.setStation_code(item
									.getString("station_code"));
							airQualityInfo.setStation_name(item
									.getString("station_name"));
							station_list.add(airQualityInfo);
							Log.i("heheda", "station list ok" + i);
						}
						handler.sendEmptyMessage(QUERY_SUCCEED);
					} else {
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

	protected void loadMapInfo() {
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		markers_list = new ArrayList<Marker>();

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_excellent_2x);
		BitmapDescriptor air_excellent = BitmapDescriptorFactory
				.fromBitmap(bitmap);

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_good_2x);
		BitmapDescriptor air_good = BitmapDescriptorFactory.fromBitmap(bitmap);

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_slightly_polluted2x);
		BitmapDescriptor air_slightly_polluted = BitmapDescriptorFactory
				.fromBitmap(bitmap);

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_moderately_polluted_2x);
		BitmapDescriptor air_moderately_polluted = BitmapDescriptorFactory
				.fromBitmap(bitmap);

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_extremely_polluted_2x);
		BitmapDescriptor air_extremely_polluted = BitmapDescriptorFactory
				.fromBitmap(bitmap);

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_seriously_polluted_2x);
		BitmapDescriptor air_seriously_polluted = BitmapDescriptorFactory
				.fromBitmap(bitmap);
		Log.i("heheda", "ppppppppppplllllllllll");
		for (int i = 0; i < station_list.size(); i++) {
			// 误差已校正
			double latitude = Double.valueOf(station_list.get(i).getLatitude());
			double longitude = Double.valueOf(station_list.get(i).getLongitude());
			Log.i("heheda", "add point"+i+",la="+latitude+",lo:"+longitude);
			LatLng tempLatLng = new LatLng(latitude, longitude);
			// 这里选第0个点似乎并不合理,看了下大致分布，选第1个
			if (i == 1) {
				aMap.moveCamera(CameraUpdateFactory
						.newCameraPosition(new CameraPosition(tempLatLng, 10,
								30, 0)));
			}
			MarkerOptions tempOption = new MarkerOptions();
			tempOption.position(tempLatLng);
			Log.i("heheda", "csite_name="
					+ station_list.get(i).getStation_name() + ",latitude="
					+ tempLatLng.latitude + ",longitude="
					+ tempLatLng.longitude);

			// tempOption.icon(BitmapDescriptorFactory
			// .defaultMarker(BitmapDescriptorFactory.HUE_RED));
			double air_index = Double.valueOf(station_list.get(i).getAir_index());
			Log.i("heheda", "air_index="+air_index);
			if (air_index <= 50) {
				tempOption.icon(air_excellent);
			} else if (air_index <= 100) {
				tempOption.icon(air_good);
			} else if (air_index <= 150) {
				tempOption.icon(air_slightly_polluted);
			} else if (air_index <= 200) {
				tempOption.icon(air_moderately_polluted);
			} else if (air_index <= 300) {
				tempOption.icon(air_extremely_polluted);
			} else {
				tempOption.icon(air_seriously_polluted);
			}

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
				Intent intent = new Intent(KqzlActivity.this,
						AirConditionTestingStationDetailsActivity.class);
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
		if(bitmap != null){
			bitmap.recycle();
		}
		aMap.clear();
		mapView.onDestroy();
	}
	/**
	 * 数据加载
	 */
	private void showLoadingProgress() {
		Log.i("hehda", "loadinginging");
		UIUtil.showProgressDialog(this, "数据加载中......");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		Log.i("heheda", "canceling canceling");
		UIUtil.cancelProgressDialog();
	}

	/**
	 * 显示网络连接有问题
	 */
	private void showNetworkError() {
		Toast toast = Toast.makeText(KqzlActivity.this, "信息加载失败，请检查网络连接",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 服务器故障 返回码不是200
	 */
	private void showServerError() {
		Toast toast = Toast.makeText(KqzlActivity.this, "服务器维护中，请稍后",
				Toast.LENGTH_SHORT);
		toast.show();
	}
}
