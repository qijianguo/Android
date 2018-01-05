package com.adc.shapingba;

import java.util.ArrayList;

import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.shapingba.R;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class JcxqActivity extends Activity {

	private Button jcxq_goback;
	private ListView listView;
	private ArrayList<String> spotList;
	private SmallCharacterAdapter myAdapter;
	
	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	
	private int spot_idx[];
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// MainTabActivity.hxdb_jcxq_tjfx_spzp_setup = 1;
			//Intent intent = new Intent(JcxqActivity.this, HxdbTabActivity.class);
			MainTabActivity.dtgl_qxdb_sjdb_tjfx = 0;
			Intent intent = new Intent(JcxqActivity.this, MainTabActivity.class);
			startActivity(intent);
			finish();
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jcxq);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(JcxqActivity.this);

		jcxq_goback = (Button) findViewById(R.id.jcxq_goback);
		jcxq_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// MainTabActivity.hxdb_jcxq_tjfx_spzp_setup = 1;
				//Intent intent = new Intent(JcxqActivity.this, HxdbTabActivity.class);
				MainTabActivity.dtgl_qxdb_sjdb_tjfx = 0;
				Intent intent = new Intent(JcxqActivity.this, MainTabActivity.class);
				startActivity(intent);
				finish();
			}
		});

		listView = (ListView) findViewById(R.id.jcxq_list);
		spotList = new ArrayList<String>();
		spot_idx = new int[1000];
		int cnt = 0;
		
		for (int i = 0; i < spotInfos.size(); i++) {
			int csite_type = Integer.valueOf(spotInfos.get(i).getCsite_type());
			if(csite_type == -1 || csite_type == -2)	continue;
			spotList.add(spotInfos.get(i).getCsite_name());
			spot_idx[cnt++] = i;
		}

		myAdapter = new SmallCharacterAdapter(this, spotList);
		listView.setAdapter(myAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Bundle dataBundle = new Bundle();
				int idx = (Integer) myAdapter.getItem(spot_idx[position]);
				//dataBundle.putInt("idx", idx + 100000);
				dataBundle.putInt("idx", idx);
				// Intent intent = new
				// Intent(JcxqActivity.this,SpotDetailsActivity.class);
				Intent intent = new Intent(JcxqActivity.this,
						SpotInfoTabActivity.class);
				intent.putExtras(dataBundle);
				startActivity(intent);
				finish();
			}
		});
	}
}
