package com.adc.surfacewater;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.hbj5.MyActivityManager;
import com.adc.hbj5.R;
import com.adc.hbj5.R.layout;
import com.adc.statistics.Pm10ConcentrationRankActivity;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SurfaceWaterCurveContrastActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int QUERY_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 106;
	
	private Handler handler = null;
	
	private Button dbsqxdb_goback;
	
	private LinearLayout dbsqxdb_rank_layout;
	
	private RadioGroup surface_water_curve_contrast_radio_group;
	private RadioButton[] surface_water_curve_contrast_buttons;
	private final int itemSize = 6;
	private int checkedIdx;
	
	private List<String> keyList;
	private List<String> valueList;
	
 	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		finish();
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surface_water_curve_contrast);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SurfaceWaterCurveContrastActivity.this);
		
		dbsqxdb_goback = (Button) findViewById(R.id.dbsqxdb_goback);
		dbsqxdb_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		dbsqxdb_rank_layout = (LinearLayout) findViewById(R.id.dbsqxdb_rank_layout);
		
		surface_water_curve_contrast_radio_group = (RadioGroup) findViewById(R.id.surface_water_curve_contrast_radio_group);
		
		surface_water_curve_contrast_buttons = new RadioButton[itemSize];
		surface_water_curve_contrast_buttons[0] = (RadioButton) findViewById(R.id.surface_water_curve_contrast1);
		surface_water_curve_contrast_buttons[1] = (RadioButton) findViewById(R.id.surface_water_curve_contrast2);
		surface_water_curve_contrast_buttons[2] = (RadioButton) findViewById(R.id.surface_water_curve_contrast3);
		surface_water_curve_contrast_buttons[3] = (RadioButton) findViewById(R.id.surface_water_curve_contrast4);
		surface_water_curve_contrast_buttons[4] = (RadioButton) findViewById(R.id.surface_water_curve_contrast5);
		surface_water_curve_contrast_buttons[5] = (RadioButton) findViewById(R.id.surface_water_curve_contrast6);
		
		checkedIdx = 0;
		surface_water_curve_contrast_radio_group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				for(int i = 0;i < itemSize;i++){
					if (checkedId == surface_water_curve_contrast_buttons[i].getId()) {
						checkedIdx = i;
						break;
					}
				}
				checkConnect();
			}
		});
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					showQueryProgress();
					query();
					break;
				case NETWORK_UNCONNECTED:
					showNetworkError();
					break;
				case START_PROCESS:
					showQueryProgress();
					break;
				case CANCEL_PROCESS:
					cancelProgress();
					break;
				case QUERY_SUCCEED:
					showResult();
					cancelProgress();
					break;
				case URL_REQUEST_FAIL:
					
					break;
				default:
					break;
				}
			}
		};
		checkConnect();
	}
	
	protected void checkConnect() {
		new Thread() {
			@Override
			public void run() {
				ConnectivityManager con = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				boolean wifi = con
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
						.isConnectedOrConnecting();
				boolean internet = con.getNetworkInfo(
						ConnectivityManager.TYPE_MOBILE)
						.isConnectedOrConnecting();
				if (wifi | internet) {
					handler.sendEmptyMessage(NETWORK_CONNECTED);
				} else {
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
				}
			}
		}.start();
	}

	protected void query() {
		new Thread() {
			@Override
			public void run() {
				String json = null;
				String query_path = LoginState.getIns().getServerURL()+"jingzhou/water/junzhiduibi?userId="
						+ LoginState.getIns().getUserId()
						+ "&type="
						+ (checkedIdx+1);
				Log.i("heheda", query_path);
				URL url;

				keyList = new ArrayList<String>();
				valueList = new ArrayList<String>();
				try {
					url = new URL(query_path);
					// Log.i("heheda",get_spot_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(50 * 1000);
					conn.setRequestMethod("GET");
					int responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = ReadStream.readStream(is);
						json = new String(data);
						JSONObject jsonObject = new JSONObject(json);
						String jsonArrayString = jsonObject.getString("data");
						JSONArray jsonArray = new JSONArray(jsonArrayString);	
						
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							String csite_name = item.getString("name");
							keyList.add(csite_name);
							String val = item.getString("value");
							valueList.add(val);
						}
						handler.sendEmptyMessage(QUERY_SUCCEED);
					} else {
						handler.sendEmptyMessage(URL_REQUEST_FAIL);
						handler.sendEmptyMessage(CANCEL_PROCESS);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
					handler.sendEmptyMessage(CANCEL_PROCESS);
					e.printStackTrace();
				}
			}
		}.start();
	}

	public GraphicalView getBarChart(List<String> name, List<String> value,
			String lengend_name) {
		// 多个渲染
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		// 横向排列,实在是太丑了。。
		// renderer.setOrientation(Orientation.VERTICAL);
		// 多个序列的数据集
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		// 构建数据集以及渲染

		double max_y = 0;// y轴的最大值
		for (int i = 0; i < 1; i++) {
			// 柱状图内容名称
			XYSeries series = new XYSeries(lengend_name);

			for (int j = 0; j < value.size(); j++) {
				Log.i("heheda", "value=" + value.get(j));
				series.add(j + 0.6, Double.valueOf(value.get(j)));
				if (Double.valueOf(value.get(j)) > max_y)
					max_y = Double.valueOf(value.get(j));
				renderer.addXTextLabel(j + 0.6, name.get(j));
			}
			dataset.addSeries(series);
			XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
			xyRenderer.setChartValuesTextSize(30);
			xyRenderer.setDisplayChartValues(true);
			// 设置颜色
			xyRenderer.setColor(Color.rgb(100, 181, 234));
			// 设置点的样式
			// xyRenderer.setPointStyle(PointStyle.);
			// 将要绘制的点添加到坐标绘制中
			renderer.addSeriesRenderer(xyRenderer);
		}
		// 设置x轴标签数
		renderer.setXLabels(0);
		// 设置Y轴标签数
		// renderer.setYLabels(1);
		// 设置x轴的最大值
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
		// 设置图标的标题
		// renderer.setChartTitle("jianceshuju");
		// renderer.setLabelsColor(Color.BLACK);

		// 设置图例的字体大小
		renderer.setLegendTextSize(30);
		// 设置x轴标记字体大小
		renderer.setLabelsTextSize(30);
		// 设置x轴标记旋转角度
		renderer.setXLabelsAngle(45f);
		// 设置允许左右拖动,但不允许上下拖动
		renderer.setPanEnabled(true, false);
		// 设置不允许缩放
		renderer.setZoomEnabled(false);

		// 设置x轴和y轴的最大最小值
		renderer.setRange(new double[] { 0, 10, 0, max_y*1.1 });
		renderer.setMarginsColor(0x00888888);
		renderer.setDisplayValues(true);
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer ssr = renderer.getSeriesRendererAt(i);
			/*ssr.setChartValuesTextAlign(Align.CENTER);
			ssr.setChartValuesTextSize(30);
			ssr.setDisplayChartValues(true);*/
		}
		GraphicalView mChartView = ChartFactory.getBarChartView(
				getApplicationContext(), dataset, renderer, Type.STACKED);

		return mChartView;

	}
	
	protected void showResult() {
		dbsqxdb_rank_layout.removeAllViews();
		dbsqxdb_rank_layout.addView(
				getBarChart(keyList,
						valueList, ""),
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
	}

	protected void showNetworkError() {
		Toast toast = Toast.makeText(SurfaceWaterCurveContrastActivity.this, "网络异常",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(SurfaceWaterCurveContrastActivity.this, "服务器维护中，请稍后再试",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 查询进度条
	 */
	private void showQueryProgress() {
		UIUtil.showProgressDialog(this, "查询中......");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
}
