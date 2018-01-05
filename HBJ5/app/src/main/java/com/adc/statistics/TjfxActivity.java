package com.adc.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.hbj5.R;
import com.adc.hbj5.DataContrastActivity;
import com.adc.hbj5.HxdbActivity;
import com.adc.hbj5.ListViewAdapter;
import com.adc.hbj5.LoginActivity;
import com.adc.hbj5.MainActivity;
import com.adc.hbj5.MainTabActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.hbj5.ZJBJMainActivity;

import android.R.bool;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TjfxActivity extends Activity {

	private ListView listView;
	private ListViewAdapter listViewAdapter;
	private List<Map<String, Object>> listItems;
	private Button tjfx_goback;
	public static int tabIdx = 0;// 给TjfxTabActivity用的
	private int itemSize = 7;	//选项数量
	private String[] tjfx_item_name = new String[] { 
			"pm10浓度排名",
			"pm10小时均值分布",
			"pm10小时均值超标排名",
			"地表水数据传输有效率排名",
			"地表水超标时长排名",
			"地表水主要污染源占比情况"};
	private int[] imageIds = new int[] {
			R.drawable.concentration_rank_press,
			R.drawable.hourly_mean_distribution_press,
			R.drawable.pm10_monthly_report_3x_press,
			R.drawable.effective_rate_rank_press,
			R.drawable.tle_rank_press,
			R.drawable.proportion_press };

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//Intent goback_intent = new Intent(TjfxActivity.this,MainActivity.class);
			//startActivity(goback_intent);
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tjfx);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(TjfxActivity.this);

		tjfx_goback = (Button) findViewById(R.id.tjfx_goback);
		tjfx_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*Intent goback_intent = null;
				if (LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)) {
					goback_intent = new Intent(TjfxActivity.this,LoginActivity.class);
				}else{
					goback_intent = new Intent(TjfxActivity.this,MainActivity.class);
				}
				startActivity(goback_intent);*/
				finish();
			}
		});

		listView = (ListView) findViewById(R.id.tjfx_list);
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
				for(int i = 0;i < itemSize;i++) {
					if (map.get("title").equals(tjfx_item_name[i])) {
						tabIdx = i;
						break;
					}
				}
				Log.i("heheda", "tabIdx="+tabIdx);
				Intent intent = new Intent(TjfxActivity.this,
						TjfxTabActivity.class);
				startActivity(intent);
				//finish();
			}
		});
	}

	private List<Map<String, Object>> getListItems() {
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
		for (int i = 0; i < tjfx_item_name.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("image", imageIds[i]);
			map.put("title", tjfx_item_name[i]);
			listItems.add(map);
		}
		return listItems;
	}
}
