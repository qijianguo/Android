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

public class WuhanComplaintActivity extends Activity {

	private static final int NETWORK_CONNECTED = 200;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int START_PROCESS = 102;
	private static final int CANCEL_PROCESS = 103;
	private static final int GET_INFO_SUCCESS = 105;
	private static final int URL_REQUEST_FAIL = 106;
		
	private Button complaint_goback;
			
	private EditText complaint_et_query_time;
	private String query_time;
	
	private ListView complaint_list_view;
	
	private String district_id = "-1";
	private String hour_or_month_or_day = "hour";
	private ArrayList<String> rank_list = new ArrayList<String>();
	private ArrayList<String> theme_list = new ArrayList<String>();
	private ArrayList<String> time_list = new ArrayList<String>();
	private ArrayList<String> detail_list = new ArrayList<String>();
	private ArrayList<String> id_list = new ArrayList<String>();
	private ArrayList<String> color_type_list = new ArrayList<String>();
	
	private ArrayList<CsitePm10RankInfo> csitePm10RankInfos;

	private List<Map<String,Object>> listItems;
	private NormalListViewAdapter normalListViewAdapter;
	
	private Handler handler = null;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent(WuhanComplaintActivity.this, WuhanMainActivity.class);
			startActivity(intent);
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wuhan_complaint);

		complaint_list_view = (ListView) findViewById(R.id.complaint_list_view);

		complaint_goback = (Button) findViewById(R.id.complaint_goback);
		complaint_goback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WuhanComplaintActivity.this, WuhanMainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		complaint_et_query_time = (EditText) findViewById(R.id.complaint_et_query_time);
		Calendar calendar = Calendar.getInstance();
		query_time = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
		complaint_et_query_time.setText(query_time);
		complaint_et_query_time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickDialogUtil dataTimePickDialog = new DatePickDialogUtil(
						WuhanComplaintActivity.this, query_time);
				dataTimePickDialog
						.dateTimePicKDialog(complaint_et_query_time);
			}
		});
		complaint_et_query_time.addTextChangedListener(new TextWatcher() {
			
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
		theme_list.clear();
		time_list.clear();
		detail_list.clear();
		id_list.clear();
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				csitePm10RankInfos = new ArrayList<CsitePm10RankInfo>();
				// String user_id = LoginState.getIns().getUser_id();
				String json = null;
				int responseCode = 0;
				String query_str = complaint_et_query_time.getText().toString();
				query_time = query_str;
				if (query_str.length() >= 16) {
					query_str = query_str.substring(0,10)+"%20"+query_str.substring(11,16)+":28";
				}
				String query_path = LoginState.getIns().getServerURL() + "getOnlineComplaintList?end_time="+query_str;
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
							rank_list.add(item.getString("serial_number"));
							theme_list.add(item.getString("theme"));
							time_list.add(item.getString("time"));
							detail_list.add(item.getString("detail"));
							id_list.add(item.getString("id"));
							if (item.getString("theme").equals("扬尘污染严重")) {
								color_type_list.add("0");
							}else if (item.getString("theme").equals("噪声过大")){
								color_type_list.add("1");
							}else {
								color_type_list.add("null");
							}
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
			map.put("district_name", theme_list.get(i));
			map.put("csite_name", time_list.get(i));
			map.put("content", detail_list.get(i));
			map.put("color_type", color_type_list.get(i));
			listItems.add(map);
		}
		return listItems;
	}
	
	private void showList(){
		
		listItems = getListItems();
		normalListViewAdapter = new NormalListViewAdapter(WuhanComplaintActivity.this,listItems,4,16,5,16,14,16);
		complaint_list_view.setAdapter(normalListViewAdapter);
		complaint_list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Bundle dataBundle = new Bundle();
				String complain_id = id_list.get(position);
				dataBundle.putString("complain_id", complain_id);
				
				Intent intent = new Intent(WuhanComplaintActivity.this,WuhanComplaintDetailsActivity.class);
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
		UIUtil.showProgressDialog(WuhanComplaintActivity.this, "搜索中......");
	}

	/**
	 * 取消进度
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
	protected void showNetworkError() {
		Toast toast = Toast.makeText(WuhanComplaintActivity.this, "亲，你还没连接网络呢= =!",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(WuhanComplaintActivity.this, "服务器维护中，请稍后",
				Toast.LENGTH_SHORT);
		toast.show();
	}
}
