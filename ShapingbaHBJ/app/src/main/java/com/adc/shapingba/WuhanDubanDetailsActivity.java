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

public class WuhanDubanDetailsActivity extends Activity {

	private static final int NETWORK_CONNECTED = 200;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int START_PROCESS = 102;
	private static final int CANCEL_PROCESS = 103;
	private static final int GET_INFO_SUCCESS = 105;
	private static final int URL_REQUEST_FAIL = 106;
		
	private Button duban_details_goback;
	private TextView duban_details_tv_title;
	private ListView duban_details_list_view;
	
	private String supervice_id;
	private ArrayList<String> rank_list = new ArrayList<String>();
	private ArrayList<String> handle_people_list = new ArrayList<String>();
	private ArrayList<String> handle_time_list = new ArrayList<String>();
	private ArrayList<String> solve_situation_list = new ArrayList<String>();
	
	private ArrayList<CsitePm10RankInfo> csitePm10RankInfos;

	private List<Map<String,Object>> listItems;
	private NormalListViewAdapter normalListViewAdapter;
	
	private Handler handler = null;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//Intent intent = new Intent(WuhanDubanDetailsActivity.this, WuhanMainActivity.class);
			//startActivity(intent);
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wuhan_duban_details);

		Intent intent = getIntent();
		Bundle dataBundle = intent.getExtras();
		supervice_id = dataBundle.getString("supervice_id");
		
		duban_details_tv_title = (TextView) findViewById(R.id.duban_details_tv_title);
		String csite_name = dataBundle.getString("csite_name");
		duban_details_tv_title.setText(csite_name);

		duban_details_list_view = (ListView) findViewById(R.id.duban_details_list_view);

		duban_details_goback = (Button) findViewById(R.id.duban_details_goback);
		duban_details_goback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Intent intent = new Intent(WuhanDubanDetailsActivity.this, WuhanMainActivity.class);
				//startActivity(intent);
				finish();
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
				};
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
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				csitePm10RankInfos = new ArrayList<CsitePm10RankInfo>();
				// String user_id = LoginState.getIns().getUser_id();
				String json = null;
				int responseCode = 0;
				String query_path = LoginState.getIns().getServerURL() + "getSuperviseHandleStatus?supervice_id="+supervice_id;
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
							handle_people_list.add(item.getString("handle_people"));
							handle_time_list.add(item.getString("handle_time"));
							solve_situation_list.add(item.getString("solve_situation"));
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
			map.put("district_name", handle_people_list.get(i));
			map.put("csite_name", handle_time_list.get(i));
			map.put("content", solve_situation_list.get(i));
			listItems.add(map);
		}
		return listItems;
	}
	
	private void showList(){
		
		listItems = getListItems();
		normalListViewAdapter = new NormalListViewAdapter(WuhanDubanDetailsActivity.this,listItems,4,16,5,16,14,16);
		duban_details_list_view.setAdapter(normalListViewAdapter);
		duban_details_list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				/*Bundle dataBundle = new Bundle();
				String supervice_id = id_list.get(position);
				dataBundle.putString("supervice_id", supervice_id);
				
				Intent intent = new Intent(WuhanDubanDetailsActivity.this,???.class);
				intent.putExtras(dataBundle);
				startActivity(intent);
				//finish();*/
			}
			
		});
	}
	
	/**
	 * 搜索进度
	 */
	private void showProgress() {
		UIUtil.showProgressDialog(WuhanDubanDetailsActivity.this, "搜索中......");
	}

	/**
	 * 取消进度
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
	protected void showNetworkError() {
		Toast toast = Toast.makeText(WuhanDubanDetailsActivity.this, "亲，你还没连接网络呢= =!",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(WuhanDubanDetailsActivity.this, "服务器维护中，请稍后",
				Toast.LENGTH_SHORT);
		toast.show();
	}
}
