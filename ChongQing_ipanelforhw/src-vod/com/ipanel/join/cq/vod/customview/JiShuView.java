package com.ipanel.join.cq.vod.customview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;

public class JiShuView extends JTextListView {
	private int jishu;
	private int per=10;
	private boolean isReverted = false;//逆序or正序
	public JiShuView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public JiShuView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	@Override
	public View getListView(int position, View convertView) {
		if (convertView == null) {
			convertView = tmpView;
		}
		TextView view = (TextView) convertView;
		view.setText(getShowItem(position,isReverted));
		if (position == this.getCurrentIndex()) {
			if(hasFocus()){
				view.setTextColor(Color.parseColor("#000000"));
				convertView.setBackgroundResource(R.drawable.vod_background);
			}else{
				view.setTextColor(Color.parseColor("#ffb400"));
				convertView.setBackgroundResource(R.drawable.translucent_background);
			}
		} else {
			view.setTextColor(Color.parseColor("#bebebe"));
			convertView.setBackgroundResource(R.drawable.translucent_background);
		}
		return view;
	}

	@Override
	public int getDataCount() {
		return jishu%per==0?jishu/per:jishu/per+1;
	}
	
	public void setShow(int jishu,int per,boolean isReverted){
		this.jishu=jishu;
		this.per=per;
		this.isReverted = isReverted;
		init();
	}
	@Override
	public void init() {
		layout_flag=true;
		spacing = (int) context.getResources().getDimension(
				R.dimen.h_list_item_spacing);
		padding = (int) context.getResources().getDimension(
				R.dimen.h_list_item_padding);
		font_scale = 30;
		list_item_height = 82;
		list_item_width = 185;
		defaultAnimDelay=0;
		super.init();
	}
	/**
	 * 
	 * @param position
	 * @param isReverted 是否逆序
	 * @return 
	 */
	private String getShowItem(int position,boolean isReverted){
		if(isReverted){
//			if(position==this.getDataCount()-1){
//				int yushu=jishu % per;
//				if(yushu==0){
//					return position*per+1+"-"+(position+1)*per;
//				}else{
//					return position*per+1+"-"+(position*per+yushu);
//
//				}
//			}else{
//				return position*per+1+"-"+(position+1)*per;
//			}
			if(position == this.getDataCount()-1){//最后一行
				return jishu-position * per +"-" + 1;
			}else{
				return jishu-position * per +"-"+(jishu-(position + 1) * per + 1);
			}
		}else{
			if(position==this.getDataCount()-1){
				int yushu=jishu%per;
				if(yushu==0){
					return position*per+1+"-"+(position+1)*per;
				}else{
					return position*per+1+"-"+(position*per+yushu);

				}
			}else{
				return position*per+1+"-"+(position+1)*per;
			}
		}
	}
}
