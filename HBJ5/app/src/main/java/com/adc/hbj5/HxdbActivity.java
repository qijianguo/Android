package com.adc.hbj5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.hbj5.R;

import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class HxdbActivity extends Activity {

	private ListView listView;
	private ListViewAdapter listViewAdapter;
	private List<Map<String, Object>> listItems;
	private Button hxdb_goback;
	public static int map_or_curve_or_data = 0;// 给HxdbTabActivity用的
	private String[] hxdb_item_name = new String[] { "地图概览", "曲线对比", "数据对比" };
	private int[] imageIds = new int[] { R.drawable.map_3x_press,
			R.drawable.line_3x_press, R.drawable.data_3x_press };

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
			Intent intent;
			if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
				intent = new Intent(HxdbActivity.this,LoginActivity.class);
			}else if(LoginState.getIns().getUi_type().equals("4")){
				//中建八局用户ui_type="4"
				intent = new Intent(HxdbActivity.this, ZJBJMainActivity.class);
			}else{
				intent = new Intent(HxdbActivity.this,MainActivity.class);
			}
			startActivity(intent);
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hxdb);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(HxdbActivity.this);

		hxdb_goback = (Button) findViewById(R.id.hxdb_goback);
		hxdb_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
				Intent intent;
				if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
					intent = new Intent(HxdbActivity.this,LoginActivity.class);
				}else if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_WUHAN)){
					intent = new Intent(HxdbActivity.this, ZJBJMainActivity.class);
				}else{
					intent = new Intent(HxdbActivity.this,MainActivity.class);
				}
				startActivity(intent);
				finish();
			}
		});

		listView = (ListView) findViewById(R.id.hxdb_list);
		listItems = getListItems();
		listViewAdapter = new ListViewAdapter(this, listItems);
		listView.setAdapter(listViewAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Log.i("heheda", "hehe="
						+ listViewAdapter.getItem(position).get("title"));
				Map<String, Object> map = listViewAdapter
						.getItem(position);
				if (map.get("title").equals(hxdb_item_name[0])) {
					map_or_curve_or_data = 0;
				} else if (map.get("title").equals(hxdb_item_name[1])) {
					map_or_curve_or_data = 1;
				} else if (map.get("title").equals(hxdb_item_name[2])) {
					map_or_curve_or_data = 2;
				}
				Intent intent = new Intent(HxdbActivity.this,
						HxdbTabActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	private List<Map<String, Object>> getListItems() {
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < hxdb_item_name.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("image", imageIds[i]);
			map.put("title", hxdb_item_name[i]);
			listItems.add(map);
		}
		return listItems;
	}
}
