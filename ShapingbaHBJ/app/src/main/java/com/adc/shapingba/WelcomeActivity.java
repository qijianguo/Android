package com.adc.shapingba;

import com.adc.shapingba.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome_page);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(WelcomeActivity.this);

		Handler xHandler = new Handler();
		xHandler.postDelayed(new splashhandler(), 2000);
	}

	class splashhandler implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			startActivity(new Intent(getApplication(), LoginActivity.class));
			WelcomeActivity.this.finish();
		}

	}
}
