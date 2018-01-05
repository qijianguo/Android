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

public class TableListViewAdapter3 extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private ArrayList<ArrayList<String>> lists;

	public TableListViewAdapter3(Context context,
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
			convertView = inflater.inflate(R.layout.table_list_item3, null);
		}
		convertView.setBackgroundColor(Color.WHITE);
		TextView detail_name = (TextView) convertView
				.findViewById(R.id.detail_name);
		TextView detail_content = (TextView) convertView
				.findViewById(R.id.detail_content);
		TextView detail_time = (TextView) convertView
				.findViewById(R.id.detail_time);
		// Log.i("heheda", "hehe ok!!!,the first string is"+list.get(0));
		// Log.i("heheda", "hehe ok!!!,the second string is"+list.get(1));

		detail_name.setText(list.get(0));
		detail_content.setText(list.get(1));
		detail_time.setText(list.get(2));
		detail_name.setTextColor(Color.BLACK);
		detail_content.setTextColor(Color.BLACK);
		detail_time.setTextColor(Color.BLACK);
		if (position == 0) {
			convertView.setBackgroundResource(R.color.icon_blue);
			detail_name.setTextColor(Color.WHITE);
			detail_content.setTextColor(Color.WHITE);
			detail_time.setTextColor(Color.WHITE);
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
