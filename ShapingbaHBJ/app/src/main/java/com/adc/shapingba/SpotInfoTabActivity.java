package com.adc.shapingba;

import java.util.ArrayList;
import java.util.List;

import com.adc.consts.Constants;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.data.TempData;
import com.adc.shapingba.R;
import com.adc.util.UIUtil;
import com.hik.mcrsdk.MCRSDK;
import com.hik.mcrsdk.rtsp.RtspClient;
import com.hikvision.vmsnetsdk.LineInfo;
import com.hikvision.vmsnetsdk.ServInfo;
import com.hikvision.vmsnetsdk.VMSNetSDK;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

@SuppressWarnings("deprecation")
public class SpotInfoTabActivity extends TabActivity {

	public static TabHost mTabHost;

	private RadioGroup spot_info_radioGroup;
	private RadioButton jcdxx;
	private RadioButton spot_video;
	private RadioButton qxbh;
	//private RadioButton zpzp;

	//public static int spot_idx;

	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spot_info_tab);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SpotInfoTabActivity.this);

		mTabHost = getTabHost();
		final TabWidget tabWidget = mTabHost.getTabWidget();
		// tabWidget.setStripEnabled(false);

		Intent intent = getIntent();
		Bundle dataBundle = intent.getExtras();
		int idx = dataBundle.getInt("idx");
		//spot_idx = idx;
		//dataBundle.putInt("idx", idx);
		//Log.i("heheda", "idx=" + idx);
		
		jcdxx = (RadioButton) findViewById(R.id.jcdxx);
		spot_video = (RadioButton) findViewById(R.id.spot_video);
		qxbh = (RadioButton) findViewById(R.id.qxbh);
		//zpzp = (RadioButton) findViewById(R.id.zpzp);

		//����ü���û����Ƶ��Ϣ
		String camera_id = spotInfos.get(idx).getCamera_id();
		if(camera_id.compareTo("null") == 0){
			spot_video.setVisibility(8);
		}
		
		/*mTabHost.addTab(mTabHost.newTabSpec("TAG1").setIndicator("0")
				.setContent(new Intent(this, SpotDetailsActivity.class)));*/
		mTabHost.addTab(mTabHost.newTabSpec("TAG1").setIndicator("0")
				.setContent(new Intent(this, NewSpotDetailsActivity.class)));
		//�ϰ汾��0.������Ϣ 1.���߱仯 2.ץ����Ƭ
		/*
		mTabHost.addTab(mTabHost.newTabSpec("TAG2").setIndicator("1")
				.setContent(new Intent(this, CurveChangeActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("2")
				.setContent(new Intent(this, CapturePhotoActivity.class)));
		*/
		
		//�°汾�в��ٱ���ץ����Ƭ���ܣ����Ӽ�����Ƶ����
		//�°汾��0.������Ϣ 1.������Ƶ 2.���߱仯
		mTabHost.addTab(mTabHost.newTabSpec("TAG2").setIndicator("1")
				.setContent(new Intent(this, SpotVideoTabActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("TAG3").setIndicator("2")
				.setContent(new Intent(this, CurveChangeActivity.class)));
		mTabHost.setCurrentTab(0);

		final int my_size = 80;
		
		//������Ϣͼ��
		final Drawable jcdxx_3x = getResources().getDrawable(
				R.drawable.jcdxx_3x);
		jcdxx_3x.setBounds(0, 0, my_size, my_size);

		final Drawable jcdxx_3x_press = getResources().getDrawable(
				R.drawable.jcdxx_3x_press);
		jcdxx_3x_press.setBounds(0, 0, my_size, my_size);
		
		//������Ƶͼ��
		final Drawable spot_video_3x = getResources().getDrawable(
				R.drawable.spot_video_3x);
		spot_video_3x.setBounds(0, 0, my_size, my_size);

		final Drawable spot_video_3x_press = getResources().getDrawable(
				R.drawable.spot_video_3x_press);
		spot_video_3x_press.setBounds(0, 0, my_size, my_size);
		
		//���߱仯ͼ��
		final Drawable qxbh_3x = getResources().getDrawable(R.drawable.qxbh_3x);
		qxbh_3x.setBounds(0, 0, my_size, my_size);

		final Drawable qxbh_3x_press = getResources().getDrawable(
				R.drawable.qxbh_3x_press);
		qxbh_3x_press.setBounds(0, 0, my_size, my_size);

		/*
		final Drawable zpzp_3x = getResources().getDrawable(R.drawable.zpzp_3x);
		zpzp_3x.setBounds(0, 0, my_size, my_size);

		final Drawable zpzp_3x_press = getResources().getDrawable(
				R.drawable.zpzp_3x_press);
		zpzp_3x_press.setBounds(0, 0, my_size, my_size);
		*/
		
		// Ĭ����ĵ�һ����ťselected=true
		jcdxx.setCompoundDrawables(null, jcdxx_3x_press, null, null);
		spot_video.setCompoundDrawables(null, spot_video_3x, null, null);
		qxbh.setCompoundDrawables(null, qxbh_3x, null, null);
		//zpzp.setCompoundDrawables(null, zpzp_3x, null, null);

		spot_info_radioGroup = (RadioGroup) findViewById(R.id.spot_info_radio);
		spot_info_radioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (checkedId == jcdxx.getId()) {
							jcdxx.setCompoundDrawables(null, jcdxx_3x_press,null, null);
							spot_video.setCompoundDrawables(null, spot_video_3x, null, null);
							qxbh.setCompoundDrawables(null, qxbh_3x, null, null);
							mTabHost.setCurrentTab(0);
						} else if (checkedId == spot_video.getId()) {
							jcdxx.setCompoundDrawables(null, jcdxx_3x,null, null);
							spot_video.setCompoundDrawables(null, spot_video_3x_press, null, null);
							qxbh.setCompoundDrawables(null, qxbh_3x, null, null);
							mTabHost.setCurrentTab(1);
						} else if (checkedId == qxbh.getId()) {
							jcdxx.setCompoundDrawables(null, jcdxx_3x,null, null);
							spot_video.setCompoundDrawables(null, spot_video_3x, null, null);
							qxbh.setCompoundDrawables(null, qxbh_3x_press, null, null);
							mTabHost.setCurrentTab(2);
						}
					}
				});

	}
	
	
}
