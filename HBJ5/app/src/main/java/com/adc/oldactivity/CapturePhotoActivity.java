package com.adc.oldactivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.adc.data.LoginState;
import com.adc.data.SpotInfo;
import com.adc.data.SpotInfoListInstance;
import com.adc.hbj5.R;
import com.adc.hbj5.HxdbTabActivity;
import com.adc.hbj5.MainTabActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.hbj5.SpotInfoTabActivity;
import com.adc.util.UIUtil;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.drawable.BitmapDrawable;
import android.media.audiofx.NoiseSuppressor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextDirectionHeuristic;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

@SuppressLint("HandlerLeak")
public class CapturePhotoActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int DOWNLOAD_SUCCEED = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int NO_PHOTO = 105;
	private final int URL_REQUEST_FAIL = 106;

	private Handler handler = null;
	private Button capture_photo_goback;
	private TextView capture_time;
	private TextView capture_noise;
	private TextView capture_pm2_5;
	private TextView capture_pm10;
	private ImageSwitcher switcher = null;
	private Gallery gallery = null;

	/** 所选择监测点列表(spotList)中监测点的下标，根据这个下标可以获得csite id name等各种信息 */
	private int idx;
	/** 当前监测点所被抓拍照片数量 */
	private int spot_photo_len;

	private String photo_url[];
	private String time[];
	private String pm2_5[];
	private String pm10[];
	private String noise[];
	private static Bitmap bitmap[];
	private int from_map_or_jcxq;

	private ArrayList<SpotInfo> spotInfos = SpotInfoListInstance.getIns().getList();
	
	private String serverURL;
	
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			recyclePhotoMemory();
			if (from_map_or_jcxq == 0) {
				Intent goback_intent = new Intent(CapturePhotoActivity.this,
						HxdbTabActivity.class);
				startActivity(goback_intent);
				finish();
			} else {
				//MainTabActivity.hxdb_tjfx_setup = 1;
				Intent goback_intent = new Intent(CapturePhotoActivity.this,
						MainTabActivity.class);
				startActivity(goback_intent);
				finish();
			}
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture_photo);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(CapturePhotoActivity.this);

		serverURL = LoginState.getIns().getServerURL();
		
		capture_time = (TextView) findViewById(R.id.capture_time);
		capture_noise = (TextView) findViewById(R.id.capture_noise);
		capture_pm2_5 = (TextView) findViewById(R.id.capture_pm2_5);
		capture_pm10 = (TextView) findViewById(R.id.capture_pm10);

		switcher = (ImageSwitcher) findViewById(R.id.switcher);
		gallery = (Gallery) findViewById(R.id.gallery);

		idx = SpotInfoTabActivity.spot_idx;
		from_map_or_jcxq = 0;
		if (idx >= 100000) {
			idx -= 100000;
			from_map_or_jcxq = 1;
		} else {
			from_map_or_jcxq = 0;
		}
		capture_photo_goback = (Button) findViewById(R.id.capture_photo_goback);
		capture_photo_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				recyclePhotoMemory();
				if (from_map_or_jcxq == 0) {
					Intent goback_intent = new Intent(
							CapturePhotoActivity.this, HxdbTabActivity.class);
					startActivity(goback_intent);
					finish();
				} else {
					//MainTabActivity.hxdb_tjfx_setup = 1;
					Intent goback_intent = new Intent(
							CapturePhotoActivity.this, MainTabActivity.class);
					startActivity(goback_intent);
					finish();
				}
			}
		});

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					downloadPhoto();
					break;
				case NETWORK_UNCONNECTED:
					showNetworkError();
					break;
				case DOWNLOAD_SUCCEED:
					loadPhoto();
					break;
				case START_PROCESS:
					showLoadProgress();
					break;
				case CANCEL_PROCESS:
					cancelProgress();
					break;
				case NO_PHOTO:
					showNoPhoto();
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
	 * 从服务器取图片 http://bbs.3gstdy.com
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getHttpBitmap(String url) {
		URL myFileUrl = null;
		Bitmap bitmap = null;
		try {
			myFileUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setConnectTimeout(0);
			conn.setDoInput(true);
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * 下载照片
	 */
	protected void downloadPhoto() {
		new Thread() {
			@Override
			public void run() {
				String photo_path = serverURL+"viewPhoto?csite_id="
						+ spotInfos.get(idx).getCsite_id();
				spot_photo_len = 0;
				URL url;
				handler.sendEmptyMessage(START_PROCESS);
				try {
					url = new URL(photo_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(50 * 1000);
					conn.setRequestMethod("GET");
					int responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = readStream(is);
						String json = new String(data);

						JSONArray jsonArray = new JSONArray(json);

						spot_photo_len = jsonArray.length();

						photo_url = new String[spot_photo_len + 1];
						bitmap = new Bitmap[spot_photo_len + 1];
						time = new String[spot_photo_len + 1];
						noise = new String[spot_photo_len];
						pm2_5 = new String[spot_photo_len + 1];
						pm10 = new String[spot_photo_len + 1];

						for (int i = 0; i < spot_photo_len; i++) {
							JSONObject item = jsonArray.getJSONObject(i);
							photo_url[i] = item.getString("url");
							bitmap[i] = getHttpBitmap(photo_url[i]);
							time[i] = item.getString("time");
							noise[i] = item.getString("noise");
							pm2_5[i] = item.getString("pm2_5");
							pm10[i] = item.getString("pm10");
						}
						if (spot_photo_len == 0) {
							handler.sendEmptyMessage(NO_PHOTO);
						}
						handler.sendEmptyMessage(DOWNLOAD_SUCCEED);
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

	/**
	 * 加载照片
	 */
	protected void loadPhoto() {
		switcher.setFactory(new ViewFactory() {
			@Override
			public View makeView() {
				// TODO Auto-generated method stub
				ImageView imageView = new ImageView(CapturePhotoActivity.this);
				imageView.setBackgroundColor(0xff0000);
				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				return imageView;
			}
		});
		switcher.setInAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in));
		switcher.setOutAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out));

		final int photo_len = spot_photo_len;
		BaseAdapter adapter = new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				ImageView imageView = new ImageView(CapturePhotoActivity.this);
				imageView.setImageBitmap(bitmap[position % photo_len]);
				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				imageView.setLayoutParams(new Gallery.LayoutParams(424, 300));
				// TypedArray typedArray =
				// obtainStyledAttributes(R.styleable.Gallery);
				// imageView.setBackgroundResource(typedArray.getResourceId(
				// R.styleable.Gallery_android_galleryItemBackground, 0));
				return imageView;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return photo_len;
			}
		};

		gallery.setAdapter(adapter);
		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// Bitmap bitmap = getHttpBitmap(photo_url[position%photo_len]);
				BitmapDrawable bd = new BitmapDrawable(bitmap[position
						% photo_len]);
				switcher.setImageDrawable(bd);

				String time_string = time[position % photo_len];
				time_string = time_string.substring(0, 4) + "-"
						+ time_string.substring(4, 6) + "-"
						+ time_string.substring(6, 8) + "  "
						+ time_string.substring(9, 11) + ":"
						+ time_string.substring(11, 13) + ":"
						+ time_string.substring(13, 15);
				capture_time.setText(time_string);
				capture_noise.setText(noise[position % photo_len]);
				capture_pm2_5.setText(pm2_5[position % photo_len]);
				capture_pm10.setText(pm10[position % photo_len]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		handler.sendEmptyMessage(CANCEL_PROCESS);
	}

	/**
	 * 提示网络环境有问题
	 */
	protected void showNetworkError() {
		Toast toast = Toast.makeText(CapturePhotoActivity.this, "网络异常，无法加载照片",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 提示该监测点暂时无照片数据
	 */
	protected void showNoPhoto() {
		Toast toast = Toast.makeText(CapturePhotoActivity.this, "该监测点暂时无照片数据",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 提示服务器维护中，因url请求返回码不为200
	 */
	protected void showServerError() {
		Toast toast = Toast.makeText(CapturePhotoActivity.this, "服务器维护中，请稍后再试",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 照片加载进度条
	 */
	private void showLoadProgress() {
		UIUtil.showProgressDialog(this, "照片加载中......");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}

	public static void recyclePhotoMemory() {
		if (bitmap == null)
			return;
		for (int i = 0; i < bitmap.length; i++) {
			if (bitmap[i] != null && !bitmap[i].isRecycled()) {
				bitmap[i].recycle();
				bitmap[i] = null;
				System.gc();
			}
		}
	}
}
