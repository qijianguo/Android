package com.adc.shapingba;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class WuhanDubanActivity extends Activity {

	private static final int NETWORK_CONNECTED = 200;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int START_PROCESS = 102;
	private static final int CANCEL_PROCESS = 103;
	private static final int GET_INFO_SUCCESS = 105;
	private static final int URL_REQUEST_FAIL = 106;
		
	private Button duban_goback;
			
	private EditText duban_et_query_time;
	private String query_time;
	
	private ListView duban_list_view;
	
	private String district_id = "-1";
	private String hour_or_month_or_day = "hour";
	private ArrayList<String> rank_list = new ArrayList<String>();
	private ArrayList<String> district_name_list = new ArrayList<String>();
	private ArrayList<String> csite_name_list = new ArrayList<String>();
	private ArrayList<String> id_list = new ArrayList<String>();
	private ArrayList<String> color_type_list = new ArrayList<String>();

	private ArrayList<CsitePm10RankInfo> csitePm10RankInfos;

	private List<Map<String,Object>> listItems;
	private NormalListViewAdapter normalListViewAdapter;
	
	private Handler handler = null;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent(WuhanDubanActivity.this, WuhanMainActivity.class);
			startActivity(intent);
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wuhan_duban);

		duban_list_view = (ListView) findViewById(R.id.duban_list_view);

		duban_goback = (Button) findViewById(R.id.duban_goback);
		duban_goback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WuhanDubanActivity.this, WuhanMainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		duban_et_query_time = (EditText) findViewById(R.id.duban_et_query_time);
		Calendar calendar = Calendar.getInstance();
		query_time = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
		duban_et_query_time.setText(query_time);
		duban_et_query_time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickDialogUtil dataTimePickDialog = new DatePickDialogUtil(
						WuhanDubanActivity.this, query_time);
				dataTimePickDialog
						.dateTimePicKDialog(duban_et_query_time);
			}
		});
		duban_et_query_time.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				checkConnect();
			}
		});
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					getListInfo();
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
	
	protected void getListInfo() {
		rank_list.clear();
		district_name_list.clear();
		csite_name_list.clear();
		id_list.clear();
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				csitePm10RankInfos = new ArrayList<CsitePm10RankInfo>();
				// String user_id = LoginState.getIns().getUser_id();
				String json = null;
				int responseCode = 0;
				String query_str = duban_et_query_time.getText().toString();
				query_time = query_str;
				if (query_str.length() >= 16) {
					query_str = query_str.substring(0,10)+"%20"+query_str.substring(11,16)+":28";
				}
				String query_path = LoginState.getIns().getServerURL() + "getSuperviseByTime?start_time="+query_str;
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
							rank_list.add(item.getString("rank"));
							district_name_list.add(item.getString("district_name"));
							csite_name_list.add(item.getString("csite_name"));
							id_list.add(item.getString("id"));
							color_type_list.add(item.getString("alert_type"));
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

		for(int i = 0;i < rank_list.size();i++){
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("rank", rank_list.get(i));
			//map.put("district_name", district_name_list.get(i));
			map.put("csite_name", csite_name_list.get(i));
			map.put("content", "查看详情");
			String color_type = color_type_list.get(i);
			map.put("color_type", color_type);
			if (color_type.equals("0")) {
				map.put("district_name", "待生成");
			} else if (color_type.equals("1")) {
				map.put("district_name", "待处理");
			} else if (color_type.equals("2")) {
				map.put("district_name", "已结束");
			}
			listItems.add(map);
		}
		return listItems;
	}
	
	private void showList(){
		
		listItems = getListItems();
		normalListViewAdapter = new NormalListViewAdapter(WuhanDubanActivity.this,listItems,4,6,5,16,16,16);
		duban_list_view.setAdapter(normalListViewAdapter);
		duban_list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Bundle dataBundle = new Bundle();
				String supervice_id = id_list.get(position);
				dataBundle.putString("supervice_id", supervice_id);
				String csite_name = csite_name_list.get(position);
				dataBundle.putString("csite_name", csite_name);
				Intent intent = new Intent(WuhanDubanActivity.this,WuhanDubanDetailsActivity.class);
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
		UIUtil.showProgressDialog(WuhanDubanActivity.this, "搜索中......");
	}

	/**
	 * 取消进度
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
	protected void showNetworkError() {
		Toast toast = Toast.makeText(WuhanDubanActivity.this, "亲，你还没连接网络呢= =!",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(WuhanDubanActivity.this, "服务器维护中，请稍后",
				Toast.LENGTH_SHORT);
		toast.show();
	}
}
