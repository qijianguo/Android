package com.adc.sewage;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.hbj5.R;
import com.adc.hbj5.MainActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.hbj5.ZJBJMainActivity;
import com.adc.pollutionsource.ZdwryActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SewageCurveActivity extends Activity {

	private Button wsclcqxbh_goback;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			/*Intent intent;
			
			if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_WUHAN)){
				intent = new Intent(SewageCurveActivity.this,ZJBJMainActivity.class);
			}else{
				intent = new Intent(SewageCurveActivity.this, MainActivity.class);
			}
			
			startActivity(intent);*/
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sewage_curve);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SewageCurveActivity.this);
		
		wsclcqxbh_goback = (Button) findViewById(R.id.wsclcqxbh_goback);
		wsclcqxbh_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*Intent intent;
				
				if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_WUHAN)){
					intent = new Intent(SewageCurveActivity.this,ZJBJMainActivity.class);
				}else{
					intent = new Intent(SewageCurveActivity.this, MainActivity.class);
				}
				
				startActivity(intent);*/
				finish();
			}
		});
	}
}
