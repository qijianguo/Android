package com.adc.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.shapingba.HxdbActivity;
import com.adc.shapingba.ListViewAdapter;
import com.adc.shapingba.LoginActivity;
import com.adc.shapingba.MyActivityManager;
import com.adc.shapingba.R;
import com.adc.shapingba.WuhanMainActivity;

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
	public static int pm10mon_or_nightnoise_or_noisedis_or_pm10dis = 0;// 给TjfxTabActivity用的
	private String[] tjfx_item_name = new String[] { "颗粒物月报", "夜间噪声", "噪声分布",
			"颗粒物分布" };
	private int[] imageIds = new int[] {
			R.drawable.pm10_monthly_report_3x_press,
			R.drawable.night_noise_3x_press,
			R.drawable.night_noise_distribution_3x_press,
			R.drawable.pm10_distribution_3x_press };

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
			Intent intent = new Intent(TjfxActivity.this,WuhanMainActivity.class);
			startActivity(intent);
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
				Intent intent = new Intent(TjfxActivity.this,WuhanMainActivity.class);
				startActivity(intent);
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
				if (map.get("title").equals(tjfx_item_name[0])) {
					pm10mon_or_nightnoise_or_noisedis_or_pm10dis = 0;
				} else if (map.get("title").equals(tjfx_item_name[1])) {
					pm10mon_or_nightnoise_or_noisedis_or_pm10dis = 1;
				} else if (map.get("title").equals(tjfx_item_name[2])) {
					pm10mon_or_nightnoise_or_noisedis_or_pm10dis = 2;
				} else if (map.get("title").equals(tjfx_item_name[3])) {
					pm10mon_or_nightnoise_or_noisedis_or_pm10dis = 3;
				}
				Intent intent = new Intent(TjfxActivity.this,
						TjfxTabActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	private List<Map<String, Object>> getListItems() {
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		boolean is_tianjin = false;
		//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
		if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
			is_tianjin = true;
		}
		for (int i = 0; i < tjfx_item_name.length; i++) {
			if(is_tianjin && (i == 1 || i == 2))	continue;
			//Log.i("heheda", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa:"+i);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("image", imageIds[i]);
			map.put("title", tjfx_item_name[i]);
			listItems.add(map);
		}
		return listItems;
	}
}
