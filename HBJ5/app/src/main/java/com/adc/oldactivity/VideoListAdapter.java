package com.adc.oldactivity;

import java.util.ArrayList;
import java.util.List;

import com.adc.consts.Constants;
import com.adc.data.TempData;
import com.adc.hbj5.R;
import com.adc.live.LiveActivity;
import com.adc.playback.PlayBackActivity;
import com.adc.util.UIUtil;
import com.hikvision.vmsnetsdk.CameraInfo;
import com.hikvision.vmsnetsdk.RegionInfo;
import com.hikvision.vmsnetsdk.VMSNetSDK;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class VideoListAdapter extends BaseAdapter {

	private Handler handler = null;
	private Context context;
	private LayoutInflater inflater;
	private String servAddr;
	private String sessionID;
	private ArrayList<RegionInfo> lists;
	private Dialog mDialog;
	private Activity a;
	private CameraInfo cameraInfo;

	public VideoListAdapter(Context context, ArrayList<RegionInfo> lists,
			Activity a, String servAddr, String sessionID) {
		super();
		Log.i("heheda", "gouzao lists size=" + lists.size());
		this.a = a;
		this.context = context;
		this.lists = lists;
		this.servAddr = servAddr;
		this.sessionID = sessionID;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lists.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final RegionInfo last_region = lists.get(position);
		final String string = last_region.name;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.small_character_list_item,
					null);
		}

		TextView small_character_textview = (TextView) convertView
				.findViewById(R.id.small_character_textview);
		small_character_textview.setText(string);
		small_character_textview.setTextColor(Color.BLACK);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 0:
					gotoLiveOrPlayBack(cameraInfo);
					break;

				default:
					break;
				}
			}
		};

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("heheda", "我点了" + string);
				new Thread() {
					@Override
					public void run() {
						List<CameraInfo> cameraInfos = new ArrayList<CameraInfo>();
						int regionID = last_region.regionID;
						boolean ret = VMSNetSDK.getInstance()
								.getCameraListFromRegion(servAddr, sessionID,
										regionID, 10000, 1, cameraInfos);
						Log.i("heheda", "my_getCameraListFromRegion ret=" + ret);
						Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
						cameraInfo = cameraInfos.get(0); // 得到摄像头信息A-XXX
						
						//具体的信息有哪些？？
						Log.i("heheda", "camera_id="+cameraInfo.cameraID);
						handler.sendEmptyMessage(0);
					}
				}.start();
			}
		});
		return convertView;
	}

	private void gotoLiveOrPlayBack(final CameraInfo info) {
		String[] datas = new String[] { "预览", "回放" };
		mDialog = new AlertDialog.Builder(a).setSingleChoiceItems(datas, 0,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mDialog.dismiss();
						switch (which) {

						case 0:
							gotoLive(info);
							break;
						case 1:
							gotoPlayback(info);
							break;
						default:
							break;
						}
					}
				}).create();
		mDialog.show();

	}

	/**
	 * 进入远程回放
	 * 
	 * @param info
	 * @since V1.0
	 */
	protected void gotoPlayback(CameraInfo info) {
		if (info == null) {
			Log.e(Constants.LOG_TAG, "gotoPlayback():: fail");
			return;
		}
		Intent it = new Intent(a, PlayBackActivity.class);
		it.putExtra(Constants.IntentKey.CAMERA_ID, info.cameraID);
		it.putExtra(Constants.IntentKey.DEVICE_ID, info.deviceID);
		Log.e("heheda", "deviceID = "+info.deviceID);
		a.startActivity(it);

	}

	/**
	 * 进入实时预览
	 * 
	 * @param info
	 * @since V1.0
	 */
	protected void gotoLive(CameraInfo info) {
		if (info == null) {
			Log.e(Constants.LOG_TAG, "gotoLive():: fail");
			return;
		}
		Log.i("heheda", "camera name = " + info.name);
		Log.i("heheda", a.getLocalClassName());
		Intent it = new Intent(a, LiveActivity.class);
		Log.i("heheda", "11111111111111111111");
		it.putExtra(Constants.IntentKey.CAMERA_ID, info.cameraID);
		Log.i("heheda", "2222222222222222222："+info.cameraID);
		TempData.getIns().setCameraInfo(info);
		Log.i("heheda", "333333333333333333");
		a.startActivity(it);
		Log.i("heheda", "4444444444444444444");
	}

}
