package com.adc.hbj5;

import java.util.ArrayList;

import com.adc.hbj5.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TableListViewAdapter2 extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private ArrayList<ArrayList<String>> lists;

	public TableListViewAdapter2(Context context,
			ArrayList<ArrayList<String>> lists) {
		super();
		this.context = context;
		this.lists = lists;
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
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ArrayList<String> list = lists.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.table_list_item_2, null);
		}
		convertView.setBackgroundColor(Color.WHITE);
		TextView monitor_object = (TextView) convertView
				.findViewById(R.id.monitor_object);
		TextView monitor_data = (TextView) convertView
				.findViewById(R.id.monitor_data);
		TextView monitor_time = (TextView) convertView
				.findViewById(R.id.monitor_time);
		// Log.i("heheda", "hehe ok!!!,the first string is"+list.get(0));
		// Log.i("heheda", "hehe ok!!!,the second string is"+list.get(1));

		monitor_object.setText(list.get(0));
		monitor_data.setText(list.get(1));
		monitor_time.setText(list.get(2));

		monitor_object.setTextColor(Color.BLACK);
		monitor_data.setTextColor(Color.BLACK);
		monitor_time.setTextColor(Color.BLACK);

		if (position == 0) {
			convertView.setBackgroundResource(R.color.icon_blue);
			monitor_object.setTextColor(Color.WHITE);
			monitor_data.setTextColor(Color.WHITE);
			monitor_time.setTextColor(Color.WHITE);
		} else {
			if (position % 2 != 0) {
				convertView.setBackgroundColor(Color.argb(250, 255, 255, 255));
			} else {
				convertView.setBackgroundColor(Color.argb(250, 224, 243, 250));
			}
		}

		return convertView;
	}

}
