package com.ipanel.join.cq.vod.player;

import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.ipanel.chongqing_ipanelforhw.R;

public class LoadingFragment extends BaseFragment {
	ImageView mImageView;
	Animation mRotate;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		ViewGroup container = createContainer(inflater, root,
				R.layout.vod_fragment_loading_view);
		mImageView = (ImageView) container.findViewById(R.id.loadingimage);

		return container;
	}

	@Override
	public void showFragment() {
		super.showFragment();
		mRotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		mRotate.setDuration(1000);
		mRotate.setRepeatCount(Integer.MAX_VALUE);
		mRotate.setInterpolator(new LinearInterpolator());
		mImageView.startAnimation(mRotate);
		mHandler.sendEmptyMessageDelayed(MessageColection.NAV_MESSAGE_LOAD_FINISH, 500);
		mHandler.sendEmptyMessageDelayed(MessageColection.NAV_MESSAGE_LOAD_TIMEUP, 10 * 1000);
	}
	
	@Override
	public void onDataChange(int type, Object o) {
		super.onDataChange(type, o);
		if (type == Constant.DATA_CHANGE_PREPARE_SUCCESS) {
			mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_PLAY,
					null);
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			getActivity().finish();
			return true;
		}
		return true;
	}
	
	@Override
	public void hideFragment() {
		super.hideFragment();
		mHandler.removeMessages(MessageColection.NAV_MESSAGE_LOAD_TIMEUP);
		mHandler.removeMessages(MessageColection.NAV_MESSAGE_LOAD_FINISH);
	}
	
	@Override
	protected void handleMessageLocal(Message msg) {
		Log.i("LoadingFragment","handleMessageLocal:" + msg.what);
		if(msg.what==MessageColection.NAV_MESSAGE_LOAD_TIMEUP){
			final SimplePlayerActivity activity=(SimplePlayerActivity) getActivity();
			activity.prompt.showDialogAndPostDelayed(" ˝æ›º”‘ÿ ß∞‹", new Runnable() {

				@Override
				public void run() {
					activity.finish();
				}
			}, 1500);
		}else if(msg.what==MessageColection.NAV_MESSAGE_LOAD_FINISH){
			final SimplePlayerActivity activity=(SimplePlayerActivity) getActivity();
			activity.startPreparePlay();
		}
		super.handleMessageLocal(msg);
	}

}
