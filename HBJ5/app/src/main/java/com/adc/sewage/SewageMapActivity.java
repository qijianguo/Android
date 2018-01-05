package com.adc.sewage;

import java.util.ArrayList;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.hbj5.R;
import com.adc.hbj5.MainActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.hbj5.ZJBJMainActivity;
import com.adc.surfacewater.SurfaceWaterMapActivity;
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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SewageMapActivity extends Activity {

	private Button sewage_map_goback;
	private Bitmap bitmap;
	private MapView mapView;
	private AMap aMap;
	private ArrayList<Marker> markers_list;
	
	private ArrayList<SpotInfo> spotInfos;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			/*Intent intent;
			
			if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_WUHAN)){
				intent = new Intent(SewageMapActivity.this,ZJBJMainActivity.class);
			}else{
				intent = new Intent(SewageMapActivity.this, MainActivity.class);
			}
			
			startActivity(intent);
			//recycleMemory();
			finish();*/
			return false;
		}
		return false;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sewage_map);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SewageMapActivity.this);
		
		spotInfos = new ArrayList<SpotInfo>();
		ArrayList<SpotInfo> original_infos = SpotInfoListInstance.getIns().getList();
		for(int i = 0;i < original_infos.size();i++){
			SpotInfo spotInfo = original_infos.get(i);
			int csite_type = Integer.valueOf(spotInfo.getCsite_type());
			if(csite_type != -2)	continue;
			spotInfos.add(spotInfo);
		}
		
		mapView = (MapView) findViewById(R.id.sewage_map);
		mapView.onCreate(savedInstanceState);
		
		sewage_map_goback = (Button) findViewById(R.id.sewage_map_goback);
		sewage_map_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*Intent intent;
				
				if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_WUHAN)){
					intent = new Intent(SewageMapActivity.this,ZJBJMainActivity.class);
				}else{
					intent = new Intent(SewageMapActivity.this, MainActivity.class);
				}
				
				startActivity(intent);*/
				//recycleMemory();
				finish();
			}
		});
		
		showMapInfo();
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		recycleMemory();
		super.onDestroy();
	}
	
	protected void showMapInfo() {
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		markers_list = new ArrayList<Marker>();

		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.air_excellent_2x);
		BitmapDescriptor sewage_good = BitmapDescriptorFactory
				.fromBitmap(bitmap);
		
		boolean is_first = true;
		for (int i = 0; i < spotInfos.size(); i++) {
			//误差已校正
			double latitude = Double.valueOf(spotInfos.get(i)
					.getLatitude());
			double longitude = Double.valueOf(spotInfos.get(i)
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

			tempOption.icon(sewage_good);
			Marker tempMarker = aMap.addMarker(tempOption);
			markers_list.add(tempMarker);
		}
		
		aMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				// TODO Auto-generated method stub
				for(int i = 0;i < markers_list.size();i++){
					if(marker.equals(markers_list.get(i))){
						SpotInfo spotInfo = spotInfos.get(i);
						double hour_cod = Double.valueOf(spotInfo.getHour_out_water_cod());
						double min_cod = Double.valueOf(spotInfo.getMinute_out_water_cod());
						double hour_nh3n = Double.valueOf(spotInfo.getHour_out_water_nh3n());
						double min_nh3n = Double.valueOf(spotInfo.getMinute_out_water_nh3n());
						double hour_out_flow = Double.valueOf(spotInfo.getHour_out_water_flow());
						double min_out_flow = Double.valueOf(spotInfo.getMinute_out_water_flow());
						
						marker.setTitle(spotInfo.getCsite_name()
								+"\n 出水COD(小时):"+String.format("%.3f", hour_cod)
								+"\n 出水COD(实时):"+String.format("%.3f", min_cod)
								+"\n 出水氨氮(小时):"+String.format("%.3f", hour_nh3n)
								+"\n 出水氨氮(实时):"+String.format("%.3f", min_nh3n)
								+"\n 出水流量(小时):"+String.format("%.3f", hour_out_flow)
								+"\n 出水流量(实时):"+String.format("%.3f", min_out_flow)
								+"\n 监测时间(小时):"
								+"\n"+spotInfo.getHour_sample_time()
								+"\n 监测时间(实时):"
								+"\n"+spotInfo.getMinute_sample_time());
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
}
