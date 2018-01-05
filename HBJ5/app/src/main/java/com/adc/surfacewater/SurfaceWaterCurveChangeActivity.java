package com.adc.surfacewater;

import com.adc.hbj5.R;
import com.adc.statistics.Pm10MonthlyReportActivity;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.hbj5.MainActivity;
import com.adc.hbj5.MyActivityManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SurfaceWaterCurveChangeActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int QUERY_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 106;
	
	private Handler handler = null;

	private Button dbsqxbh_goback;
	
	private LinearLayout dbs_curve1_layout;
	private LinearLayout dbs_curve2_layout;
	private LinearLayout dbs_curve3_layout;
	private LinearLayout dbs_curve4_layout;

	/** Chart 渲染器 */
	private DefaultRenderer renderer1;
	private DefaultRenderer renderer2;
	private DefaultRenderer renderer3;
	private DefaultRenderer renderer4;
	/** pm2.5数据序列 */
	private CategorySeries series1;
	private CategorySeries series2;
	private CategorySeries series3;
	private CategorySeries series4;
	
	private List<String> keyList1;
	private List<String> keyList2;
	private List<String> keyList3;
	private List<String> keyList4;
	
	private List<String> valueList1;
	private List<String> valueList2;
	private List<String> valueList3;
	private List<String> valueList4;
	
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
		setContentView(R.layout.activity_surface_water_curve_change);
		
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SurfaceWaterCurveChangeActivity.this);
		
		dbsqxbh_goback = (Button) findViewById(R.id.dbsqxbh_goback);
		dbsqxbh_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		dbs_curve1_layout = (LinearLayout) findViewById(R.id.dbs_curve1_layout);
		dbs_curve2_layout = (LinearLayout) findViewById(R.id.dbs_curve2_layout);
		dbs_curve3_layout = (LinearLayout) findViewById(R.id.dbs_curve3_layout);
		dbs_curve4_layout = (LinearLayout) findViewById(R.id.dbs_curve4_layout);
				
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					showQueryProgress();
					querySurfaceWaterCurve();
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
					showServerError();
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
	
	protected void querySurfaceWaterCurve() {
		new Thread() {
			@Override
			public void run() {
				String json = null;
				String query_path = LoginState.getIns().getServerURL()+"jingzhou/water/lishishuju";
				URL url;

				valueList1 = new ArrayList<String>();
				valueList2 = new ArrayList<String>();
				valueList3 = new ArrayList<String>();
				valueList4 = new ArrayList<String>();
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
						if (jsonArray.length() == 0) {
							return;
						}
						JSONObject dataObj = jsonArray.getJSONObject(0);
						//N
						JSONArray danArray = dataObj.getJSONArray("dan");
						for (int i = 0; i < danArray.length(); i++) {
							String dan = danArray.getString(i);
							valueList1.add(dan);
						}
						//P
						JSONArray linArray = dataObj.getJSONArray("lin");
						for (int i = 0; i < linArray.length(); i++) {
							String lin = linArray.getString(i);
							valueList2.add(lin);
						}
						//MnO4-
						JSONArray MnO4Array = dataObj.getJSONArray("gaomensuanyan");
						for (int i = 0; i < MnO4Array.length(); i++) {
							String MnO4 = MnO4Array.getString(i);
							valueList3.add(MnO4);
						}
						//NP
						JSONArray npArray = dataObj.getJSONArray("andan");
						for (int i = 0; i < npArray.length(); i++) {
							String np = npArray.getString(i);
							valueList4.add(np);
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

	public GraphicalView getCubeLineChart(List<String> valueList, String legend) {
		
		double minVal = Double.MAX_VALUE;
		double maxVal = Double.MIN_VALUE;
		
		for(int i = 0;i < valueList.size();i++){
			double value = Double.valueOf(valueList.get(i));
			if (value > maxVal) {
				maxVal = value;
			}
			if (value < minVal) {
				minVal = value;
			}
		}
		// 多个渲染
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		// 横向排列
		//renderer.setOrientation(Orientation.VERTICAL);
		renderer.setApplyBackgroundColor(true);
		renderer.setMarginsColor(Color.WHITE);
		renderer.setBackgroundColor(Color.WHITE);
		renderer.setPointSize(8f);
		renderer.setRange(new double[] { 0, 25, minVal-10 < 0 ? 0 : minVal-10, maxVal*1.2 });
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);
		renderer.setAxesColor(Color.BLACK);
		renderer.setXLabels(0);
		// 设置图例的字体大�??
		renderer.setLegendTextSize(30);
		//设置x轴标记字体大�??
		renderer.setLabelsTextSize(30);
		renderer.setPanEnabled(true, false);
		renderer.setXAxisMin(-1);//设置x轴起点
		// 多个序列的数据集
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		
		XYSeries series = new XYSeries(legend);
		
		Time t = new Time();
		t.setToNow();
		int h = t.hour;
		
		for(int i = 0;i < valueList.size();i++){
			series.add(i*3, Double.valueOf(valueList.get(i)));
			renderer.addXTextLabel(i*3, ""+(h+i)%24);
		}
		
		dataset.addSeries(series);
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.rgb(71, 167, 230));
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillPoints(true);
		r.setDisplayChartValues(true);
		r.setFillBelowLine(true);
		r.setFillBelowLineColor(Color.parseColor("#6687CEFA"));
		r.setChartValuesSpacing(30);
		r.setChartValuesTextSize(30);
		r.setLineWidth(6);
		renderer.addSeriesRenderer(r);
		renderer.setDisplayValues(true);
		GraphicalView mChartView = ChartFactory.getCubeLineChartView(
				getApplicationContext(), dataset, renderer,0.5f);
		return mChartView;

	}
	
	protected void showResult() {
		dbs_curve1_layout.removeAllViews();
		dbs_curve2_layout.removeAllViews();
		dbs_curve3_layout.removeAllViews();
		dbs_curve4_layout.removeAllViews();
		dbs_curve1_layout.addView(
				getCubeLineChart(valueList1, "氮"),
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
		dbs_curve2_layout.addView(
				getCubeLineChart(valueList2, "磷"),
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
		dbs_curve3_layout.addView(
				getCubeLineChart(valueList3, "高锰酸盐"),
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
		dbs_curve4_layout.addView(
				getCubeLineChart(valueList4, "氨氮"),
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
	}

	protected void showNetworkError() {
		Toast toast = Toast.makeText(SurfaceWaterCurveChangeActivity.this, "网络异常",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void showServerError() {
		Toast toast = Toast.makeText(SurfaceWaterCurveChangeActivity.this, "服务器维护中，请稍后再试",
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
