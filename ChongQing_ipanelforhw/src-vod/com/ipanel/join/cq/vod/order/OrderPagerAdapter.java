package com.ipanel.join.cq.vod.order;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.utils.Tools;

/*
 * @time:2016_03_31
 */
public class OrderPagerAdapter extends PagerAdapter {
	private Context context;
	private List<OrderInfo> list;
	private final static int PAGE_NUMBER = 2;// 每一页个数
	private static final int PAGE_ROW = 1;//一个viewpager放几行数据
	
	public OrderPagerAdapter(Context context, List<OrderInfo> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		if (list == null) {
			return 0;
		}
		return list.size() % PAGE_NUMBER == 0 ? list.size() / PAGE_NUMBER
				: list.size() / PAGE_NUMBER + 1;
	}

	@Override
	public float getPageWidth(int position) {
		return (PAGE_ROW + 0.0f) / PAGE_NUMBER;
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return view == obj;
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		View v = (View) object;
		container.removeView(v);
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		View itemView = null;
		if (itemView == null) {
			itemView = View.inflate(context, R.layout.vod_hotpageritem, null);
		}
		WeightGridLayout mWeightLayout = (WeightGridLayout) itemView
				.findViewById(R.id.hotgrid);
		mWeightLayout.setClipToPadding(false);
		mWeightLayout.setTag(position);
		mWeightLayout.setAdapter(new OrderWeightGridAdapter(position));
		itemView.setTag(position);
		container.addView(itemView);
		return itemView;
	}

	class OrderWeightGridAdapter extends WeightGridAdapter {

		int row;

		public OrderWeightGridAdapter(int row) {
			this.row = row;
		}

		@Override
		public int getCount() {
			int total = list.size() % PAGE_NUMBER == 0 ? list.size()
					/ PAGE_NUMBER : list.size() / PAGE_NUMBER + 1;
			if (row == total - 1) {
				return list.size() % PAGE_NUMBER == 0 ? PAGE_NUMBER : list
						.size() % PAGE_NUMBER;
			} else {
				return PAGE_NUMBER;
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			int index = row * PAGE_NUMBER + position;
			final OrderInfo item = list.get(index);
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.vod_order_item, parent, false);
			}
			ImageView image = (ImageView) convertView
					.findViewById(R.id.order_img);
			final TextView name = (TextView) convertView
					.findViewById(R.id.order_id);
			TextView price = (TextView)convertView.findViewById(R.id.order_price);
			TextView desc = (TextView)convertView.findViewById(R.id.order_desc);
			Button buy = (Button)convertView.findViewById(R.id.order_button);
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

				}
			});
			name.setText(item.orderName);
			price.setText(item.price);
			desc.setText(item.desc);
			buy.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Tools.showToastMessage(context, item.orderName+"购买接口开发中");
				}
			});
			ImageFetcher mFetcher = SharedImageFetcher
					.getNewFetcher(context, 3);
			mFetcher.loadImage(item.posterUrl, image);
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
			return PAGE_NUMBER;
		}

		@Override
		public int getYSize() {
			return PAGE_ROW;
		}

		@Override
		public int getYSpace() {
			return 35;
		}

		@Override
		public int getXSpace() {
			return 35;
		}

	}

}
