package com.adc.shapingba;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adc.consts.Constants;
import com.adc.data.LoginState;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class WuhanMainRightMenuFragment extends Fragment{
	private View my_view;
	private ListView wuhan_main_right_menu_list;
	
	private ListViewAdapter my_list_adapter;
	private String[] menu_item_name = new String[] { "工地地图",
			"统计分析",
			"运维管理",
			"国控数据",
			"运维报告",
			"督办列表",
			"在线投诉"};
	private int[] menu_item_image_id = new int[] {
			R.drawable.menu_map,
			R.drawable.menu_statistic,
			R.drawable.menu_maintain,
			R.drawable.menu_guokong,
			R.drawable.menu_report,
			R.drawable.menu_list,
			R.drawable.menu_complain};
	
	private List<Map<String, Object>> list_items;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		if (my_view == null)
		{
			initView(inflater, container);
		}
		return my_view;
	}

	private void initView(LayoutInflater inflater, ViewGroup container)
	{
		my_view = inflater.inflate(R.layout.wuhan_main_right_menu, container, false);
		wuhan_main_right_menu_list = (ListView) my_view
				.findViewById(R.id.wuhan_main_right_menu_list);
		
		list_items = getListItems();
		my_list_adapter = new ListViewAdapter(getActivity(), list_items);		
		wuhan_main_right_menu_list.setAdapter(my_list_adapter);
		
		wuhan_main_right_menu_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				//arg2就是position，即选择的菜单项的下标
				Log.i("heheda", "pos="+arg2);
				if(arg2 == 0) {
					//Intent intent = new Intent(getActivity(),WuhanCsiteMapActivity.class);
					Intent intent = new Intent(getActivity(),MapOverviewActivity.class);
					startActivity(intent);
				}else if (arg2 == 1) {
					Intent intent = new Intent(getActivity(),WuhanStatisticsActivity.class);
					startActivity(intent);
				}else if (arg2 == 2) {
					Intent intent = new Intent(getActivity(),WuhanYunweiActivity.class);
					startActivity(intent);
				}else if (arg2 == 3) {
					Intent intent = new Intent(getActivity(),WuhanStationMapActivity.class);
					startActivity(intent);
				}else if (arg2 == 4) {
					Intent intent = new Intent(getActivity(),WuhanYunweiBaogaoActivity.class);
					startActivity(intent);
				}else if (arg2 == 5) {
					Intent intent = new Intent(getActivity(),WuhanDubanActivity.class);
					startActivity(intent);
				}else if (arg2 == 6) {
					Intent intent = new Intent(getActivity(),WuhanComplaintActivity.class);
					startActivity(intent);
				}
			}
		});
	}
	
	private List<Map<String, Object>> getListItems() {
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < menu_item_name.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("image", menu_item_image_id[i]);
			map.put("title", menu_item_name[i]);
			listItems.add(map);
		}
		return listItems;
	}
}
