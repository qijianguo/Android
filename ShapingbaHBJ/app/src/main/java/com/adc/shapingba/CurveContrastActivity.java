package com.adc.shapingba;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.shapingba.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

public class CurveContrastActivity extends Activity {

	private RadioGroup curve_contrast_radioGroup;
	private RadioButton realtime_dataButton;
	private RadioButton hour_dataButton;
	private Button qxdb_goback;
	private int noise_or_pm10;
	private int realtime_or_hour;
	private final int noise_checked = 0;
	private final int pm10_checked = 1;
	private final int realtime_checked = 0;
	private final int hour_checked = 1;

	private LinearLayout curve_contrast_layout;

	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	
	class SpotData implements Comparable {
		String spot_name;
		double realtime_noise;
		double hour_noise;
		double realtime_pm10;
		double hour_pm10;

		public SpotData(String spot_name, double realtime_noise,
				double hour_noise, double realtime_pm10, double hour_pm10) {
			// TODO Auto-generated constructor stub
			this.spot_name = spot_name;
			this.realtime_noise = realtime_noise;
			this.hour_noise = hour_noise;
			this.realtime_pm10 = realtime_pm10;
			this.hour_pm10 = hour_pm10;
		}

		@Override
		public int compareTo(Object another) {
			// TODO Auto-generated method stub
			SpotData o = (SpotData) another;
			return realtime_noise > o.realtime_noise ? 1
					: (realtime_noise == o.realtime_noise ? 0 : -1);
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			//MainTabActivity.hxdb_tjfx_setup = 0;
			//Intent intent = new Intent(CurveContrastActivity.this,MainTabActivity.class);
			Intent intent = new Intent(CurveContrastActivity.this,WuhanMainActivity.class);
			startActivity(intent);			
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_curve_contrast);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(CurveContrastActivity.this);

		curve_contrast_radioGroup = (RadioGroup) findViewById(R.id.curve_contrast_radio);
		realtime_dataButton = (RadioButton) findViewById(R.id.curve_contrast_realtime_data);
		hour_dataButton = (RadioButton) findViewById(R.id.curve_contrast_hour_data);
		qxdb_goback = (Button) findViewById(R.id.qxdb_goback);

		qxdb_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CurveContrastActivity.this,WuhanMainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		noise_or_pm10 = LoginState.getIns().getNoise_or_pm10();

		curve_contrast_layout = (LinearLayout) findViewById(R.id.curve_contrast_layout);

		realtime_or_hour = realtime_checked;

		curve_contrast_layout.addView(xychar(noise_or_pm10, realtime_or_hour),
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));

