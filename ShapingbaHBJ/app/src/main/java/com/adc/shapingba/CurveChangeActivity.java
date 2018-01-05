package com.adc.shapingba;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.shapingba.R;
import com.adc.shapingba.CurveContrastActivity.SpotData;
import com.adc.util.UIUtil;
import com.amap.api.maps2d.model.Text;

import android.R.integer;
import android.R.menu;
import android.annotation.SuppressLint;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class CurveChangeActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int GET_INFO_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int URL_REQUEST_FAIL = 106;
	
	private TextView curve_change_tv_chart_title;
	private Handler handler = null;
	private Button curve_change_goback; 
	private LinearLayout curve_change_layout;
	private int idx;//SpotInfoListInstance的第idx个监测点
	private int menu_position;//从NewSpotDetailsRightMenuFragment选择的菜单项下标
	private double max_val;
	private double min_val;
	
	private ArrayList<String> curve_change_val;
	private ArrayList<String> curve_change_mark;
	
	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	
	private String serverURL;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			finish();
			return false;
		}
		return false;
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_curve_change);
		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(CurveChangeActivity.this);
		
		Bundle bundle = new Bundle();
        bundle = this.getIntent().getExtras();
		menu_position = bundle.getInt("menu_position");
		idx = NewSpotDetailsActivity.idx;
		
		serverURL = LoginState.getIns().getServerURL();
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					getData();
					break;
				case NETWORK_UNCONNECTED:
					showNetworkError();
					break;
				case START_PROCESS:
					showLoadingProgress();
					break;
				case CANCEL_PROCESS:
					cancelProgress();
					break;
				case GET_INFO_SUCCEED:
					showResult();
				default:
					break;
				}
			}
		};
		
		
		curve_change_tv_chart_title = (TextView) findViewById(R.id.curve_change_tv_chart_title);
		
		String chart_title;
		if(menu_position == 0){
			//小时均值
			chart_title = "PM10小时均值（过去24小时）\n（单位：μg/m³）";
		}else if(menu_position == 1){
			//日均值
			chart_title = "PM10日均值（过去30天）\n（单位：μg/m³）";
		}else{
			//月均值
			chart_title = "PM10月均值（本年度）\n（单位：μg/m³）";
		}
		curve_change_tv_chart_title.setText(chart_title);
		
		curve_change_goback = (Button) findViewById(R.id.curve_change_goback);
		curve_change_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
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
	
	public GraphicalView get_curve_change_view() {
		
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();// 多个渲染
		//renderer.setOrientation(Orientation.VERTICAL);	// 横向排列
		renderer.setApplyBackgroundColor(true);
		renderer.setMarginsColor(Color.WHITE);
		renderer.setBackgroundColor(Color.WHITE);
		renderer.setPointSize(8f);//设置点的大小
		renderer.setRange(new double[] { 0, 25, min_val-10 < 0 ? 0 : min_val-10, max_val+50 });
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);
		renderer.setAxesColor(Color.BLACK);
		renderer.setXLabels(0);
		renderer.setXAxisMin(-2);//设置x轴起点
		renderer.setZoomEnabled(false);// 不可缩放
		renderer.setLegendTextSize(30);// 设置图例的字体大小
		renderer.setLabelsTextSize(30);//设置x轴标记字体大小
		//左右可滑
		renderer.setPanEnabled(true, false);
		
		//renderer.setShowGridX(true);//显示网格
		//renderer.setShowGridY(true);
		//renderer.setGridColor(Color.BLACK);//网格颜色
		// 多个序列的数据集
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		
		//String title = "PM10(μg/m³)";
		String title = "";
		XYSeries series = new XYSeries(title);
		
		int valid_cnt = 0;//x轴下标应从第一个有效的数据开始，valid_cnt表示有效数据计数器
		boolean yesterday = true;//小时均值如果是昨天的最前面要加“昨”字
		for(int i = 0;i < curve_change_val.size();i++){
			double pm10_val = Double.valueOf(curve_change_val.get(i));
			if(pm10_val <= 0)	continue;
			
			if(valid_cnt != 0)	valid_cnt++;
			else				valid_cnt = 1;
			
			//20160802 月均值接口返回的小数点后的数字太多了
			if(menu_position == 0 || menu_position == 1){
				series.add(valid_cnt*3, Double.valueOf(curve_change_val.get(i)));
			}else {
				double val = Double.valueOf(curve_change_val.get(i));
				series.add(valid_cnt*5, (int)val);
			}
			
			String mark_string;//x轴下标
			String mark = curve_change_mark.get(i);
			if(menu_position == 0){
				//xx:00
				String hour_string = mark.substring(11,13);
				if(hour_string.equals("00")){
					yesterday = false;
				}
				if(yesterday){
					hour_string = "昨"+hour_string;
				}
				mark_string = hour_string+":00";
				renderer.addXTextLabel(valid_cnt*3, mark_string);
			}else if(menu_position == 1){
				//xx-xx
				mark_string = mark.substring(5,10);
				renderer.addXTextLabel(valid_cnt*3, mark_string);
			}else {
				//xxxx-xx
				renderer.addXTextLabel(valid_cnt*5, mark);
			}
			
		}
		
		dataset.addSeries(series);
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.parseColor("#47A7E6"));
		r.setPointStyle(PointStyle.CIRCLE);
		r.setFillBelowLine(true);
		r.setFillBelowLineColor(Color.parseColor("#6647A7E6"));
		r.setFillPoints(true);
		r.setDisplayChartValues(true);
		r.setChartValuesSpacing(30);
		r.setChartValuesTextSize(30);
		r.setLineWidth(6);
		renderer.addSeriesRenderer(r);
		//getCubeLineChartView 这个方法返回的就是平滑的曲线了
		/*GraphicalView mChartView = ChartFactory.getLineChartView(
				getApplicationContext(), dataset, renderer);*/
		GraphicalView mChartView = ChartFactory.getCubeLineChartView(
				getApplicationContext(), dataset, renderer, 0.5f);

		return mChartView;

	}
	
	protected void getData() {
		new Thread(){
			@Override
			public void run(){
				handler.sendEmptyMessage(START_PROCESS);
				String json = null;
				int responseCode = 0;
				/*Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, -1);
				String time1 = new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(calendar.getTime());
				Log.i("heheda", time1);
				Calendar calendar2 = Calendar.getInstance();
				calendar2.add(Calendar.HOUR, -1);
				String time2 = new SimpleDateFormat("yyyy-MM-dd HH:00:00").format(calendar2.getTime());
				Log.i("heheda", time2);
				
				String query_path = serverURL+"getHistoryData?csite_id="
						+spotInfos.get(idx).getCsite_id()
						+"&time_type=hour&begin_time="
						+time1.substring(0, 10)+"%20"+time1.substring(11, 19)
						+"&end_time="
						+time2.substring(0, 10)+"%20"+time2.substring(11, 19);*/
				String data_type;
				if(menu_position == 0){
					data_type = "hour";
				}else if(menu_position == 1){
					data_type = "day";
				}else {
					data_type = "month";
				}
				String query_path = serverURL+"csiteWhDynamicCurve?csite_id="+spotInfos.get(idx).getCsite_id()+"&data_type="+data_type;
				URL url;
				try {
					url = new URL(query_path);
					Log.i("heheda", query_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(30 * 1000);
					conn.setRequestMethod("GET");
					responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						curve_change_val = new ArrayList<String>();
						curve_change_mark = new ArrayList<String>();
						
						InputStream is = conn.getInputStream();
						byte[] data = readStream(is);
						json = new String(data);
						JSONArray jsonArray = new JSONArray(json);
						
						max_val = 0;
						min_val = 1000000000;
						
						for(int i = 0;i < jsonArray.length();i++){
							JSONObject item = jsonArray.getJSONObject(i);
							double val = Double.valueOf(item.getString("rawdata"));
							if(val > max_val)	max_val = val;
							if(val < min_val)	min_val = val;
							Log.i("heheda", "rawdata val = "+item.getString("rawdata"));
							curve_change_val.add(item.getString("rawdata"));
							curve_change_mark.add(item.getString("time"));
						}
						handler.sendEmptyMessage(GET_INFO_SUCCEED);
					}else{
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
	
	private void showResult(){
		Log.i("heheda", "come show result");
		curve_change_layout = (LinearLayout)findViewById(R.id.curve_change_layout);
		curve_change_layout.addView(get_curve_change_view(),
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
		handler.sendEmptyMessage(CANCEL_PROCESS);
	}
	
	private byte[] readStream(InputStream is) throws IOException {
		// TODO Auto-generated method stub
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		while ((rc = is.read(buff, 0, 100)) > 0) {
			swapStream.write(buff, 0, rc);
		}
		byte[] in2b = swapStream.toByteArray();
		return in2b;
	}
	/**
	 * 数据加载
	 */
	private void showLoadingProgress() {
		Log.i("hehda", "loadinginging");
		UIUtil.showProgressDialog(this, "数据加载中......");
	}

	/**
	 * 取消进度�????
	 */
	private void cancelProgress() {
		Log.i("heheda", "canceling canceling");
		UIUtil.cancelProgressDialog();
	}

	/**
	 * 显示网络连接有问�????
	 */
	private void showNetworkError(){
		Toast toast = Toast.makeText(CurveChangeActivity.this, "信息加载失败",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	/**
	 * 服务器故�???? 返回码不�????200
	 */
	private void showServerError() {
		Toast toast = Toast.makeText(CurveChangeActivity.this, "服务器维护中，请稍后",
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
}
