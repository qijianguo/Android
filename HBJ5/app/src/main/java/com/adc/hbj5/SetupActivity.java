package com.adc.hbj5;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.adc.consts.Constants;
import com.adc.data.LoginState;
import com.adc.hbj5.R;
import com.adc.statistics.TjfxActivity;
import com.adc.util.UIUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint({ "HandlerLeak", "InflateParams" })
public class SetupActivity extends Activity {

	private final int NETWORK_CONNECTED = 100;
	private final int NETWORK_UNCONNECTED = 101;
	private final int URL_REQUEST_FAIL = 102;
	private final int START_PROCESS = 103;
	private final int CANCEL_PROCESS = 104;
	private final int GET_VERSION_INFO_SUCCEED = 105;
	private final int UPDATE_NEEDED = 106;
	private final int UPDATE_DOWNLOAD_PROGRESS = 107;
	private final int DOWNLOAD_FINISHED = 108;
	
	private Handler handler = null;
	private ListView listView;
	private ListViewAdapter listViewAdapter;
	private List<Map<String, Object>> listItems;
	private Button setup_goback;
	private String currentVersion;
	private String latestVersion;
	private String downloadUrl;

	/* 下载保存路径 */
	private String mSavePath;
	/* 记录进度条数量 */
	private int progress;
	/* 下载进度条 */
	private ProgressBar mProgress;

	private boolean cancelUpdate = false;

	private Dialog mDownloadDialog;

