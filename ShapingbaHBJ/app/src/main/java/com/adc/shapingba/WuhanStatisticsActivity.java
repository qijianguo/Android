package com.adc.shapingba;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.data.LoginState;
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
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class WuhanStatisticsActivity extends Activity {

	private static final int NETWORK_CONNECTED = 100;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int START_PROGRESS = 102;
	private static final int CANCEL_PROGRESS = 103;
	private static final int LOAD_INFO_SUCCEED = 105;
	private static final int URL_REQUEST_FAIL = 106;
	
	private Button wuhan_statistics_bt_goback;
	
	private RadioGroup wuhan_statistics_group;
	private RadioButton wuhan_statistics_24hour;
	private RadioButton wuhan_statistics_month;
	
	private LinearLayout wuhan_statistics_result_layout;
	
	private String serverURL = LoginState.getIns().getServerURL();
	private String time_type = "day";//time_type = day or month
	
	private ArrayList<String> name_list;
	private ArrayList<String> value_list;
	private Handler handler = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			return false;
		}
		return false;
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wuhan_statistics);
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					getWuhanStatisticInfo();
					break;
				case NETWORK_UNCONNECTED:
					showNetworkError();
					break;
				case START_PROGRESS:
					showLoadingProgress();
					break;
				case CANCEL_PROGRESS:
					cancelProgress();
					break;
				case URL_REQUEST_FAIL:
					showServerError();
					break;
				case LOAD_INFO_SUCCEED:
					showResult();
					break;
				default:
					break;
				}
			}
		};
		
		wuhan_statistics_bt_goback = (Button) findViewById(R.id.wuhan_statistics_bt_goback);
		wuhan_statistics_bt_goback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		wuhan_statistics_result_layout = (LinearLayout) findViewById(R.id.wuhan_statistics_result_layout);
		
		wuhan_statistics_group = (RadioGroup) findViewById(R.id.wuhan_statistics_group);
		wuhan_statistics_24hour = (RadioButton) findViewById(R.id.wuhan_statistics_24hour);
		wuhan_statistics_month = (RadioButton) findViewById(R.id.wuhan_statistics_month);
		
		wuhan_statistics_group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == wuhan_statistics_24hour.getId()){
					time_type = "day";
				}else if(checkedId == wuhan_statistics_month.getId()){
					time_type = "month";
				}
				checkConnect();
			}
		});
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
	
	/** 查询PM10浓度超标率数据 */
	protected void getWuhanStatisticInfo() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROGRESS);
				String query_path = serverURL+"getDataExrate?time_type="
						+ time_type
						+ "&name_type=csite";

				Log.i("heheda", query_path);
				String json = null;
				URL url;
				name_list = new ArrayList<String>();
				value_list = new ArrayList<String>();
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

						JSONArray jsonArray = new JSONArray(json);
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							name_list.add(item.getString("name"));
							value_list.add(item.getString("rate"));
						}

						handler.sendEmptyMessage(LOAD_INFO_SUCCEED);
					} else {
						handler.sendEmptyMessage(URL_REQUEST_FAIL);
						handler.sendEmptyMessage(CANCEL_PROGRESS);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					handler.sendEmptyMessage(NETWORK_UNCONNECTED);
					handler.sendEmptyMessage(CANCEL_PROGRESS);
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public GraphicalView getBarChart(List<String> name, List<String> value) {
		// 多个渲染
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		// 横向排列,实在是太丑了。。
		//renderer.setOrientation(Orientation.VERTICAL);
		// 多个序列的数据集
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		// 构建数据集以及渲染

		double max_y = 0;// y轴的最大值
		for (int i = 0; i < 1; i++) {
			// 柱状图内容名称
			XYSeries series = new XYSeries("");

			for (int j = 0; j < value.size(); j++) {
				Log.i("heheda", "value=" + value.get(j));
				series.add(j + 0.8, Double.valueOf(value.get(j)));
				if (Double.valueOf(value.get(j)) > max_y)
					max_y = Double.valueOf(value.get(j));
				renderer.addXTextLabel(j + 0.8, name.get(j));
			}
			dataset.addSeries(series);
			XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
			// 设置颜色
			xyRenderer.setColor(Color.rgb(0x34,0xB6,0x67));
			//xyRenderer.setChartValuesTextAlign(Align.RIGHT);
			xyRenderer.setChartValuesTextSize(30);
			xyRenderer.setDisplayChartValues(true);
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
		renderer.setBarSpacing(0.5);
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
		renderer.setRange(new double[] { 0, 10, 0, max_y + 10 });
		renderer.setMarginsColor(0x00888888);
		renderer.setDisplayValues(true);

		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer ssr = renderer.getSeriesRendererAt(i);
			//20160801 导入achart-engine-1.2 方法失效
			//ssr.setChartValuesTextAlign(Align.CENTER);
			//ssr.setChartValuesTextSize(30);
			//ssr.setDisplayChartValues(true);
		}
		GraphicalView mChartView = ChartFactory.getBarChartView(getApplicationContext(), dataset, renderer, Type.DEFAULT);

		return mChartView;

	}
	
	protected void showResult() {
		wuhan_statistics_result_layout.removeAllViews();
		wuhan_statistics_result_layout.addView(
				getBarChart(name_list, value_list),
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
		handler.sendEmptyMessage(CANCEL_PROGRESS);
	}
	
	/**
	 * 数据加载中
	 */
	private void showLoadingProgress() {
		UIUtil.showProgressDialog(this, "数据加载中");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}
	
	private void showNetworkError() {
		Toast toast = Toast.makeText(WuhanStatisticsActivity.this,
				"网络错误", Toast.LENGTH_SHORT);
		toast.show();
	}
	
	private void showServerError() {
		Toast toast = Toast.makeText(WuhanStatisticsActivity.this,
				"网络错误", Toast.LENGTH_SHORT);
		toast.show();
	}
}
