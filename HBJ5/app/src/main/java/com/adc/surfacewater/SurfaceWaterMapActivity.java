package com.adc.surfacewater;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.data.WryInfo;
import com.adc.data.WryRecordInfo;
import com.adc.hbj5.R;
import com.adc.hbj5.SpotInfoTabActivity;
import com.adc.pollutionsource.ZdwryActivity;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;
import com.adc.hbj5.MainActivity;
import com.adc.hbj5.MyActivityManager;
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

import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SurfaceWaterMapActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int LOAD_INFO_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 106;
	
	private Handler handler = null;
	
	private Button dbs_map_goback;
	private Bitmap bitmap;
	private MapView mapView;
	private AMap aMap;
	private List<Marker> markersList;
	
	private List<SpotInfo> surfaceWaterSpotInfos;
	private List<Integer> spotIdxs;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent(SurfaceWaterMapActivity.this,
					MainActivity.class);
			startActivity(intent);
			//recycleMemory();
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surface_water_map);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SurfaceWaterMapActivity.this);
		
		mapView = (MapView) findViewById(R.id.dbs_map);
		mapView.onCreate(savedInstanceState);
		
		dbs_map_goback = (Button) findViewById(R.id.dbs_map_goback);
		dbs_map_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SurfaceWaterMapActivity.this,
						MainActivity.class);
				startActivity(intent);
				//recycleMemory();
				finish();
			}
		});
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					query();
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
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		recycleMemory();
		super.onDestroy();
	}
	
	protected void query() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				surfaceWaterSpotInfos = new ArrayList<SpotInfo>();
				spotIdxs = new ArrayList<Integer>();
				String query_path = LoginState.getIns().getServerURL()+"jingzhou/water/jiancedianxiangqing?userId="
						+ LoginState.getIns().getUserId()
						+ "&with_data=1";
				Log.i("heheda", query_path);
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
						byte[] data = ReadStream.readStream(is);
						String json = new String(data);
						JSONObject jsonObject = new JSONObject(json);
						String jsonArrayString = jsonObject.getString("data");
						JSONArray jsonArray = new JSONArray(jsonArrayString);
						
						for(int i = 0;i < jsonArray.length();i++){
							JSONObject item = jsonArray.getJSONObject(i);
							
							SpotInfo spotInfo = new SpotInfo();
							spotInfo.setCsite_id(item.getString("csite_id"));
							spotInfo.setCsite_name(item.getString("csite_name"));
							spotInfo.setMinute_water_kmn(item.getString("minute_water_kmn"));
							spotInfo.setMinute_water_flow(item.getString("minute_water_flow"));
							spotInfo.setMinute_water_sewage(item.getString("minute_water_sewage"));
							spotInfo.setMinute_water_nh3n(item.getString("minute_water_nh3n"));
							spotInfo.setMinute_water_pho(item.getString("minute_water_pho"));
							spotInfo.setMinute_water_ntu(item.getString("minute_water_ntu"));
							spotInfo.setMinute_water_ph(item.getString("minute_water_ph"));
							spotInfo.setMinute_water_N(item.getString("minute_water_N"));
							spotInfo.setStatus(item.getString("status"));
							spotInfo.setLatitude(item.getString("latitude"));
							spotInfo.setLongitude(item.getString("longitude"));
							spotInfo.setHour_sample_time(item.getString("hour_sample_time"));
							surfaceWaterSpotInfos.add(spotInfo);
							Log.i("heheda", "idx = "+i);
						}
						handler.sendEmptyMessage(CANCEL_PROCESS);
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
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		markersList = new ArrayList<Marker>();

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_excellent_2x);
		BitmapDescriptor dbs_good = BitmapDescriptorFactory
				.fromBitmap(bitmap);
		
		boolean is_first = true;
		Log.i("heheda", "ok!!!!!!!!!!");
		for (int i = 0; i < surfaceWaterSpotInfos.size(); i++) {
			//误差已校正
			double latitude = Double.valueOf(surfaceWaterSpotInfos.get(i)
					.getLatitude());
			double longitude = Double.valueOf(surfaceWaterSpotInfos.get(i)
					.getLongitude());
			LatLng tempLatLng = new LatLng(latitude, longitude);

			if(is_first){
				aMap.moveCamera(CameraUpdateFactory
						.newCameraPosition(new CameraPosition(tempLatLng, 13,
								30, 0)));
				is_first = false;
			}
			
			MarkerOptions tempOption = new MarkerOptions();
			tempOption.position(tempLatLng);

			tempOption.icon(dbs_good);
			Marker tempMarker = aMap.addMarker(tempOption);
			markersList.add(tempMarker);
		}
		aMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			
			@Override
			public void onInfoWindowClick(Marker marker) {
				/*Bundle dataBundle = new Bundle();
				for (int i = 0; i < markersList.size(); i++) {
					if (marker.equals(markersList.get(i))) {
						dataBundle.putInt("idx", spotIdxs.get(i));
					}
				}*/
				Intent intent = new Intent(SurfaceWaterMapActivity.this,
						SurfaceWaterCurveChangeActivity.class);
				//intent.putExtras(dataBundle);
				startActivity(intent);
			}
		});
		
		aMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				// TODO Auto-generated method stub
				for(int i = 0;i < markersList.size();i++){
					if(marker.equals(markersList.get(i))){
						SpotInfo spotInfo = surfaceWaterSpotInfos.get(i);
						double minute_water_kmn = spotInfo.getMinute_water_kmn() == null ? 0 : Double.valueOf(spotInfo.getMinute_water_kmn());
						double minute_water_nh3n = spotInfo.getMinute_water_nh3n() == null ? 0 : Double.valueOf(spotInfo.getMinute_water_nh3n());
						double minute_water_pho = spotInfo.getMinute_water_pho() == null ? 0 : Double.valueOf(spotInfo.getMinute_water_pho());
						double minute_water_N = spotInfo.getMinute_water_N() == null ? 0 : Double.valueOf(spotInfo.getMinute_water_N());
						double minute_water_ph = spotInfo.getMinute_water_ph() == null ? 0 : Double.valueOf(spotInfo.getMinute_water_ph());
						double minute_water_ntu = spotInfo.getMinute_water_ntu() == null ? 0 : Double.valueOf(spotInfo.getMinute_water_ntu());
						String status = "null";
						if (spotInfo.getStatus() != null) {
							status = spotInfo.getStatus();
						}
						marker.setTitle(
								" 站点:"+spotInfo.getCsite_name()
								+"\n 标准:《地表水环境质量标准》GB3938-2002 III类"
								+"\n 状态:"+status
								+"\n 高锰酸盐(mg/l):"+String.format("%.3f", minute_water_kmn)
								+"\n 氨氮(mg/l):"+String.format("%.3f", minute_water_nh3n)
								+"\n 总磷(mg/l):"+String.format("%.3f", minute_water_pho)
								+"\n 总氮(mg/l):"+String.format("%.3f", minute_water_N)
								+"\n pH:"+String.format("%.3f", minute_water_ph)
								+"\n 浊度(NTU):"+String.format("%.3f", minute_water_ntu)
								+"\n 监测时间:"+spotInfo.getHour_sample_time()
								);
					}
				}
				return false;
			}
		});
	}
	
	private void recycleMemory(){
		if(bitmap != null){
			bitmap.recycle();
		}
		if(aMap != null){
			aMap.clear();
		}
		if(mapView != null){
			mapView.onDestroy();
		}
	}

	/**
	 * 提示服务器维护中，因url请求返回码不为200
	 */
	protected void showServerError() {
		Toast toast = Toast.makeText(SurfaceWaterMapActivity.this, "服务器维护中，请稍后再试",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 污染源信息加载进度条
	 */
	private void showLoadProgress() {
		UIUtil.showProgressDialog(this, "地表水信息加载中......");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
	protected void showNetworkError() {
		Toast toast = Toast.makeText(SurfaceWaterMapActivity.this, "亲，你还没连接网络呢= =!",
				Toast.LENGTH_SHORT);
		toast.show();
	}
}