	//private String[] setup_item_name = new String[] { "版本 V2.2", "获取更新", "关于" };
	private String[] setup_item_name = new String[] { "版本 V2.6","关于" };

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
			Intent intent;
			if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
				intent = new Intent(SetupActivity.this,LoginActivity.class);
			}if(LoginState.getIns().getUi_type().equals("4")){
				//中建八局用户ui_type="4"
				intent = new Intent(SetupActivity.this, ZJBJMainActivity.class);
			}else{
				intent = new Intent(SetupActivity.this,ZJBJMainActivity.class);
			}
			startActivity(intent);
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(SetupActivity.this);

		setup_goback = (Button) findViewById(R.id.setup_goback);
		setup_goback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//int city_id = Integer.valueOf(LoginState.getIns().getCityId());
				Intent intent;
				if(LoginState.getIns().getServerURL().equals(Constants.SERVER_URL_TIANJIN)){
					intent = new Intent(SetupActivity.this,LoginActivity.class);
				}else if(LoginState.getIns().getUi_type().equals("4")){
					//中建八局用户ui_type="4"
					intent = new Intent(SetupActivity.this, ZJBJMainActivity.class);
				}else{
					intent = new Intent(SetupActivity.this,ZJBJMainActivity.class);
				}
				startActivity(intent);
				finish();
			}
		});

		listView = (ListView) findViewById(R.id.setup_list);
		listItems = getListItems();
		listViewAdapter = new ListViewAdapter(this, listItems);
		listView.setAdapter(listViewAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Log.i("heheda", "hehe="
						+ listViewAdapter.getItem(position).get("title"));
				Map<String, Object> map = listViewAdapter
						.getItem(position);
				if (map.get("title").equals(setup_item_name[0])) {

				} else if (map.get("title").equals(setup_item_name[1])) {
					//checkConnect();
				} //else if (map.get("title").equals(setup_item_name[2])) {

				//}
			}
		});

		currentVersion = "";
		try {
			currentVersion = getVersionName();
			Log.i("heheda", "get current version success");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case NETWORK_CONNECTED:
					getLatestVersionName();
					break;
				case NETWORK_UNCONNECTED:
					showNetworkError();
					break;
				case START_PROCESS:
					showProgress();
					break;
				case CANCEL_PROCESS:
					cancelProgress();
					break;
				case GET_VERSION_INFO_SUCCEED:
					checkVersion();
					break;
				case UPDATE_NEEDED:
					showDownloadDialog();
					break;
				case UPDATE_DOWNLOAD_PROGRESS:
					mProgress.setProgress(progress);
					break;
				case DOWNLOAD_FINISHED:
					installApk();
					break;
				case URL_REQUEST_FAIL:
					showServerError();
				default:
					break;
				}
			}
		};
	}

	private List<Map<String, Object>> getListItems() {
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < setup_item_name.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			// map.put("image", R.drawable.blank_grey);
			map.put("title", setup_item_name[i]);
			listItems.add(map);
		}
		return listItems;
	}

	private void checkConnect() {
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

	private void showNetworkError() {
		Toast toast = Toast.makeText(SetupActivity.this, "亲，你还没连接网络呢= =!",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	private void showServerError() {
		Toast toast = Toast.makeText(SetupActivity.this, "获取更新失败!",
				Toast.LENGTH_SHORT);
		toast.show();
	}

	private void checkVersion() {
		handler.sendEmptyMessage(CANCEL_PROCESS);
		Log.i("heheda", "cur" + currentVersion);
		Log.i("heheda", "la" + latestVersion);
		double v1 = Double.valueOf(currentVersion);
		double v2 = Double.valueOf(latestVersion);
		if (v1 == v2) {
			Log.i("heheda", "yiyang");
			Toast toast = Toast.makeText(SetupActivity.this, "您安装的当前版本已是最新版本！",
					Toast.LENGTH_SHORT);
			toast.show();
		} else {
			Log.i("heheda", "buyiyang");
			AlertDialog.Builder builder = new AlertDialog.Builder(
					SetupActivity.this);
			builder.setTitle("您确定要下载新的版本吗？");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// 这里添加点击确定后的逻辑
							handler.sendEmptyMessage(UPDATE_NEEDED);
						}
					});
			builder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// 这里添加点击确定后的逻辑
						}
					});
			builder.create().show();
		}
	}

	private String getVersionName() throws Exception {
		PackageManager packageManager = getPackageManager();
		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),
				0);
		return packInfo.versionName;
	}

	private void getLatestVersionName() {
		new Thread() {
			@Override
			public void run() {
				handler.sendEmptyMessage(START_PROCESS);
				String query_path = "http://112.64.16.233:8088/Android_App/version.txt";
				String json = null;
				int responseCode = 0;
				URL url;
				try {
					url = new URL(query_path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(10 * 1000);
					conn.setRequestMethod("GET");
					responseCode = conn.getResponseCode();
					if (responseCode == 200) {
						InputStream is = conn.getInputStream();
						byte[] data = readStream(is);
						json = new String(data);
						JSONObject item = new JSONObject(json);

						latestVersion = item.getString("version");
						downloadUrl = item.getString("download_url");
						handler.sendEmptyMessage(GET_VERSION_INFO_SUCCEED);
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

	private static byte[] readStream(InputStream is) throws IOException {
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
	 * 获取更新进度条
	 */
	private void showProgress() {
		UIUtil.showProgressDialog(this, "新版本查询中");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}

	/**
	 * 显示软件下载对话框
	 */
	private void showDownloadDialog() {
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(SetupActivity.this);
		builder.setTitle("正在更新");
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(SetupActivity.this);
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		// 取消更新
		builder.setNegativeButton("取消更新",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// 这里添加点击确定后的逻辑
						dialog.dismiss();
						cancelUpdate = true;
					}
				});
		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		// 下载文件
		downloadApk();
	}

	/**
	 * 下载apk文件
	 */
	private void downloadApk() {
		// 启动新线程下载软件
		new downloadApkThread().start();
	}

	/**
	 * 下载文件线程
	 * 
	 * @author coolszy
	 * @date 2012-4-26
	 * @blog http://blog.92coding.com
	 */
	private class downloadApkThread extends Thread {
		@Override
		public void run() {
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory()
							+ "/";
					mSavePath = sdpath + "download";
					URL url = new URL(downloadUrl);
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(mSavePath, "HBJ");
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						handler.sendEmptyMessage(UPDATE_DOWNLOAD_PROGRESS);
						if (numread <= 0) {
							// 下载完成
							handler.sendEmptyMessage(DOWNLOAD_FINISHED);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
		}
	};

	/**
	 * 安装APK文件
	 */
	private void installApk() {
		File apkfile = new File(mSavePath, "HBJ");
		if (!apkfile.exists()) {
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		startActivity(i);
	}

}
