package com.adc.air;

import com.adc.hbj5.R;
import com.adc.hbj5.MyActivityManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AirConditionTestingStationDetailsActivity extends Activity {

	private LinearLayout air_check_layout;
	private TextView station_name;
	private TextView aqi_number;
	private TextView aqi_level;
	private TextView aqi_source;
	private TextView health_guide;
	private TextView recommended_measure;
	private TextView air_check_time;

	private String[] aqi_levelString = { "优", "良", "轻度污染", "中度污染", "重度污染",
			"严重污染" };

	private String[] guideString = { "空气质量令人满意，基本无空气污染.",
			"空气质量可接受，但某些污染物可能对极少数异常敏感人群健康有较弱影响.", "易感人群症状有轻度加剧，健康人群出现刺激症状.",
			"进一步加剧易感人群症状，可能对健康人群心脏、呼吸系统有影响.",
			"心脏病和肺病患者症状显著加剧，运动耐受力降低，健康人群普遍出现症状.",
			"健康人群运动耐受力降低，有明显强烈症状，提前出现某些疾病." };

	private String[] measureString = { "各类人群可正常活动.", "建议极少数异常敏感人群应减少户外活动.",
			"建议儿童、老年人及心脏病、呼吸系统疾病患者应减少长时间、高强度的户外锻炼.",
			"建议疾病患者避免长时间、高强度的户外锻练，一般人群适量减少户外运动.",
			"建议儿童、老年人和心脏病、肺病患者应停留在室内，停止户外运动，一般人群减少户外运动.",
			"建议儿童、老年人和病人应当留在室内，避免体力消耗，一般人群应避免户外活动." };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station_details);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(AirConditionTestingStationDetailsActivity.this);

		station_name = (TextView) findViewById(R.id.station_name);
		aqi_number = (TextView) findViewById(R.id.aqi_number);
		aqi_level = (TextView) findViewById(R.id.aqi_level);
		aqi_source = (TextView) findViewById(R.id.aqi_source);
		health_guide = (TextView) findViewById(R.id.health_guide);
		recommended_measure = (TextView) findViewById(R.id.recommended_measure);
		air_check_time = (TextView) findViewById(R.id.air_check_time);

		Intent intent = getIntent();
		Bundle dataBundle = intent.getExtras();
		int idx = dataBundle.getInt("idx");
		double aqi = Double.valueOf(KqzlActivity.station_list.get(idx)
				.getAir_index());

		station_name.setText(KqzlActivity.station_list.get(idx)
				.getStation_name());
		aqi_number.setText(KqzlActivity.station_list.get(idx).getAir_index());
		aqi_level.setText(aqi_levelString[get_index(aqi)]);
		aqi_source.setText(KqzlActivity.station_list.get(idx).getSource());
		health_guide.setText(guideString[get_index(aqi)]);
		recommended_measure.setText(measureString[get_index(aqi)]);
		air_check_time.setText(KqzlActivity.station_list.get(idx).getTime());

		air_check_layout = (LinearLayout) findViewById(R.id.air_check_layout);
		air_check_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	private int get_index(double aqi) {
		if (aqi <= 50)
			return 0;
		if (aqi <= 100)
			return 1;
		if (aqi <= 150)
			return 2;
		if (aqi <= 200)
			return 3;
		if (aqi <= 300)
			return 4;
		return 5;
	}
}
