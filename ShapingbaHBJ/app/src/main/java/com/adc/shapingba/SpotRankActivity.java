package com.adc.shapingba;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.CsitePm10RankInfo;
import com.adc.data.DistrictInfo;
import com.adc.data.DistrictInfoListInstance;
import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SpotRankActivity extends Activity {

	private static final int NETWORK_CONNECTED_FOR_CSITE = 200;
	private static final int NETWORK_CONNECTED_FOR_DISTRICT = 201;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int START_PROCESS = 102;
	private static final int CANCEL_PROCESS = 103;
	private static final int GET_INFO_SUCCESS = 105;
	private static final int URL_REQUEST_FAIL = 106;
	
	private static final String NOT_FOR_CSITE = "NOT_FOR_CSITE";
	
	private Button spot_rank_goback;
	
	private TextView spot_rank_tv_title;
	private String time_title_string;
	
	private RadioGroup spot_rank_radio_group_time;
	private RadioButton spot_rank_radio_hour;
	private RadioButton spot_rank_radio_month;
	private RadioButton spot_rank_radio_day;

	private RadioGroup spot_rank_radio_group_rank;
	private RadioButton spot_rank_radio_all_spot;
	private RadioButton spot_rank_radio_district;
	private RadioButton spot_rank_radio_dis_spot;

	private LinearLayout spot_rank_layout_pm10;
	
	private TextView spot_rank_tv_change_item;
	private TextView spot_rank_tv_arrow;
	
	private Spinner spot_rank_spinner;
	
	private ListView spot_rank_list_view;
	
	private String district_id = "-1";
	private String hour_or_month_or_day = "hour";
	private boolean first_to_get_spot_dis = true;	//第一次看某个区的监测点的标记
	private boolean rank_z_to_a = true;				//是否逆序排序，默认降序：从大到小
	private ArrayList<String> vendor_list;
	private ArrayList<String> csite_id_list;		//排序列表中对应的csite_id的顺序列表
	private ArrayList<String> csite_pm10_list;		//排序列表中对应的csite_id的pm10列表
	private ArrayList<String> csite_pm10_raw_list;	//排序列表中对应的csite_id的pm10_raw列表
	
	private ArrayList<CsitePm10RankInfo> csitePm10RankInfos;

	private List<Map<String,Object>> listItems;
	private SpotRankListViewAdapter spotRankListViewAdapter;
	
	private Handler handler = null;
	
	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	private ArrayList<DistrictInfo> districtInfos = DistrictInfoListInstance.getIns().getList();
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent(SpotRankActivity.this, WuhanMainActivity.class);
			startActivity(intent);
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spot_rank);
		spot_rank_tv_title = (TextView) findViewById(R.id.spot_rank_tv_title);
		
		spot_rank_radio_group_time = (RadioGroup) findViewById(R.id.spot_rank_radio_group_time);
		spot_rank_radio_hour = (RadioButton) findViewById(R.id.spot_rank_radio_hour);
		spot_rank_radio_month = (RadioButton) findViewById(R.id.spot_rank_radio_month);
		spot_rank_radio_day = (RadioButton) findViewById(R.id.spot_rank_radio_day);

		spot_rank_radio_group_rank = (RadioGroup) findViewById(R.id.spot_rank_radio_group_rank);
		spot_rank_radio_all_spot = (RadioButton) findViewById(R.id.spot_rank_radio_all_spot);
		spot_rank_radio_district = (RadioButton) findViewById(R.id.spot_rank_radio_district);
		spot_rank_radio_dis_spot = (RadioButton) findViewById(R.id.spot_rank_radio_dis_spot);

		spot_rank_list_view = (ListView) findViewById(R.id.spot_rank_list_view);

		spot_rank_goback = (Button) findViewById(R.id.spot_rank_goback);
		spot_rank_goback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SpotRankActivity.this, WuhanMainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		spot_rank_spinner = (Spinner) findViewById(R.id.spot_rank_spinner);
		ArrayList<String> district_name_list = new ArrayList<String>();
		for(int i = 0;i < districtInfos.size();i++){
			district_name_list.add(districtInfos.get(i).getName());
		}
		ArrayAdapter<String> district_list_Adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, district_name_list);
		district_list_Adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spot_rank_spinner.setAdapter(district_list_Adapter);
		spot_rank_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// arg2 应该是position
				rank_z_to_a = true;	//修改为默认的逆序
				TextView tmp = (TextView) arg1;
				tmp.setTextColor(Color.WHITE);
				district_id = districtInfos.get(arg2).getDistrictId();
				checkConnect(NETWORK_CONNECTED_FOR_CSITE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		spot_rank_spinner.setVisibility(View.GONE);
		
		spot_rank_tv_arrow = (TextView) findViewById(R.id.spot_rank_tv_arrow);
		
		spot_rank_tv_change_item = (TextView) findViewById(R.id.spot_rank_tv_change_item);
		
		spot_rank_radio_group_time.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Log.i("heheda", "checkedId = "+checkedId);
				rank_z_to_a = true;	//修改为默认的逆序
				spot_rank_tv_arrow.setBackgroundResource(R.drawable.spot_rank_z_to_a);
				if (checkedId == spot_rank_radio_hour.getId()) {
					Log.i("heheda", "-----------------------hour");
					hour_or_month_or_day = "hour";
				} else if (checkedId == spot_rank_radio_day.getId()) {
					Log.i("heheda", "-----------------------day");
					hour_or_month_or_day = "day";
				} else if (checkedId == spot_rank_radio_month.getId()) {
					Log.i("heheda", "-----------------------month");
					hour_or_month_or_day = "month";
				}
				if (district_id.equals(NOT_FOR_CSITE)) {
					checkConnect(NETWORK_CONNECTED_FOR_DISTRICT);
				}else {
					checkConnect(NETWORK_CONNECTED_FOR_CSITE);
				}
			}
		});
		
		spot_rank_radio_group_rank.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				rank_z_to_a = true;	//修改为默认的逆序
				spot_rank_tv_arrow.setBackgroundResource(R.drawable.spot_rank_z_to_a);
				if(checkedId == spot_rank_radio_all_spot.getId()){
					district_id = "-1";
					spot_rank_tv_change_item.setText("监测点");
					spot_rank_spinner.setVisibility(View.GONE);
					checkConnect(NETWORK_CONNECTED_FOR_CSITE);
				}else if(checkedId == spot_rank_radio_district.getId()){
					district_id = NOT_FOR_CSITE;
					spot_rank_tv_change_item.setText("监测点数");
					spot_rank_spinner.setVisibility(View.GONE);
					checkConnect(NETWORK_CONNECTED_FOR_DISTRICT);
				}else if(checkedId == spot_rank_radio_dis_spot.getId()){
					spot_rank_spinner.setVisibility(View.VISIBLE);
					district_id = districtInfos.get(0).getDistrictId();
					spot_rank_tv_change_item.setText("监测点");
					
					if(!first_to_get_spot_dis){
						district_id = districtInfos.get(spot_rank_spinner.getSelectedItemPosition()).getDistrictId();
						checkConnect(NETWORK_CONNECTED_FOR_CSITE);
					}else{
						first_to_get_spot_dis = false;
					}
				}
			}
		});
		
		//spot_rank_layout_pm10，点击后调整排序
		spot_rank_layout_pm10 = (LinearLayout) findViewById(R.id.spot_rank_layout_pm10);
		spot_rank_layout_pm10.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				rank_z_to_a = !rank_z_to_a;
				if(rank_z_to_a){
					spot_rank_tv_arrow.setBackgroundResource(R.drawable.spot_rank_z_to_a);
				}else{
					spot_rank_tv_arrow.setBackgroundResource(R.drawable.spot_rank_a_to_z);
				}
				showList();
			}
		});
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED_FOR_CSITE:
					getCsitePm10Rank();
					break;
				case NETWORK_CONNECTED_FOR_DISTRICT:
					getDistrictPm10Rank();
					break;
				case NETWORK_UNCONNECTED:
					// 提示用户未连接网络
					showNetworkError();
					break;
				case URL_REQUEST_FAIL:
					// 服务器连接失败，提示服务器维护中
					showServerError();
					break;
				case START_PROCESS:
					// 显示登录进度
					Log.i("heheda", "show progress!!!!");
					showProgress();
					break;
				case CANCEL_PROCESS:
					// 取消登录进度
					Log.i("heheda", "cancel!!!!!!!!!!!!");
					cancelProgress();
					break;
				case GET_INFO_SUCCESS:
					Log.i("heheda", "GET_INFO_SUCCESS");
					showList();
				default:
					break;
				}
				;
			}
		};
		
		checkConnect(NETWORK_CONNECTED_FOR_CSITE);
		
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
	
	protected void getCsitePm10Rank() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				csitePm10RankInfos = new ArrayList<CsitePm10RankInfo>();
				// String user_id = LoginState.getIns().getUser_id();
				String json = null;
				int responseCode = 0;
				String query_path = LoginState.getIns().getServerURL() + "getCsitePm10Rank?user_id="
						+ LoginState.getIns().getUserId() + "&data_type=" + hour_or_month_or_day + "&district_id=" + district_id;
				URL url;
				try {
					url = new URL(query_path);
					Log.i("heheda", query_path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(30 * 1000);
					conn.setRequestMethod("GET");
					responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = ReadStream.readStream(is);
						json = new String(data);

						JSONArray jsonArray = new JSONArray(json);

						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							
							time_title_string = item.getString("time_stamp");
							
							CsitePm10RankInfo csitePm10RankInfo = new CsitePm10RankInfo();
							csitePm10RankInfo.setRank(item.getString("rank"));
							csitePm10RankInfo.setCamera_id(item.getString("camera_id"));
							csitePm10RankInfo.setCsite_id(item.getString("csite_id"));
							csitePm10RankInfo.setVendorName(item.getString("vendorName"));
							String csiteName_string = item.getString("csiteName");
							if(csiteName_string.length() > 6){
								csiteName_string = csiteName_string.substring(0,6)+"..";
							}
							csitePm10RankInfo.setCsiteName(csiteName_string);
							
							String districtName_string = item.getString("districtName");
							if(districtName_string.length() > 4){
								districtName_string = districtName_string.substring(0, 4);
							}
							csitePm10RankInfo.setDistrictName(districtName_string);
							
							String pm10_string = item.getString("pm10");
							double pm10_val = Double.valueOf(pm10_string);
							pm10_string = String.format("%.1f", pm10_val);
							csitePm10RankInfo.setPm10(pm10_string);
							
							csitePm10RankInfo.setPm10_raw(item.getString("pm10_raw"));
							
							csitePm10RankInfo.setVideo_service_ip(item.getString("video_service_ip"));
							csitePm10RankInfos.add(csitePm10RankInfo);
						}
						handler.sendEmptyMessage(GET_INFO_SUCCESS);
						handler.sendEmptyMessage(CANCEL_PROCESS);
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
	
	protected void getDistrictPm10Rank() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				csitePm10RankInfos = new ArrayList<CsitePm10RankInfo>();
				// String user_id = LoginState.getIns().getUser_id();
				String json = null;
				int responseCode = 0;
				String query_path = LoginState.getIns().getServerURL() + "getDistrictPm10Rank?user_id="
						+ LoginState.getIns().getUserId() + "&data_type=" + hour_or_month_or_day;
				URL url;
				try {
					url = new URL(query_path);
					Log.i("heheda", query_path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(30 * 1000);
					conn.setRequestMethod("GET");
					responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = ReadStream.readStream(is);
						json = new String(data);

						JSONArray jsonArray = new JSONArray(json);

						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							CsitePm10RankInfo csitePm10RankInfo = new CsitePm10RankInfo();
							csitePm10RankInfo.setRank(item.getString("rank"));
							//csitePm10RankInfo.setCamera_id(item.getString("camera_id"));
							//csitePm10RankInfo.setCsite_id(item.getString("csite_id"));
							csitePm10RankInfo.setCsiteName(item.getString("count"));
							
							String districtName_string = item.getString("districtName");
							if(districtName_string.length() > 4){
								districtName_string = districtName_string.substring(0, 4);
							}
							csitePm10RankInfo.setDistrictName(districtName_string);
							
							csitePm10RankInfo.setPm10(item.getString("pm10"));
							//csitePm10RankInfo.setVideo_service_ip(item.getString("video_service_ip"));
							csitePm10RankInfos.add(csitePm10RankInfo);
						}
						handler.sendEmptyMessage(GET_INFO_SUCCESS);
						handler.sendEmptyMessage(CANCEL_PROCESS);
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
	
	private ArrayList<Map<String,Object>> getListItems(){
		ArrayList<Map<String,Object>>	listItems = new ArrayList<Map<String, Object>>();
		vendor_list = new ArrayList<String>();
		csite_id_list = new ArrayList<String>();
		csite_pm10_list = new ArrayList<String>();
		csite_pm10_raw_list = new ArrayList<String>();
		if(rank_z_to_a){
			//如果要求是降序的
			for(int i = 0;i < csitePm10RankInfos.size();i++){
				Map<String,Object> map = new HashMap<String,Object>();
				CsitePm10RankInfo csitePm10RankInfo = csitePm10RankInfos.get(i);
				map.put("rank", csitePm10RankInfo.getRank());
				map.put("csite_id", csitePm10RankInfo.getCsite_id());
				map.put("csiteName", csitePm10RankInfo.getCsiteName());
				map.put("districtName", csitePm10RankInfo.getDistrictName());
				map.put("VendorName", csitePm10RankInfo.getVendorName());
				String pm10_string = csitePm10RankInfo.getPm10();
				/*double pm10_val = Double.valueOf(pm10_string);
				if (pm10_val <= 0) {
					pm10_string = "/";
				}*/
				map.put("pm10", pm10_string);
				listItems.add(map);
				
				csite_id_list.add(csitePm10RankInfo.getCsite_id());
				csite_pm10_list.add(csitePm10RankInfo.getPm10());
				csite_pm10_raw_list.add(csitePm10RankInfo.getPm10_raw());
				vendor_list.add(csitePm10RankInfo.getVendorName());
			}
		}else{
			//如果要求是升序的
			int idx = csitePm10RankInfos.size();	//idx为最后一个pm10为负的下标
			int i;
			int rank_idx = 0;
			for(i = csitePm10RankInfos.size()-1;i >= 0;i--){
				CsitePm10RankInfo csitePm10RankInfo = csitePm10RankInfos.get(i);
				String pm10_string = csitePm10RankInfo.getPm10();
				double pm10_val = Double.valueOf(pm10_string);
				if (pm10_val <= 0) {
					idx = i;
					continue;
				}
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("rank", ++rank_idx+"");
				map.put("csite_id", csitePm10RankInfo.getCsite_id());
				map.put("csiteName", csitePm10RankInfo.getCsiteName());
				map.put("districtName", csitePm10RankInfo.getDistrictName());
				map.put("VendorName", csitePm10RankInfo.getVendorName());

				map.put("pm10", pm10_string);
				listItems.add(map);
				
				csite_id_list.add(csitePm10RankInfo.getCsite_id());
				csite_pm10_list.add(csitePm10RankInfo.getPm10());
				csite_pm10_raw_list.add(csitePm10RankInfo.getPm10_raw());
				vendor_list.add(csitePm10RankInfo.getVendorName());

			}
			for(i = csitePm10RankInfos.size()-1;i >= idx;i--){
				CsitePm10RankInfo csitePm10RankInfo = csitePm10RankInfos.get(i);
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("rank", ++rank_idx+"");
				map.put("csite_id", csitePm10RankInfo.getCsite_id());
				map.put("csiteName", csitePm10RankInfo.getCsiteName());
				map.put("districtName", csitePm10RankInfo.getDistrictName());
				map.put("VendorName", csitePm10RankInfo.getVendorName());

				//map.put("pm10", "/");
				map.put("pm10", csitePm10RankInfo.getPm10());
				listItems.add(map);
				
				csite_id_list.add(csitePm10RankInfo.getCsite_id());
				csite_pm10_list.add(csitePm10RankInfo.getPm10());
				csite_pm10_raw_list.add(csitePm10RankInfo.getPm10_raw());
				vendor_list.add(csitePm10RankInfo.getVendorName());

			}
		}
		return listItems;
	}
	
	private void showList(){

		//先把时间展示出来
		spot_rank_tv_title.setText(time_title_string);
		
		listItems = getListItems();
		spotRankListViewAdapter = new SpotRankListViewAdapter(SpotRankActivity.this,listItems);
		spot_rank_list_view.setAdapter(spotRankListViewAdapter);
		spot_rank_list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(district_id.equals(NOT_FOR_CSITE)){
					return;
				}
				Bundle dataBundle = new Bundle();
				//String csite_id = csitePm10RankInfos.get(position).getCsite_id();
				String csite_id = csite_id_list.get(position);
				Log.i("heheda", "choose csite_id="+csite_id);
				int idx = 0;
				for(int i = 0;i < spotInfos.size();i++){
					if(spotInfos.get(i).getCsite_id().equals(csite_id)){
						idx = i;
						break;
					}
				}
				Log.i("heheda", "idx="+idx);
				dataBundle.putInt("idx", idx);
				
				/*20160827起不需要自动生成异常信息
				//自动异常信息根据pm10数值的-1 -2 -3 -4生成
				 
				String auto_exception_string = "";
				
				String pm10_string = csite_pm10_list.get(position);
				Log.i("heheda", "pm10_string = "+pm10_string);
				
				if(hour_or_month_or_day.equals("hour")){
					if(pm10_string.equals("-1.0")){
						auto_exception_string = "昨日无数据";
					}else if(pm10_string.equals("-2.0")){
						auto_exception_string = "PM10小时平均值过高(超过500μg/m³)";
					}else if(pm10_string.equals("-3.0")){
						auto_exception_string = "PM10小时平均值过低(低于20μg/m³)";
					}else if(pm10_string.equals("-4.0")){
						auto_exception_string = "PM10小时均值数据有效率低于80%";
					}
				}else if (hour_or_month_or_day.equals("day")) {
					if(pm10_string.equals("-1.0")){
						auto_exception_string = "上一小时无数据";
					}else if(pm10_string.equals("-2.0")){
						auto_exception_string = "PM10日平均值过高(超过500μg/m³)";
					}else if(pm10_string.equals("-3.0")){
						auto_exception_string = "PM10日平均值过低(低于20μg/m³)";
					}else if(pm10_string.equals("-4.0")){
						auto_exception_string = "PM10日均值数据有效率低于80%";
					}
				}else if (hour_or_month_or_day.equals("month")) {
					if(pm10_string.equals("-1.0")){
						auto_exception_string = "本月无数据";
					}else if(pm10_string.equals("-2.0")){
						auto_exception_string = "PM10月平均值过高(超过500μg/m³)";
					}else if(pm10_string.equals("-3.0")){
						auto_exception_string = "PM10月平均值过低(低于20μg/m³)";
					}else if(pm10_string.equals("-4.0")){
						auto_exception_string = "PM10月均值数据有效率低于80%";
					}
				}
				
				dataBundle.putString("auto_exception_string", auto_exception_string);
				*/
				String data_type = hour_or_month_or_day;
				dataBundle.putString("data_type", data_type);
				
				Intent intent = new Intent(SpotRankActivity.this,NewSpotDetailsActivity.class);
				intent.putExtras(dataBundle);
				startActivity(intent);
				//finish();
			}
			
		});
	}
	
	/**
	 * 搜索进度
	 */
	private void showProgress() {
		UIUtil.showProgressDialog(SpotRankActivity.this, "搜索中......");
	}

	/**
	 * 取消进度
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
	protected void showNetworkError() {
		Toast toast = Toast.makeText(SpotRankActivity.this, "亲，你还没连接网络呢= =!",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(SpotRankActivity.this, "服务器维护中，请稍后",
				Toast.LENGTH_SHORT);
		toast.show();
	}
}
