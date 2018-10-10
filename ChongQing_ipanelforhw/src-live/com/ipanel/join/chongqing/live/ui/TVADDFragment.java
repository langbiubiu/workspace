package com.ipanel.join.chongqing.live.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.base.BaseFragment;
import com.ipanel.join.chongqing.live.manager.StationManager;
import com.ipanel.join.chongqing.live.manager.UIManager;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.cq.vod.utils.Tools;

public class TVADDFragment extends BaseFragment {
	
	RelativeLayout head_shift_btn;  //��ͷ��
	RelativeLayout record_btn;		//¼��
	RelativeLayout series_btn;		//ѡ����
	RelativeLayout recommend_btn;   //����ӰƬ
	RelativeLayout favorite_btn;	//�ղ�Ƶ��
	RelativeLayout mine_btn;		//�ҵ�
	RelativeLayout jst_rl;			//����ͨ
	TextView record_text;
	TextView favorite_text;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		View popView = LayoutInflater.from(getLiveActivity()).inflate(R.layout.live_pop_tv_add_1, null);
		head_shift_btn = (RelativeLayout) popView.findViewById(R.id.head_shift_btn);   //��ͷ��
		record_btn = (RelativeLayout) popView.findViewById(R.id.record_btn);		   //¼��
		series_btn = (RelativeLayout) popView.findViewById(R.id.series_btn);		   //ѡ����
		recommend_btn = (RelativeLayout) popView.findViewById(R.id.recommend_btn);     //����ӰƬ
		favorite_btn = (RelativeLayout) popView.findViewById(R.id.favorite_btn);	   //�ղ�Ƶ��
		mine_btn = (RelativeLayout) popView.findViewById(R.id.mine_btn);			   //�ҵ�
		jst_rl = (RelativeLayout)popView.findViewById(R.id.live_jst);				   //����ͨ
		record_text = (TextView) popView.findViewById(R.id.text_tck2);
		favorite_text = (TextView) popView.findViewById(R.id.text_tck5);
		head_shift_btn.requestFocus();
		head_shift_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getLiveActivity().startShiftPlayByMode(-1, StationManager.SHIFT_TYPE_WATCH_HEAD);
			}
		});
		record_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		series_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getLiveActivity().getUIManager().showUI(UIManager.ID_UI_CQ_SERIES, null);
			}
		});
		recommend_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getLiveActivity().getUIManager().showUI(UIManager.ID_UI_RECOMEND, null);
			}
		});
		favorite_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				LiveChannel channel = getLiveActivity().getStationManager().getPlayChannel();
				if (getLiveActivity().getDataManager().isFavoriteChannel(channel)) {
					Toast.makeText(getLiveActivity(), "ȡ��Ƶ���ղ�", Toast.LENGTH_SHORT).show();
					favorite_text.setText("Ƶ���ղ�");
				} else {
					Toast.makeText(getLiveActivity(), "�ղسɹ�", Toast.LENGTH_SHORT).show();
					favorite_text.setText("���ղ�");
				}
				getLiveActivity().getDataManager().updateChannelFavorite(channel);
			}
		});
		mine_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
			}
		});
		jst_rl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Tools.showToastMessage(getLiveActivity(), "��Ŀ������");
			}
		});
		return popView;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		LiveChannel channel = getLiveActivity().getStationManager().getPlayChannel();
//		Program event = getLiveActivity().getDataManager().getChannelCurrentProgram(channel);
		if (getLiveActivity().getDataManager().isFavoriteChannel(channel))
			favorite_text.setText("���ղ�");
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataChange(int type, Object data) {
		// TODO Auto-generated method stub
		boolean success = false;
		if (data != null)
			success = (Boolean)data;
		switch (type) {
		case Constant.DATA_CHANGE_OF_CANCEL_FAVORITE_CHANNEL:
			if (success) {
				Toast.makeText(getLiveActivity(), "ȡ��Ƶ���ղسɹ�", Toast.LENGTH_SHORT).show();
				favorite_text.setText("Ƶ���ղ�");
			} else {
				Toast.makeText(getLiveActivity(), "ȡ��Ƶ���ղ�ʧ��", Toast.LENGTH_SHORT).show();
			}
			break;
		case Constant.DATA_CHANGE_OF_SET_FAVORITE_CHANNEL:
			if (success) {
				Toast.makeText(getLiveActivity(), "Ƶ���ղسɹ�", Toast.LENGTH_SHORT).show();
				favorite_text.setText("���ղ�");
			} else {
				Toast.makeText(getLiveActivity(), "Ƶ���ղ�ʧ��", Toast.LENGTH_SHORT).show();
			}
		default:
			break;
		}
	}

}
