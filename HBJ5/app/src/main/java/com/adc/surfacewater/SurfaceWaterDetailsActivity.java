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
import com.adc.hbj5.R;
import com.adc.hbj5.MainActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.hbj5.TableListViewAdapter;
import com.adc.hbj5.TableListViewAdapter3;
import com.adc.oldactivity.SpotDetailsActivity;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;

import android.R.array;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SurfaceWaterDetailsActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int QUERY_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 106;
	
	private Handler handler = null;
	
	private Button dbsxq_goback;
	private ListView dbs_details_list;
	
	private RadioGroup surface_water_details_radio_group;
	private RadioButton[] surface_water_details_buttons;
	private final int itemSize = 6;
	private int checkedIdx;
	
	private List<String> keyList;
	private List<String> valueList;
	private List<String> timeList;
	
	private ArrayList<ArrayList<String>> lists;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surface_water_details);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SurfaceWaterDetailsActivity.this);
		
		dbsxq_goback = (Button) findViewById(R.id.dbsxq_goback);
		dbsxq_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		dbs_details_list = (ListView) findViewById(R.id.dbs_details_list);
		
		surface_water_details_radio_group = (RadioGroup) findViewById(R.id.surface_water_details_radio_group);
		
		surface_water_details_buttons = new RadioButton[itemSize];
		surface_water_details_buttons[0] = (RadioButton) findViewById(R.id.surface_water_details1);
		surface_water_details_buttons[1] = (RadioButton) findViewById(R.id.surface_water_details2);
		surface_water_details_buttons[2] = (RadioButton) findViewById(R.id.surface_water_details3);
		surface_water_details_buttons[3] = (RadioButton) findViewById(R.id.surface_water_details4);
		surface_water_details_buttons[4] = (RadioButton) findViewById(R.id.surface_water_details5);
		surface_water_details_buttons[5] = (RadioButton) findViewById(R.id.surface_water_details6);
		
		checkedIdx = 0;
		surface_water_details_radio_group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				for(int i = 0;i < itemSize;i++){
					if (checkedId == surface_water_details_buttons[i].getId()) {
						checkedIdx = i;
						break;
					}
				}
				checkConnect();
			}
		});
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					showQueryProgress();
					query();
					break;
				case NETWORK_UNCONNECTED:
					showNetworkError();
					break;
				case START_PROCESS:
					showQueryProgress();
					break;
				case CANCEL_PROCESS:
					cancelProgress();
					break;
				case QUERY_SUCCEED:
					showResult();
					cancelProgress();
					break;
				case URL_REQUEST_FAIL:
					
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
	
	protected void query() {
		new Thread() {
			@Override
			public void run() {
				String json = null;
				String query_path = LoginState.getIns().getServerURL()+"jingzhou/water/junzhiduibi?userId="
						+ LoginState.getIns().getUserId()
						+ "&type="
						+ (checkedIdx+1);
				Log.i("heheda", query_path);
				URL url;

				keyList = new ArrayList<String>();
				valueList = new ArrayList<String>();
				timeList = new ArrayList<String>();
				try {
					url = new URL(query_path);
					// Log.i("heheda",get_spot_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(50 * 1000);
					conn.setRequestMethod("GET");
					int responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = ReadStream.readStream(is);
						json = new String(data);
						JSONObject jsonObject = new JSONObject(json);
						String jsonArrayString = jsonObject.getString("data");
						JSONArray jsonArray = new JSONArray(jsonArrayString);	
						
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							String csite_name = item.getString("name");
							keyList.add(csite_name);
							String val = item.getString("value");
							valueList.add(val);
							String time = item.getString("time");
							timeList.add(time);
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
	
	void showResult(){
		lists = new ArrayList<ArrayList<String>>();
		ArrayList<String> list = new ArrayList<String>();
		list.add("监测站");
		list.add("数值");
		list.add("时间");
		lists.add(list);
		for(int i = 0;i < keyList.size();i++){
			ArrayList<String> list2 = new ArrayList<String>();
			list2.add(keyList.get(i));
			list2.add(valueList.get(i));
			list2.add(timeList.get(i));
			lists.add(list2);
		}
		
		TableListViewAdapter3 myAdapter = new TableListViewAdapter3(SurfaceWaterDetailsActivity.this, lists);
		dbs_details_list.setAdapter(myAdapter);
	}
	
	protected void showNetworkError() {
		Toast toast = Toast.makeText(SurfaceWaterDetailsActivity.this, "网络异常",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(SurfaceWaterDetailsActivity.this, "服务器维护中，请稍后再试",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 查询进度条
	 */
	private void showQueryProgress() {
		UIUtil.showProgressDialog(this, "查询中......");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
}
