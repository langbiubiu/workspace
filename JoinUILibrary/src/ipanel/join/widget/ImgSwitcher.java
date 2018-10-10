package ipanel.join.widget;

import org.json.JSONArray;
import org.json.JSONException;

import cn.ipanel.android.net.imgcache.SharedImageFetcher;

import ipanel.join.ad.widget.SwitchAnimations;
import ipanel.join.ad.widget.SwitchAnimations.AnimationType;
import ipanel.join.configuration.Bind;
import ipanel.join.configuration.View;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class ImgSwitcher extends ImageSwitcher implements IConfigView {

	public static final String PROP_IMAGELIST = "imagelist";
	public static final String PROP_SCALE_TYPE = "scaleType";
	public static final String PROP_SWITCH_DELAY = "switchDelay";
	public static final String PROP_SWITCH_ANIMATION = "switchAnimation";

	private int mSwitchDelay = 10000;

	public ImgSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		ImageView img1 = new ImageView(getContext());
		ImageView img2 = new ImageView(getContext());
		LayoutParams lp = new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		addView(img1, lp);
		addView(img2, lp);

		setInAnimation(getContext(), android.R.anim.fade_in);
		setOutAnimation(getContext(), android.R.anim.fade_out);
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				switchImage();
				break;
			}
		}

	};

	@Override
	protected void onAttachedToWindow() {
		mHandler.sendEmptyMessageDelayed(0, mSwitchDelay);
		super.onAttachedToWindow();
	}

	protected void switchImage() {
		if (mImgArray != null && mImgArray.length() > 0) {
			showNext();
			mIndex++;
			if (mIndex >= mImgArray.length())
				mIndex = 0;
			postDelayed(new Runnable() {

				@Override
				public void run() {
					try {
						SharedImageFetcher.getSharedFetcher(getContext()).loadImage(
								mImgArray.getString(mIndex), getNextView());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}, 1000);
			mHandler.sendEmptyMessageDelayed(0, mSwitchDelay);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		mHandler.removeMessages(0);
		super.onDetachedFromWindow();
	}

	private View mData;

	private JSONArray mImgArray;

	private int mIndex = 0;

	public ImgSwitcher(Context ctx, View data) {
		super(ctx);
		init();
		this.mData = data;

		bindProperty(ctx, data);

		if (mImgArray != null && mImgArray.length() > 0) {
			try {
				SharedImageFetcher.getSharedFetcher(getContext()).loadImage(
						mImgArray.getString(mIndex), getCurrentView());
				SharedImageFetcher.getSharedFetcher(getContext()).loadImage(
						mImgArray.getString(getNextIndex()), getNextView());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int getNextIndex() {
		int next = mIndex + 1;
		if (next >= mImgArray.length()) {
			next = 0;
		}
		return next;
	}

	private void bindProperty(Context ctx, View data) {
		PropertyUtils.setCommonProperties(this, data);

		Bind bd = data.getBindByName(PROP_IMAGELIST);
		if (bd != null) {
			try {
				mImgArray = new JSONArray(bd.getValue().getvalue());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		bd = data.getBindByName(PROP_SCALE_TYPE);
		if (bd != null) {
			ScaleType scaleType = ScaleType.valueOf(bd.getValue().getvalue().toUpperCase());
			ImageView img = (ImageView) getCurrentView();
			img.setScaleType(scaleType);
			img = (ImageView) getNextView();
			img.setScaleType(scaleType);
		}

		bd = data.getBindByName(PROP_SWITCH_DELAY);
		if (bd != null) {
			mSwitchDelay = Integer.parseInt(bd.getValue().getvalue());
		}

		bd = data.getBindByName(PROP_SWITCH_ANIMATION);
		if (bd != null) {
			AnimationType type = AnimationType.valueOf(bd.getValue().getvalue());
			setInAnimation(SwitchAnimations.getInAnimation(getContext(), type));
			setOutAnimation(SwitchAnimations.getOutAnimation(getContext(), type));
		}
	}

	public void setWitchDelay(int delay) {
		this.mSwitchDelay = delay;
	}

	@Override
	public View getViewData() {
		return mData;
	}

	@Override
	public void onAction(String type) {
		ActionUtils.handleAction(this, mData, type);
	}

	private boolean mShowFocusFrame;

	@Override
	public boolean showFocusFrame() {
		return mShowFocusFrame;
	}

	@Override
	public void setShowFocusFrame(boolean show) {
		this.mShowFocusFrame = show;
	}

}
