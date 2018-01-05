package com.adc.shapingba;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.data.LoginState;
import com.adc.statistics.Pm10DistributionActivity;
import com.adc.util.ReadStream;
import com.adc.util.UIUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.effect.Effect;
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

public class WuhanYunweiActivity extends Activity {

	private static final int NETWORK_CONNECTED = 100;
	private static final int NETWORK_UNCONNECTED = 101;
	private static final int START_PROGRESS = 102;
	private static final int CANCEL_PROGRESS = 103;
	private static final int LOAD_INFO_SUCCEED = 105;
	private static final int URL_REQUEST_FAIL = 106;
	
	private static final int PIE_GREEN = Color.rgb(0x51, 0xCE, 0xC0);
	private static final int PIE_RED = Color.rgb(0xAA, 0x04, 0x18);
	
	private Button wuhan_yunwei_bt_goback;
	
	private RadioGroup wuhan_yunwei_time_type_group;
	private RadioButton wuhan_yunwei_24hour;
	private RadioButton wuhan_yunwei_month;
	private RadioButton wuhan_yunwei_24hour_all;
	
	private RadioGroup wuhan_yunwei_data_type_group;
	private RadioButton wuhan_yunwei_efficient_rate;
	private RadioButton wuhan_yunwei_online_rate;
	
	private LinearLayout wuhan_statistics_result_layout1;
	private LinearLayout wuhan_statistics_result_layout2;
	private LinearLayout wuhan_statistics_result_layout3;
	
	private String serverURL = LoginState.getIns().getServerURL();
	
	private String data_type = "efficient";//data_type = efficient or online
	private String time_type = "day";//time_type = day or month
	
	/** pieChart 渲染器1 */
	private DefaultRenderer renderer1;
	/** pieChart 渲染器2 */
	private DefaultRenderer renderer2;
	/** pieChart 渲染器3 */
	private DefaultRenderer renderer3;
	/** 蓝丰数据序列 */
	private CategorySeries series1;
	/** 巨正 数据序列 */
	private CategorySeries series2;
	/** 雪迪龙 数据序列 */
	private CategorySeries series3;
	
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
		setContentView(R.layout.activity_wuhan_yunwei);
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					getWuhanYunweiInfo();
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
		
		wuhan_yunwei_bt_goback = (Button) findViewById(R.id.wuhan_yunwei_bt_goback);
		wuhan_yunwei_bt_goback.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		wuhan_yunwei_time_type_group = (RadioGroup) findViewById(R.id.wuhan_yunwei_time_type_group);
		wuhan_yunwei_24hour = (RadioButton) findViewById(R.id.wuhan_yunwei_24hour);
		wuhan_yunwei_month = (RadioButton) findViewById(R.id.wuhan_yunwei_month);
		wuhan_yunwei_24hour_all = (RadioButton) findViewById(R.id.wuhan_yunwei_24hour_all);
		
