package com.adc.shapingba;

import java.util.List;
import java.util.Map;

import com.adc.data.LoginState;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SpotRankListViewAdapter extends BaseAdapter{

	private Context context;
	private List<Map<String,Object>> listItems;
	private LayoutInflater listContainer;
	
	public final class ListItemView{
		public TextView spot_rank_lv_tv_provider;
		public TextView spot_rank_lv_tv_rank;
		public TextView spot_rank_lv_tv_district;
		public TextView spot_rank_lv_tv_csite;
		public TextView spot_rank_lv_tv_pm10;
	}
	
	public SpotRankListViewAdapter(Context context,List<Map<String, Object>> listItems){
		this.context = context;
		listContainer = LayoutInflater.from(context);
		this.listItems = listItems;
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
			convertView = listContainer.inflate(R.layout.spot_rank_list_item, null);
			listItemView.spot_rank_lv_tv_provider = (TextView) convertView.findViewById(R.id.spot_rank_lv_tv_provider);
			listItemView.spot_rank_lv_tv_rank = (TextView) convertView.findViewById(R.id.spot_rank_lv_tv_rank);
			listItemView.spot_rank_lv_tv_district = (TextView) convertView.findViewById(R.id.spot_rank_lv_tv_district);
			listItemView.spot_rank_lv_tv_csite = (TextView) convertView.findViewById(R.id.spot_rank_lv_tv_csite);
			listItemView.spot_rank_lv_tv_pm10 = (TextView) convertView.findViewById(R.id.spot_rank_lv_tv_pm10);
			convertView.setTag(listItemView);
		}else{
			listItemView = (ListItemView)convertView.getTag();
		}
		
		listItemView.spot_rank_lv_tv_rank.setText((String)listItems.get(position).get("rank"));
		listItemView.spot_rank_lv_tv_district.setText((String)listItems.get(position).get("districtName"));
		listItemView.spot_rank_lv_tv_csite.setText((String)listItems.get(position).get("csiteName"));
		
		if(listItems.get(position).get("VendorName") == null || listItems.get(position).get("VendorName").equals("null")){
			listItemView.spot_rank_lv_tv_provider.setBackgroundColor(Color.TRANSPARENT);
		}else{
			String VendorName = (String) listItems.get(position).get("VendorName");
			if(VendorName.equals("1")){
				listItemView.spot_rank_lv_tv_provider.setBackgroundResource(R.color.landfun);
			}else if(VendorName.equals("2")){
				listItemView.spot_rank_lv_tv_provider.setBackgroundResource(R.color.juzheng);
			}else {
				listItemView.spot_rank_lv_tv_provider.setBackgroundResource(R.color.xuedilong);
			}
		}
	
		String pm10_string = (String)listItems.get(position).get("pm10");
		//Log.i("heheda", (String)listItems.get(position).get("csiteName")+":"+pm10_string);
		double pm10_val = Double.valueOf(pm10_string);
		if(pm10_val < 0){
			if(LoginState.getIns().getEdition_type().equals("1")){
				//专业版权限可以查看异常种类
				if(pm10_val > -1.1){
					//pm10_val=-1.0 无数据
					pm10_string = "设备断电";				
				}else if(pm10_val > -2.1){
					//pm10_val=-2.0 数据过高
					pm10_string = "数据异常";
				}else if (pm10_val > -3.1) {
					//pm10_val=-3.0 数据高低
					pm10_string = "数据异常";
				}else {
					//pm10_val=-4.0
					pm10_string = "设备故障";
				}
				listItemView.spot_rank_lv_tv_pm10.setTextColor(Color.WHITE);
				listItemView.spot_rank_lv_tv_pm10.setBackgroundResource(R.drawable.pm10_rank_e);
			}else {
				//非专业版只显示"/"
				pm10_string = "/";
				listItemView.spot_rank_lv_tv_pm10.setTextColor(Color.rgb(0x52, 0xBD, 0x62));
				listItemView.spot_rank_lv_tv_pm10.setBackgroundResource(R.drawable.pm10_rank_d);
			}			
		}else{
			if(pm10_val <= 120){
				listItemView.spot_rank_lv_tv_pm10.setTextColor(Color.WHITE);
				listItemView.spot_rank_lv_tv_pm10.setBackgroundResource(R.drawable.pm10_rank_a);
			}else if (pm10_val <= 150) {
				listItemView.spot_rank_lv_tv_pm10.setTextColor(Color.WHITE);
				listItemView.spot_rank_lv_tv_pm10.setBackgroundResource(R.drawable.pm10_rank_b);
			}else {
				listItemView.spot_rank_lv_tv_pm10.setTextColor(Color.WHITE);
				listItemView.spot_rank_lv_tv_pm10.setBackgroundResource(R.drawable.pm10_rank_c);
			}
		}
		listItemView.spot_rank_lv_tv_pm10.setText(pm10_string);
		return convertView;
	}

}
