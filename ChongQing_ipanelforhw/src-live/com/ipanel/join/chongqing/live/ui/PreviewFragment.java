package com.ipanel.join.chongqing.live.ui;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.base.BaseFragment;
import com.ipanel.join.chongqing.live.data.NoAuthChannel;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWDataManagerImpl;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.portal.TeeveeWidgetHolder;

public class PreviewFragment extends BaseFragment {
	
	private TeeveeWidgetHolder tv;
	
	TextView channelName, info, price;
	FrameLayout tv_layout;
	
	Handler mHandler = new Handler() {
		
		public void dispatchMessage(Message msg) {
			Bundle bundle = msg.getData();
			switch (msg.what) {
			case 1:
				String freq = bundle.getString("frequency");
				String prog = bundle.getString("previewid");
				String name = bundle.getString("name");
				String jianjie = bundle.getString("info");
				String order_price = bundle.getString("price");
				tv = new TeeveeWidgetHolder(tv_layout.getContext(), freq, prog);
				tv_layout.setFocusable(false);
				tv_layout.addView(tv);
				
				tv.initRemoteView(tv.getContext());
				tv.playProgram();
				tv.updateTvRect();
				
				channelName.setText(name);
				info.setText(jianjie);
				price.setText(order_price);
				break;
			}
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.live_fragment_preview, root, false);
		tv_layout = (FrameLayout) vg.findViewById(R.id.tv);
		channelName = (TextView) vg.findViewById(R.id.channelName);
		info = (TextView) vg.findViewById(R.id.info);
		price = (TextView) vg.findViewById(R.id.price);
		return vg;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		LiveChannel channel = getLiveActivity().getStationManager().getPlayChannel();
		if (channel != null) {
			List<NoAuthChannel> list = ((HWDataManagerImpl)getLiveActivity().getDataManager()).noAuthList;
			if (list != null && !list.isEmpty()) {
				for (NoAuthChannel ch : list) {
					if (ch.getServiceId().equals(channel.getChannelKey().getProgram()+"")) {
						Bundle bundle = new Bundle();
						bundle.putString("frequency", ch.getFrequency());
						bundle.putString("previewid", ch.getPreviewid());
						bundle.putString("name", ch.getName());
						bundle.putString("info", ch.getInfo());
						bundle.putString("price", ch.getPrice());
						Message msg = new Message();
						msg.setData(bundle);
						msg.what = 1;
						mHandler.sendMessage(msg);
					}
				}
			}
		}
		
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub
		if (tv.hostView != null) {
			tv.hostView.toChannel("channelId://-1");
		}
		tv.deleteWidget(false);
	}

	@Override
	public void onDataChange(int type, Object data) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_2:
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_7:
		case KeyEvent.KEYCODE_8:
		case KeyEvent.KEYCODE_9:
			return false;
		}
		return true;
	}

}
