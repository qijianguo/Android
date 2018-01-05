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

public class NewSpotDetailsRightMenuFragment extends Fragment{
	private View my_view;
	private ListView new_spot_details_right_menu_list;
	
	private ListViewAdapter my_list_adapter;
	private String[] menu_item_name = new String[] { "工地小时均值",
			"工地日均值",
			"工地月均值"};
	private int[] menu_item_image_id = new int[] {
			R.drawable.menu_hour,
			R.drawable.menu_day,
			R.drawable.menu_month};
	
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
		my_view = inflater.inflate(R.layout.new_spot_details_right_menu, container, false);
		new_spot_details_right_menu_list = (ListView) my_view
				.findViewById(R.id.new_spot_details_right_menu_list);
		
		list_items = getListItems();
		my_list_adapter = new ListViewAdapter(getActivity(), list_items);		
		new_spot_details_right_menu_list.setAdapter(my_list_adapter);
		
		new_spot_details_right_menu_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				//arg2就是position，即选择的菜单项的下标
				Log.i("heheda", "pos="+arg2);
				if(arg2 <= 2){
					Bundle bundle = new Bundle();
					bundle.putInt("menu_position", arg2);
					Intent intent = new Intent(getActivity(),CurveChangeActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}/*else {
					Intent intent = new Intent(getActivity(),WuhanCsiteMapActivity.class);
					startActivity(intent);
				}*/
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
