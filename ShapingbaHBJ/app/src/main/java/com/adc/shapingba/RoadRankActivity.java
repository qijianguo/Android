package com.adc.shapingba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RoadRankActivity extends Activity {

	private Button road_rank_goback;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent(RoadRankActivity.this, WuhanMainActivity.class);
			startActivity(intent);
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_road_rank);
		
		road_rank_goback = (Button) findViewById(R.id.road_rank_goback);
		road_rank_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RoadRankActivity.this,WuhanMainActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}
}
