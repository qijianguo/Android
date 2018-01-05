package com.adc.shapingba;

import java.util.List;
import java.util.Map;

import com.adc.data.LoginState;
import com.adc.util.IsNumericUtil;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NormalListViewAdapter extends BaseAdapter{

	private Context context;
	private List<Map<String,Object>> listItems;
	private LayoutInflater listContainer;
	private int len1;//第1个属性字符串的最大长度
	private int len2;//第2个属性字符串的最大长度
	private int len3;//第2个属性字符串的最大长度
	private int text_size1;//第1个属性的字体大小
	private int text_size2;//第2个属性的字体大小
	private int text_size3;//第3个属性的字体大小
	private int colors[] = {
		Color.rgb(0, 203, 0),
		Color.rgb(248, 140, 23),
		Color.rgb(0, 154, 205)
	};
	
	public final class ListItemView{
//		public TextView normal_lv_tv_color_block;
		public TextView normal_lv_tv_rank;
		public TextView normal_lv_tv_district;
		public TextView normal_lv_tv_csite;
		public TextView normal_lv_tv_pm10;
	}
	
	public NormalListViewAdapter(Context context,List<Map<String, Object>> listItems,
			int len1,int len2,int len3,int text_size1,int text_size2,int text_size3){
		this.context = context;
		listContainer = LayoutInflater.from(context);
		this.listItems = listItems;
		this.len1 = len1;
		this.len2 = len2;
		this.len3 = len3;
		this.text_size1 = text_size1;
		this.text_size2 = text_size2;
		this.text_size3 = text_size3;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listItems.size();
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
		ListItemView listItemView = null;
		if(convertView == null){
			listItemView = new ListItemView();
			convertView = listContainer.inflate(R.layout.normal_list_item, parent, false);
//			listItemView.normal_lv_tv_color_block = (TextView) convertView.findViewById(R.id.normal_lv_tv_color_block);
			listItemView.normal_lv_tv_rank = (TextView) convertView.findViewById(R.id.normal_lv_tv_rank);
			listItemView.normal_lv_tv_district = (TextView) convertView.findViewById(R.id.normal_lv_tv_district);
			listItemView.normal_lv_tv_csite = (TextView) convertView.findViewById(R.id.normal_lv_tv_csite);
			listItemView.normal_lv_tv_pm10 = (TextView) convertView.findViewById(R.id.normal_lv_tv_pm10);
			convertView.setTag(listItemView);
		}else{
			listItemView = (ListItemView)convertView.getTag();
		}
		
		listItemView.normal_lv_tv_rank.setText((String)listItems.get(position).get("rank"));
		String str1 = (String)listItems.get(position).get("district_name");
		if(str1.length() > len1){
			str1 = str1.substring(0,len1);
		}
		listItemView.normal_lv_tv_district.setText(str1);
		listItemView.normal_lv_tv_district.setTextSize(text_size1);
		
		String str2 = (String)listItems.get(position).get("csite_name");
		if(str2.length() > len2){
			str2 = str2.substring(0,len2);
		}
		listItemView.normal_lv_tv_csite.setText(str2);
		listItemView.normal_lv_tv_csite.setTextSize(text_size2);

		String str3 = (String)listItems.get(position).get("content");
		if(str3.length() > len3){
			str3 = str3.substring(0,len3);
		}
		listItemView.normal_lv_tv_pm10.setText(str3);
		listItemView.normal_lv_tv_pm10.setTextSize(text_size3);
//
//		String color_type = (String) listItems.get(position).get("color_type");
//		if(IsNumericUtil.isInteger(color_type)){
//			int idx = Integer.valueOf(color_type);
//			//Log.i("heheda", "color_type="+color_type+",idx="+idx);
//			listItemView.normal_lv_tv_color_block.setBackgroundColor(colors[idx%colors.length]);
//		}
		return convertView;
	}

}