		curve_contrast_radioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						if (checkedId == realtime_dataButton.getId()) {
							realtime_or_hour = realtime_checked;
							curve_contrast_layout.removeAllViews();
							curve_contrast_layout.addView(
									xychar(noise_or_pm10, realtime_or_hour),
									new RelativeLayout.LayoutParams(
											LayoutParams.MATCH_PARENT,
											LayoutParams.MATCH_PARENT));
						} else if (checkedId == hour_dataButton.getId()) {
							realtime_or_hour = hour_checked;
							curve_contrast_layout.removeAllViews();
							curve_contrast_layout.addView(
									xychar(noise_or_pm10, realtime_or_hour),
									new RelativeLayout.LayoutParams(
											LayoutParams.MATCH_PARENT,
											LayoutParams.MATCH_PARENT));
						}
					}
				});
	}

	public GraphicalView xychar(int noise_or_pm10, int realtime_or_hour) {
		// 多个渲染
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		// 横向排列,实在是太丑了。�??
		// renderer.setOrientation(Orientation.VERTICAL);
		// 多个序列的数据集
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		// 构建数据集以及渲�???
		int spot_num = spotInfos.size();
		List<SpotData> list = new ArrayList<CurveContrastActivity.SpotData>();
		String dataname[] = new String[] { "实时噪声db(A)", "小时噪声db(A)",
				"实时PM10(μg/m³)", "小时PM10(μg/m³)" };
		
		double max_y = 0;// y轴的�???大�??
		for (int i = 0; i < 1; i++) {
			// 柱状图内容名�???
			XYSeries series = new XYSeries(dataname[noise_or_pm10 * 2
					+ realtime_or_hour]);

			for (int j = 0; j < spot_num; j++) {
				// series.add(j + 1,
				// Double.valueOf(LoginActivity.SpotInfoList.get(j).getRealtime_noise()));
				double realtime_noise = Double
						.valueOf(spotInfos.get(j)
								.getRealtime_noise());
				double hour_noise = Double.valueOf(SpotInfoListInstance.getIns().getList()
						.get(j).getHour_noise());
				double realtime_pm10 = Double
						.valueOf(spotInfos.get(j)
								.getReltime_pm_10());
				double hour_pm10 = Double.valueOf(SpotInfoListInstance.getIns().getList()
						.get(j).getHour_pm10());
				String csite_nameString = spotInfos.get(j)
						.getCsite_name();
				if (csite_nameString.length() >= 6)
					csite_nameString = csite_nameString.substring(0, 5);
				SpotData data = new SpotData(csite_nameString, realtime_noise,
						hour_noise, realtime_pm10, hour_pm10);
				list.add(data);
			}

			if (noise_or_pm10 == noise_checked
					&& realtime_or_hour == realtime_checked) {
				Collections.sort(list, new Comparator<SpotData>() {
					@Override
					public int compare(SpotData lhs, SpotData rhs) {
						// TODO Auto-generated method stub
						return lhs.realtime_noise > rhs.realtime_noise ? -1
								: (lhs.realtime_noise == rhs.realtime_noise ? 0
										: 1);
					}
				});
			} else if (noise_or_pm10 == pm10_checked
					&& realtime_or_hour == realtime_checked) {
				Collections.sort(list, new Comparator<SpotData>() {
					@Override
					public int compare(SpotData lhs, SpotData rhs) {
						// TODO Auto-generated method stub
						return lhs.realtime_pm10 > rhs.realtime_pm10 ? -1
								: (lhs.realtime_pm10 == rhs.realtime_pm10 ? 0
										: 1);
					}
				});
			} else if (noise_or_pm10 == noise_checked
					&& realtime_or_hour == hour_checked) {
				Collections.sort(list, new Comparator<SpotData>() {
					@Override
					public int compare(SpotData lhs, SpotData rhs) {
						// TODO Auto-generated method stub
						return lhs.hour_noise > rhs.hour_noise ? -1
								: (lhs.hour_noise == rhs.hour_noise ? 0 : 1);
					}
				});
			} else {
				Collections.sort(list, new Comparator<SpotData>() {
					@Override
					public int compare(SpotData lhs, SpotData rhs) {
						// TODO Auto-generated method stub
						return lhs.hour_pm10 > rhs.hour_pm10 ? -1
								: (lhs.hour_pm10 == rhs.hour_pm10 ? 0 : 1);
					}
				});
			}

			for (int j = 0; j < spot_num; j++) {
				if (realtime_or_hour == realtime_checked) {
					if (noise_or_pm10 == noise_checked) {
						if (list.get(j).realtime_noise == 0)
							break;
						if (j == 0)
							max_y = list.get(j).realtime_noise;
						series.add(j + 0.6, (int)list.get(j).realtime_noise);
					}
					if (noise_or_pm10 == pm10_checked) {
						if (list.get(j).realtime_pm10 == 0)
							break;
						if (j == 0)
							max_y = list.get(j).realtime_pm10;
						series.add(j + 0.6, (int)list.get(j).realtime_pm10);
					}
				} else {
					if (noise_or_pm10 == noise_checked) {
						if (list.get(j).hour_noise == 0)
							break;
						if (j == 0)
							max_y = list.get(j).hour_noise;
						series.add(j + 0.6, (int)list.get(j).hour_noise);
					}
					if (noise_or_pm10 == pm10_checked) {
						if (list.get(j).hour_pm10 == 0)
							break;
						if (j == 0)
							max_y = list.get(j).hour_pm10;
						series.add(j + 0.6, (int)list.get(j).hour_pm10);
					}
				}
				renderer.addXTextLabel(j + 0.6, list.get(j).spot_name);
			}
			dataset.addSeries(series);
			XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
			// 设置颜色
			xyRenderer.setColor(Color.rgb(100, 181, 234));
			// 设置点的样式
			// xyRenderer.setPointStyle(PointStyle.);
			// 将要绘制的点添加到坐标绘制中
			renderer.addSeriesRenderer(xyRenderer);
		}
		
		/* 想加�???条直线来�???，然而加不上去�?��??
		 * XYSeries line = new XYSeries("");
		line.add(-50, 100);
		line.add(50, 100);
		dataset.addSeries(line);
		XYSeriesRenderer lineRenderer = new XYSeriesRenderer();
		lineRenderer.setColor(Color.RED);
		lineRenderer.setPointStyle(PointStyle.POINT);
		lineRenderer.setLineWidth(3);
		
		renderer.addSeriesRenderer(lineRenderer);
		*/
		
		// 设置x轴标签数
		renderer.setXLabels(0);
		// 设置Y轴标签数
		// renderer.setYLabels(1);
		// 设置x轴的�???大�??
		// renderer.setXAxisMax(spot_num+2);
		// 设置轴的颜色
		renderer.setAxesColor(Color.BLACK);
		// 设置x轴和y轴的标签对齐方式
		renderer.setXLabelsAlign(Align.LEFT);
		renderer.setYLabelsAlign(Align.CENTER);

		// renderer.setXLabelsPadding(-50);
		// 设置现实网格
		renderer.setShowGrid(true);

		renderer.setShowAxes(true);
		// 设置条形图之间的距离
		renderer.setBarSpacing(0.2);
		// 设置柱子宽度
		// renderer.setBarWidth(5.0f);

		renderer.setInScroll(false);

		renderer.setClickEnabled(false);
		// 设置x轴和y轴标签的颜色
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);

		int length = renderer.getSeriesRendererCount();
		// 设置图标的标�???
		// renderer.setChartTitle("jianceshuju");
		// renderer.setLabelsColor(Color.BLACK);

		// 设置图例的字体大�???
		renderer.setLegendTextSize(30);
		// 设置x轴标记字体大�???
		renderer.setLabelsTextSize(30);
		// 设置x轴标记旋转角�???
		renderer.setXLabelsAngle(45f);
		// 设置允许左右拖动,但不允许上下拖动
		renderer.setPanEnabled(true, false);
		// 设置不允许缩�???
		renderer.setZoomEnabled(false);

		// 设置x轴和y轴的�???大最小�??
		renderer.setRange(new double[] { 0, 10, 0, max_y + 10 });
		renderer.setMarginsColor(0x00888888);
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer ssr = renderer.getSeriesRendererAt(i);
			//20160801 导入achart-engine-1.2 方法失效
			/*ssr.setChartValuesTextAlign(Align.CENTER);
			ssr.setChartValuesTextSize(30);
			ssr.setDisplayChartValues(true);*/
		}
		GraphicalView mChartView = ChartFactory.getBarChartView(
				getApplicationContext(), dataset, renderer, Type.STACKED);

		return mChartView;

	}

}
