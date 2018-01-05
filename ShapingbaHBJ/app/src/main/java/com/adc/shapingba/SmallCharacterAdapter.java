package com.adc.shapingba;

import java.util.ArrayList;

import com.adc.shapingba.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SmallCharacterAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private ArrayList<String> lists;

	public SmallCharacterAdapter(Context context, ArrayList<String> lists) {
		super();
		Log.i("heheda", "gouzao lists size=" + lists.size());
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
		String string = lists.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.small_character_list_item,
					null);
		}

		TextView small_character_textview = (TextView) convertView
				.findViewById(R.id.small_character_textview);
		small_character_textview.setText(string);
		small_character_textview.setTextColor(Color.BLACK);

		return convertView;
	}

}
