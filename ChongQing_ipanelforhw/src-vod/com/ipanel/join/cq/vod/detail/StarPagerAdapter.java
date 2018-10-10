package com.ipanel.join.cq.vod.detail;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki.Info.Star;
import com.ipanel.join.cq.vod.HWDataManager;

public class StarPagerAdapter extends PagerAdapter {
	private Context context;
	private List<Star> mData;

	public StarPagerAdapter(Context context, List<Star> mData) {
		this.context = context;
		this.mData = mData;
	}
	
	@Override
	public float getPageWidth(int position) {
		return (1 + 0.0f) / 2;  
	}
	
	@Override
	public int getCount() {
		return mData.size() % 5 == 0 ? mData.size() / 5 : mData.size() / 5 + 1 ;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		View itemView = null;
		if (itemView == null) {
			itemView = View.inflate(context, R.layout.vod_hotpageritem, null);
		}
		WeightGridLayout mWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.hotgrid);
		mWeightLayout.setClipToPadding(false);
		mWeightLayout.setTag(position);
		mWeightLayout.setAdapter(new VODWeightGridAdapter(position));
		itemView.setTag(position);
		container.addView(itemView);
		return itemView;
	}
	
	class VODWeightGridAdapter extends WeightGridAdapter {

		int row;

		public VODWeightGridAdapter(int row) {
			this.row = row;
		}

		@Override
		public int getCount() {
			int total =  mData.size() % 5 == 0 ? mData.size() / 5 : mData.size() / 5 + 1;
			if(row == total -1){
				return mData.size() % 5 == 0 ? 5 : mData.size() % 5;
			}else{
				return 5;
			}
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			int index = row * 5 + position;
			final Star item = mData.get(index);
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.vod_hot_element3, parent, false);
			}
			ImageView image = (ImageView) convertView.findViewById(R.id.film_img);
			final TextView name = (TextView) convertView.findViewById(R.id.film_name);
			image.setBackgroundResource(R.drawable.default_poster);
			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						name.setSelected(true);
					} else {
						name.setSelected(false);
					}
				}
			});
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					HWDataManager.openStarShowActivity(context, item.getId());
				}
			});
			name.setText(item.getTitle());
			ImageFetcher mFetcher = SharedImageFetcher.getSharedFetcher(context);
			mFetcher.loadImage(item.getAvatar(), image);
			return convertView;
		}

		@Override
		public int getChildXSize(int position) {
			return 1;
		}

		@Override
		public int getChildYSize(int position) {
			return 1;
		}

		@Override
		public int getXSize() {
			return 5;
		}

		@Override
		public int getYSize() {
			return 1;
		}

		@Override
		public int getYSpace() {
			return 35;
		}

		@Override
		public int getXSpace() {
			return 45;
		}

	}
}