		wuhan_yunwei_time_type_group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == wuhan_yunwei_24hour_all.getId()){
					return;
				}else if(checkedId == wuhan_yunwei_24hour.getId()){
					time_type = "day";
				}else if(checkedId == wuhan_yunwei_month.getId()){
					time_type = "month";
				}
				checkConnect();
			}
		});
		
		wuhan_yunwei_data_type_group = (RadioGroup) findViewById(R.id.wuhan_yunwei_data_type_group);
		wuhan_yunwei_efficient_rate = (RadioButton) findViewById(R.id.wuhan_yunwei_efficient_rate);
		wuhan_yunwei_online_rate = (RadioButton) findViewById(R.id.wuhan_yunwei_online_rate);
		wuhan_yunwei_data_type_group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == wuhan_yunwei_efficient_rate.getId()){
					data_type = "efficient";
					wuhan_yunwei_24hour.setVisibility(View.VISIBLE);
					wuhan_yunwei_month.setVisibility(View.VISIBLE);
					wuhan_yunwei_24hour_all.setVisibility(View.GONE);
					if(time_type.equals("day")){
						wuhan_yunwei_24hour.setChecked(true);
					}else{
						wuhan_yunwei_month.setChecked(true);
					}
				}else{
					data_type = "online";
					wuhan_yunwei_24hour.setVisibility(View.GONE);
					wuhan_yunwei_month.setVisibility(View.GONE);
					wuhan_yunwei_24hour_all.setVisibility(View.VISIBLE);
				}
				checkConnect();
				//wuhan_yunwei_time_type_group.setVisibility(View.GONE);
			}
		});
		
		wuhan_statistics_result_layout1 = (LinearLayout) findViewById(R.id.wuhan_statistics_result_layout1);
		wuhan_statistics_result_layout2 = (LinearLayout) findViewById(R.id.wuhan_statistics_result_layout2);
		wuhan_statistics_result_layout3 = (LinearLayout) findViewById(R.id.wuhan_statistics_result_layout3);
		
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
	
	/** 查询运维数据 */
	protected void getWuhanYunweiInfo() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROGRESS);
				String query_path;
				
				if(data_type.equals("efficient")){
					//查询数据有效率
					query_path = serverURL+"getDataEfficient?time_type="
							+ time_type
							+ "&name_type=vendor";
				}else{
					//查询设备在线率
					query_path = serverURL+"getEquipmentOnlineRate?time_type="
							+ "day"
							+ "&name_type=vendor";
				}

				Log.i("heheda", query_path);
				String json = null;
				URL url;
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
							
							double rate_a = Double.valueOf(item.getString("rate"));
							Log.i("heheda", "rate_a = "+rate_a);
							double rate_b = 100 - rate_a;
							
							DecimalFormat df = new DecimalFormat("#.00");  
							String rate_a_str = df.format(rate_a);
							String rate_b_str = df.format(rate_b);
							
							if(i == 0){								
								renderer1 = getRenderer(item.getString("name"));
								
								series1 = new CategorySeries(item.getString("name"));
								if(data_type.equals("efficient")){
									series1.add("有效(%)", Double.valueOf(rate_a_str));
									series1.add("无效(%)", Double.valueOf(rate_b_str));
								}else{
									series1.add("在线(%)", Double.valueOf(rate_a_str));
									series1.add("离线(%)", Double.valueOf(rate_b_str));
								}
							}
							
							if(i == 1){								
								renderer2 = getRenderer(item.getString("name"));
								
								series2 = new CategorySeries(item.getString("name"));
								if(data_type.equals("efficient")){
									series2.add("有效(%)", Double.valueOf(rate_a_str));
									series2.add("无效(%)", Double.valueOf(rate_b_str));
								}else{
									series2.add("在线(%)", Double.valueOf(rate_a_str));
									series2.add("离线(%)", Double.valueOf(rate_b_str));
								}
							}
							
							if(i == 2){								
								renderer3 = getRenderer(item.getString("name"));
								
								series3 = new CategorySeries(item.getString("name"));
								if(data_type.equals("efficient")){
									series3.add("有效(%)", Double.valueOf(rate_a_str));
									series3.add("无效(%)", Double.valueOf(rate_b_str));
								}else{
									series3.add("在线(%)", Double.valueOf(rate_a_str));
									series3.add("离线(%)", Double.valueOf(rate_b_str));
								}
							}
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
	
	protected DefaultRenderer getRenderer(String name) {
		DefaultRenderer renderer = new DefaultRenderer();
		renderer.setDisplayValues(true);
		renderer.setChartTitle(name);
		renderer.setLabelsTextSize(50);
		renderer.setLegendTextSize(50);
		renderer.setLabelsColor(Color.BLACK);
		renderer.setPanEnabled(false);
		renderer.setChartTitleTextSize(50);
		renderer.setZoomEnabled(false);
		renderer.setShowLegend(false);
		SimpleSeriesRenderer r1 = new SimpleSeriesRenderer();
		if(name.contains("蓝丰")){
			r1.setColor(Color.rgb(0x51, 0xCE, 0xC0));
		}else if (name.contains("巨正")) {
			r1.setColor(Color.rgb(0x56, 0xAF, 0xDC));
		}else if (name.contains("雪迪龙")){
			r1.setColor(Color.rgb(0xAD, 0xDC, 0x87));
		}else{
			r1.setColor(PIE_GREEN);
		}
		
		renderer.addSeriesRenderer(r1);
		SimpleSeriesRenderer r2 = new SimpleSeriesRenderer();
		r2.setColor(Color.GRAY);
		renderer.addSeriesRenderer(r2);
		return renderer;
	}
	
	protected void showResult() {
		//蓝丰
		if(series1 != null){
			GraphicalView pie1 = ChartFactory.getPieChartView(
					WuhanYunweiActivity.this, series1, renderer1);
			wuhan_statistics_result_layout1.removeAllViews();
			wuhan_statistics_result_layout1.addView(pie1,new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		}
		
		//雪迪龙
		if(series2 != null){
			GraphicalView pie2 = ChartFactory.getPieChartView(
				WuhanYunweiActivity.this, series2, renderer2);
			wuhan_statistics_result_layout2.removeAllViews();
			wuhan_statistics_result_layout2.addView(pie2, new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
		//巨正
		if(series3 != null){
			GraphicalView pie3 = ChartFactory.getPieChartView(
					WuhanYunweiActivity.this, series3, renderer3);
			wuhan_statistics_result_layout3.removeAllViews();
			wuhan_statistics_result_layout3.addView(pie3, new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
		
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
		Toast toast = Toast.makeText(WuhanYunweiActivity.this,
				"网络错误", Toast.LENGTH_SHORT);
		toast.show();
	}
	
	private void showServerError() {
		Toast toast = Toast.makeText(WuhanYunweiActivity.this,
				"网络错误", Toast.LENGTH_SHORT);
		toast.show();
	}
}
