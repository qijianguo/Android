package com.adc.oldactivity;

import java.util.ArrayList;
import java.util.List;

import com.adc.consts.Constants;
import com.adc.hbj5.R;
import com.adc.hbj5.MainActivity;
import com.adc.hbj5.MyActivityManager;
import com.adc.live.LiveActivity;
import com.adc.playback.PlayBackActivity;
import com.adc.util.UIUtil;
import com.hikvision.vmsnetsdk.ControlUnitInfo;
import com.hikvision.vmsnetsdk.LineInfo;
import com.hikvision.vmsnetsdk.RegionInfo;
import com.hikvision.vmsnetsdk.ServInfo;
import com.hikvision.vmsnetsdk.VMSNetSDK;
import com.adc.data.LoginState;
import com.adc.data.TempData;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

@SuppressLint("HandlerLeak")
public class OldVideoActivity extends Activity {

	private final int LOGIN_HIKVISION_SUCCEED = 100;
	private final int GET_REGION_LIST_SUCCEED = 102;

	private final int ChongQin = 35;
	private final int JiNan = 21;
	private final int ShanghaiPudong = 13;
	
	private final class MsgHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case LOGIN_HIKVISION_SUCCEED:
				// 已经登陆成功，开始获取控制中心信息，一直到最后一层区域列表
				// my_getControlUnitList();
				Log.i("heheda", "city id = " + LoginState.getIns().getCityId());
				showLoadingProgress();
				if (Integer.valueOf(LoginState.getIns().getCityId()) == 35) {
					my_getRegionListFromCtrlUnit_Chongqin();
				} else if (Integer.valueOf(LoginState.getIns().getCityId()) == 21) {
					my_getRegionListFromCtrlUnit_Jinan();
				} else if (Integer.valueOf(LoginState.getIns().getCityId()) == 13) {
					my_getRegionListFromCtrlUnit_ShanghaiPudong();
				}
				break;
			case GET_REGION_LIST_SUCCEED:
				// 已经获得了全部的区域列表，开始加载list
				loadList();
				cancelProgress();
			default:
				break;
			}
		}
	}

	/** 发送消息的对象 */
	private MsgHandler handler = new MsgHandler();
	/** 登录地址 */
	private String servAddr;
	/** 用户选中的线路 */
	private LineInfo lineInfo;
	/** 登录返回的数据 */
	private ServInfo servInfo;
	/** 控制中心 这里写死成 主控制中心 */
	private ControlUnitInfo controlUnitInfo;
	/** 区域信息 最后一层区域信息 */
	private ArrayList<RegionInfo> lastRegionInfoList;

	private Button video_gobackButton;
	private ListView video_list;
	private VideoListAdapter myAdapter;
	private int region_idx;// 被点击的监测点下标
	private Dialog mDialog;

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent(OldVideoActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
			return false;
		}
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);

		MyActivityManager mam = MyActivityManager.getInstance();
		mam.pushOneActivity(OldVideoActivity.this);

		video_gobackButton = (Button) findViewById(R.id.video_goback);
		video_gobackButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(OldVideoActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
		});

		video_list = (ListView) findViewById(R.id.video_list);
		servAddr = "http://140.207.216.106";

		// 入口 登录服务器 获得监测点列表
		login();
	}

	/**
	 * 获得线路
	 */
	protected void login() {
		new Thread() {
			@Override
			public void run() {
				/*List<LineInfo> lineInfoList = new ArrayList<LineInfo>();
				boolean ret = VMSNetSDK.getInstance().getLineList(servAddr,
						lineInfoList);
				if (ret) {
					Log.i("heheda", "获得线路列表成功" + lineInfoList.size());
					// 默认选择第一条线路
					lineInfo = lineInfoList.get(0);
				} else {
					Log.i("heheda", "获得线路列表失败");
				}

				String userName = "admin";
				String password = "landfun2012";

				String macAddress = getMac();

				// 登录请求
				servInfo = new ServInfo();
				ret = VMSNetSDK.getInstance().login(servAddr, userName,
						password, lineInfo.lineID, macAddress, servInfo);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				if (servInfo != null) {
					// 打印出登录时返回的信息
					Log.i("heheda", "login ret : " + ret);
					Log.i("heheda", "login response info[" + "sessionID:"
							+ servInfo.sessionID + ",userID:" + servInfo.userID
							+ ",magInfo:" + servInfo.magInfo
							+ ",picServerInfo:" + servInfo.picServerInfo
							+ ",ptzProxyInfo:" + servInfo.ptzProxyInfo
							+ ",userCapability:" + servInfo.userCapability
							+ ",vmsList:" + servInfo.vmsList + ",vtduInfo:"
							+ servInfo.vtduInfo + ",webAppList:"
							+ servInfo.webAppList + "]");
				}
				
				if (ret)
					TempData.getIns().setLoginData(servInfo);
				*/
				servInfo =  TempData.getIns().getLoginData();
				handler.sendEmptyMessage(LOGIN_HIKVISION_SUCCEED);
			};
		}.start();
	}

	/**
	 * 获取登录设备mac地址
	 * 
	 * @return
	 */
	protected String getMac() {
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		String mac = wm.getConnectionInfo().getMacAddress();
		return mac == null ? "" : mac;
	}

	protected void my_getRegionListFromCtrlUnit_Jinan() {
		new Thread() {
			@Override
			public void run() {
				List<ControlUnitInfo> controlUnitInfos = new ArrayList<ControlUnitInfo>();
				boolean ret = VMSNetSDK.getInstance().getControlUnitList(
						servAddr, servInfo.sessionID, 0, 10000, 1,
						controlUnitInfos);
				Log.i("heheda", "my_getControlUnitList ret=" + ret);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				controlUnitInfo = controlUnitInfos.get(0);
				Log.i("heheda", "ctrl unit info:" + controlUnitInfo.name);
				List<RegionInfo> regionInfos = new ArrayList<RegionInfo>();
				boolean ret1 = VMSNetSDK.getInstance()
						.getRegionListFromCtrlUnit(servAddr,
								servInfo.sessionID,
								controlUnitInfo.controlUnitID, 10000, 1,
								regionInfos);
				Log.i("heheda", "my_getRegionListFromCtrlUnit ret=" + ret1);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				RegionInfo regionInfo = regionInfos.get(4); // 这里得到济南
				Log.i("heheda", "region name=" + regionInfo.name);

				List<RegionInfo> subRegionInfos = new ArrayList<RegionInfo>();
				boolean ret2 = VMSNetSDK.getInstance().getRegionListFromRegion(
						servAddr, servInfo.sessionID, regionInfo.regionID,
						10000, 1, subRegionInfos);
				Log.i("heheda", "my_getRegionListFromRegion ret=" + ret2);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				RegionInfo subRegionInfo = subRegionInfos.get(0); // 这里得到天桥
				Log.i("heheda", "sub region info:" + subRegionInfo.name);

				lastRegionInfoList = new ArrayList<RegionInfo>();
				boolean ret3 = VMSNetSDK.getInstance().getRegionListFromRegion(
						servAddr, servInfo.sessionID, subRegionInfo.regionID,
						10000, 1, lastRegionInfoList);
				Log.i("heheda", "my_getLastregionListFromSubregion ret=" + ret3);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				Log.i("heheda",
						"lastRegionInfoList size=" + lastRegionInfoList.size());

				handler.sendEmptyMessage(GET_REGION_LIST_SUCCEED);
			}
		}.start();
	}

	protected void my_getRegionListFromCtrlUnit_Chongqin() {
		new Thread() {
			@Override
			public void run() {
				List<ControlUnitInfo> controlUnitInfos = new ArrayList<ControlUnitInfo>();
				boolean ret = VMSNetSDK.getInstance().getControlUnitList(
						servAddr, servInfo.sessionID, 0, 10000, 1,
						controlUnitInfos);
				Log.i("heheda", "my_getControlUnitList ret=" + ret);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				controlUnitInfo = controlUnitInfos.get(0);
				Log.i("heheda", "ctrl unit info:" + controlUnitInfo.name);
				List<RegionInfo> regionInfos = new ArrayList<RegionInfo>();
				boolean ret1 = VMSNetSDK.getInstance()
						.getRegionListFromCtrlUnit(servAddr,
								servInfo.sessionID,
								controlUnitInfo.controlUnitID, 10000, 1,
								regionInfos);
				Log.i("heheda", "my_getRegionListFromCtrlUnit ret=" + ret1);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				RegionInfo regionInfo = regionInfos.get(0); // 这里得到重庆
				Log.i("heheda", "region name=" + regionInfo.name);

				List<RegionInfo> subRegionInfos = new ArrayList<RegionInfo>();
				boolean ret2 = VMSNetSDK.getInstance().getRegionListFromRegion(
						servAddr, servInfo.sessionID, regionInfo.regionID,
						10000, 1, subRegionInfos);
				Log.i("heheda", "my_getRegionListFromRegion ret=" + ret2);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				RegionInfo subRegionInfo = subRegionInfos.get(0); // 这里得到江北
				Log.i("heheda", "sub region info:" + subRegionInfo.name);

				lastRegionInfoList = new ArrayList<RegionInfo>();
				boolean ret3 = VMSNetSDK.getInstance().getRegionListFromRegion(
						servAddr, servInfo.sessionID, subRegionInfo.regionID,
						10000, 1, lastRegionInfoList);
				Log.i("heheda", "my_getLastregionListFromSubregion ret=" + ret3);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				Log.i("heheda",
						"lastRegionInfoList size=" + lastRegionInfoList.size());

				for(int i = 0;i < lastRegionInfoList.size();i++){
					RegionInfo rginfo = lastRegionInfoList.get(i);
					Log.i("heheda", "每个region的信息如下\n"+"name="+rginfo.name
							+"\n region_id="+rginfo.regionID);
				}
				handler.sendEmptyMessage(GET_REGION_LIST_SUCCEED);
			}
		}.start();
	}

	protected void my_getRegionListFromCtrlUnit_ShanghaiPudong() {
		new Thread() {
			@Override
			public void run() {
				List<ControlUnitInfo> controlUnitInfos = new ArrayList<ControlUnitInfo>();
				boolean ret = VMSNetSDK.getInstance().getControlUnitList(
						servAddr, servInfo.sessionID, 0, 10000, 1,
						controlUnitInfos);
				Log.i("heheda", "my_getControlUnitList ret=" + ret);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				controlUnitInfo = controlUnitInfos.get(0);
				Log.i("heheda", "ctrl unit info:" + controlUnitInfo.name);
				List<RegionInfo> regionInfos = new ArrayList<RegionInfo>();
				boolean ret1 = VMSNetSDK.getInstance()
						.getRegionListFromCtrlUnit(servAddr,
								servInfo.sessionID,
								controlUnitInfo.controlUnitID, 10000, 1,
								regionInfos);
				Log.i("heheda", "my_getRegionListFromCtrlUnit ret=" + ret1);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				RegionInfo regionInfo = regionInfos.get(3); // 这里得到上海
				Log.i("heheda", "region name=" + regionInfo.name);

				List<RegionInfo> subRegionInfos = new ArrayList<RegionInfo>();
				boolean ret2 = VMSNetSDK.getInstance().getRegionListFromRegion(
						servAddr, servInfo.sessionID, regionInfo.regionID,
						10000, 1, subRegionInfos);
				Log.i("heheda", "my_getRegionListFromRegion ret=" + ret2);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				RegionInfo subRegionInfo = subRegionInfos.get(1); // 这里得到浦东
				Log.i("heheda", "sub region info:" + subRegionInfo.name);

				lastRegionInfoList = new ArrayList<RegionInfo>();
				boolean ret3 = VMSNetSDK.getInstance().getRegionListFromRegion(
						servAddr, servInfo.sessionID, subRegionInfo.regionID,
						10000, 1, lastRegionInfoList);
				RegionInfo zhangjiang = lastRegionInfoList.get(0);
				int len = lastRegionInfoList.size();
				for (int i = 0; i < len; i++) {
					lastRegionInfoList.remove(0);
				}
				lastRegionInfoList.add(zhangjiang);
				Log.i("heheda", "my_getLastregionListFromSubregion ret=" + ret3);
				Log.i("heheda", "error code = " + UIUtil.getErrorDesc());
				Log.i("heheda",
						"lastRegionInfoList size=" + lastRegionInfoList.size());
				handler.sendEmptyMessage(GET_REGION_LIST_SUCCEED);
			}
		}.start();
	}

	// 这里要大改。
	protected void loadList() {
		myAdapter = new VideoListAdapter(OldVideoActivity.this,
				lastRegionInfoList, this, servAddr, servInfo.sessionID);
		video_list.setAdapter(myAdapter);
		/*
		 * video_list.setOnItemClickListener(new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> parent, View view,
		 * int position, long id) { // TODO Auto-generated method stub
		 * region_idx = position; int idx = (Integer)
		 * myAdapter.getItem(position); Log.i("heheda",
		 * "点击了"+lastRegionInfoList.get(idx).name); } });
		 * handler.sendEmptyMessage(103);
		 */
	}

	/*
	 * protected void my_getCameraListFromRegion() { new Thread(){ public void
	 * run(){ List<CameraInfo> cameraInfos = new ArrayList<CameraInfo>(); int
	 * regionID = lastRegionInfoList.get(region_idx).regionID; boolean ret =
	 * VMSNetSDK.getInstance().getCameraListFromRegion(servAddr,
	 * servInfo.sessionID, regionID, 10000, 1, cameraInfos); Log.i("heheda",
	 * "my_getCameraListFromRegion ret=" + ret); Log.i("heheda", "error code = "
	 * + UIUtil.getErrorDesc()); cameraInfo = cameraInfos.get(0); //
	 * 得到摄像头信息A-XXX Log.i("heheda", "cameraInfo:" + cameraInfo.name);
	 * handler.sendEmptyMessage(104); } }.start(); }
	 */

	/**
	 * 加载进度条
	 */
	private void showLoadingProgress() {
		UIUtil.showProgressDialog(this, "正在加载监控点列表");
	}

	/**
	 * 取消进度条
	 */
	private void cancelProgress() {
		UIUtil.cancelProgressDialog();
	}

}
