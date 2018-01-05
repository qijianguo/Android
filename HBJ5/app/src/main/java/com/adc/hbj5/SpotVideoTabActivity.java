package com.adc.hbj5;

import com.adc.hbj5.R;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SpotVideoTabActivity extends TabActivity {

	public static TabHost mTabHost;

	private RadioGroup spot_video_radioGroup;
	private RadioButton video_live;
	private RadioButton video_playback;
	//private RadioButton video_capture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spot_video_tab);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SpotVideoTabActivity.this);

		mTabHost = getTabHost();
		final TabWidget tabWidget = mTabHost.getTabWidget();
		// tabWidget.setStripEnabled(false);

		video_live = (RadioButton) findViewById(R.id.video_live);
		video_playback = (RadioButton) findViewById(R.id.video_playback);
		//video_capture = (RadioButton) findViewById(R.id.video_capture);

		mTabHost.addTab(mTabHost.newTabSpec("TAG1").setIndicator("0")
				.setContent(new Intent(this, VideoLiveActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG2").setIndicator("1")
				.setContent(new Intent(this, VideoPlaybackActivity.class)));
		//mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("2")
		//		.setContent(new Intent(this, VideoCaptureActivity.class)));
		mTabHost.setCurrentTab(0);

		final int my_size = 80;
		
		//直播图标
		final Drawable video_live_3x = getResources().getDrawable(
				R.drawable.video_live_3x);
		video_live_3x.setBounds(0, 0, my_size, my_size);

		final Drawable video_live_3x_press = getResources().getDrawable(
				R.drawable.video_live_3x_press);
		video_live_3x_press.setBounds(0, 0, my_size, my_size);
		
		//回看图标
		final Drawable video_playback_3x = getResources().getDrawable(
				R.drawable.video_playback_3x);
		video_playback_3x.setBounds(0, 0, my_size, my_size);

		final Drawable video_playback_3x_press = getResources().getDrawable(
				R.drawable.video_playback_3x_press);
		video_playback_3x_press.setBounds(0, 0, my_size, my_size);
		
		//抓拍图标
		/*final Drawable zpzp_3x = getResources().getDrawable
				(R.drawable.zpzp_3x);
		zpzp_3x.setBounds(0, 0, my_size, my_size);

		final Drawable zpzp_3x_press = getResources().getDrawable(
				R.drawable.zpzp_3x_press);
		zpzp_3x_press.setBounds(0, 0, my_size, my_size);
		*/
		
		// 默认设的第一个按钮selected=true
		video_live.setCompoundDrawables(null, video_live_3x_press, null, null);
		video_playback.setCompoundDrawables(null, video_playback_3x, null, null);
		//video_capture.setCompoundDrawables(null, zpzp_3x, null, null);

		spot_video_radioGroup = (RadioGroup) findViewById(R.id.spot_video_radio);
		spot_video_radioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == video_live.getId()) {
							video_live.setCompoundDrawables(null, video_live_3x_press,null, null);
							video_playback.setCompoundDrawables(null, video_playback_3x, null, null);
							//video_capture.setCompoundDrawables(null, zpzp_3x, null, null);
							mTabHost.setCurrentTab(0);
						}else if (checkedId == video_playback.getId()) {
							video_live.setCompoundDrawables(null, video_live_3x,null, null);
							video_playback.setCompoundDrawables(null, video_playback_3x_press, null, null);
							//video_capture.setCompoundDrawables(null, zpzp_3x, null, null);
							mTabHost.setCurrentTab(1);
						}/*else if (checkedId == video_capture.getId()) {
							video_live.setCompoundDrawables(null, video_live_3x,null, null);
							video_playback.setCompoundDrawables(null, video_playback_3x, null, null);
							video_capture.setCompoundDrawables(null, zpzp_3x_press, null, null);
							mTabHost.setCurrentTab(2);
						}*/
					}
				});
				
	}
}
