package ipanel.join.ad.widget;

import ipanel.join.ad.widget.ImageAd.ImageEntry;
import ipanel.join.configuration.Bind;
import ipanel.join.widget.ActionUtils;
import ipanel.join.widget.IConfigView;
import ipanel.join.widget.PropertyUtils;

import java.io.InputStream;

import cn.ipanel.android.util.IOUtils;
import cn.ipanel.android.util.JSONUtil;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

@SuppressWarnings("deprecation")
public class ImageAdView extends ImageSwitcher implements IConfigView {
	public static final String PROP_AD_URI = "AdUri";
	public static final String PROP_RANDOM_ANIMATION = "randomAnimation";

	public interface OnLoopCompleteListener {
		public void onLoopComplete(int count);
	}

	public interface OnImageSwitchListener {
		public void beforeImageSwitch(ImageAdView adView, Uri uri);
	}

	private static final String TAG = ImageAdView.class.getSimpleName();
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (mImageAd == null || mImageAd.imgAds == null)
					return;
				mIndex++;
				if (mIndex >= mImageAd.imgAds.imgEntrys.size()) {
					mLoopCount++;
					mIndex = 0;
					if (mLoopLimit == -1 || mLoopCount < mLoopLimit)
						updateAdData();
					else {
						if (mLoopCompleteListener != null)
							mLoopCompleteListener.onLoopComplete(mLoopCount);
						else
							showDefaultImage();
					}
				} else {
					showImage(true);
				}
				break;
			}
		}

	};

	public ImageAdView(Context context) {
		super(context);
		init();
	}

	public ImageAdView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	protected void init() {
		this.setFactory(new ViewFactory() {

			@Override
			public View makeView() {
				ImageView iv = new ImageView(getContext());
				iv.setScaleType(ScaleType.CENTER_CROP);
				iv.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT));
				return iv;
			}
		});

		//
		// this.setWillNotDraw(false);
		// mPaint.setColor(0xFFCB1B1D);
		// mPaint.setTextAlign(Align.CENTER);
		// mPaint.setFakeBoldText(true);
		// mPaint.setTextSize(48);
		// mPaint.setShadowLayer(30, 0, 2, Color.WHITE);
	}

	// TextPaint mPaint = new TextPaint();
	//
	// @Override
	// public void draw(Canvas canvas) {
	// super.draw(canvas);
	//
	// canvas.drawText("¹ã¸æÎ»", getWidth()/2, getHeight()/2, mPaint);
	//
	// }

	private ImageAd mImageAd;
	private Uri mAdUri;
	private String mAdRootUri;
	private int mIndex = 0;

	private ImageEntry mEntry;

	private int mLoopCount = 0;
	private int mLoopLimit = -1;

	private boolean mRandomAnimation = true;

	private boolean mEnableAutoLayout = false;
	
	private Drawable mDefaultImage = null;

	ContentObserver mObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (getVisibility() == View.VISIBLE)
				updateAdData();
		}

	};
	
	public void setDefaultDrawable(Drawable d){
		this.mDefaultImage = d;
		updateAdData();
	}

	public void setAutoLayoutEnabled(boolean enable) {
		mEnableAutoLayout = enable;
		updateAdLayout();
	}

	public void setRandomAnimationEnabled(boolean enable) {
		mRandomAnimation = enable;
	}

	public void setAdUri(String contentUri, int loopLimit) {
		this.mLoopLimit = loopLimit;
		setAdUri(contentUri);
	}

	public void setAdUri(String contentUri) {
		contentUri = contentUri.trim();
		Log.d(TAG, "setAdUri uri="+contentUri);
		mLoopCount = 0;
		mLastJson = null;
		getContext().getContentResolver().unregisterContentObserver(mObserver);
		Uri uri = Uri.parse(contentUri);
		mAdUri = uri;
		mAdRootUri = contentUri.substring(0, contentUri.lastIndexOf('/') + 1);
		getContext().getContentResolver().registerContentObserver(Uri.parse(mAdRootUri), true,
				mObserver);
		updateAdData();
	}

	private String mLastJson;

	public ImageAd getAdData(){
		return mImageAd;
	}
	
	protected void updateAdData() {
		Log.d(TAG, "updateAdData");
		try {
			InputStream is = getContext().getContentResolver().openInputStream(mAdUri);
			String json = new String(IOUtils.IS2ByteArray(is), "GBK");
			Log.d(TAG, json);
			if (json.trim().startsWith("var"))
				json = json.substring(json.indexOf('=') + 1).trim();
			if (json.equals(mLastJson) && mImageAd != null && mImageAd.imgAds != null
					&& mImageAd.imgAds.imgEntrys.size() <= mIndex) {
				Log.d(TAG, "updateAdData no data change");
				mHandler.removeMessages(0);
				if (mImageAd.imgAds.imgEntrys.size() > 1)
					mHandler.sendEmptyMessageDelayed(0, mEntry.pauseTime * 1000);
				return;
			}
			mImageAd = JSONUtil.fromJSON(json, ImageAd.class);
			mIndex = 0;
			updateAdLayout();
			setVisibility(View.VISIBLE);
			if(getBackground() != null)
				getBackground().setAlpha(0);
			Log.d(TAG, "updateAdData with data change");
			showImage(false);
			mLastJson = json;
		} catch (Exception e) {
			Log.e(TAG, "updateAdData exception ");
			showDefaultImage();
			mLastJson = null;
		}
	}
	
	void showDefaultImage(){
		if(mDefaultImage == null && getBackground() != null)
			getBackground().setAlpha(255);
		if(getNextView() != null)
			setImageDrawable(mDefaultImage);
	}

	public void updateAdLayout() {
		if (mEnableAutoLayout && getParent() instanceof AbsoluteLayout && mImageAd != null) {
			try {
				AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(
						Integer.parseInt(mImageAd.width), Integer.parseInt(mImageAd.height),
						Integer.parseInt(mImageAd.x), Integer.parseInt(mImageAd.y));
				setLayoutParams(lp);
				requestLayout();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private OnImageSwitchListener mImageSwitchListener;

	public void setOnImageSwitchListener(OnImageSwitchListener listener) {
		this.mImageSwitchListener = listener;
	}

	private OnLoopCompleteListener mLoopCompleteListener;

	public void setOnLoopCompleteListener(OnLoopCompleteListener listener) {
		this.mLoopCompleteListener = listener;
	}

	protected void showImage(boolean animate) {
		mHandler.removeMessages(0);
		if (mImageAd == null || mImageAd.imgAds == null
				|| mImageAd.imgAds.imgEntrys.size() <= mIndex)
			return;
		mEntry = mImageAd.imgAds.imgEntrys.get(mIndex);
		String url = mAdRootUri + mEntry.src;
		Log.d(TAG, "show image animate=" + animate + ", uri: " + url);
		Uri uri = Uri.parse(url);
		if (mRandomAnimation)
			SwitchAnimations.setRandomAnimation(this);
		if (mImageSwitchListener != null)
			mImageSwitchListener.beforeImageSwitch(this, uri);
		if (animate) {
			ImageView imageView = (ImageView) getNextView();
			imageView.setImageDrawable(null);
			setImageURI(uri);
		} else {
			ImageView imageView = (ImageView) getCurrentView();
			imageView.setImageDrawable(null);
			imageView.setImageURI(uri);
		}
		Log.d(TAG, "show image end uri = " + url);
		if (mImageAd.imgAds.imgEntrys.size() > 1)
			mHandler.sendEmptyMessageDelayed(0, mEntry.pauseTime * 1000);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		updateAdData();
		if (mAdUri != null) {
			getContext().getContentResolver().registerContentObserver(Uri.parse(mAdRootUri), true,
					mObserver);
		}
	}

//	@Override
//	public void setBackgroundDrawable(Drawable d) {
//		this.mDefaultImage = d;
//		super.setBackgroundDrawable(null);
//		updateAdData();
//	}
//
//	@Override
//	public Drawable getBackground() {
//		return mDefaultImage;
//	}

	@Override
	protected void onDetachedFromWindow() {
		mHandler.removeMessages(0);
		getContext().getContentResolver().unregisterContentObserver(mObserver);
		super.onDetachedFromWindow();
	}

	public ImageAdView(Context ctx, ipanel.join.configuration.View data) {
		this(ctx);
		this.mView = data;
		PropertyUtils.setCommonProperties(this, data);

		Bind bd = data.getBindByName(PROP_RANDOM_ANIMATION);
		if (bd != null) {
			setRandomAnimationEnabled(Boolean.parseBoolean(bd.getValue().getvalue()));
		}

		bd = data.getBindByName(PROP_AD_URI);
		if (bd != null) {
			setAdUri(bd.getValue().getvalue());
		}
	}

	private ipanel.join.configuration.View mView;

	private boolean mShowFocusFrame = false;

	@Override
	public ipanel.join.configuration.View getViewData() {
		return mView;
	}

	@Override
	public void onAction(String type) {
		ActionUtils.handleAction(this, mView, type);

	}

	@Override
	public boolean showFocusFrame() {
		return mShowFocusFrame;
	}

	@Override
	public void setShowFocusFrame(boolean show) {
		this.mShowFocusFrame = show;
	}

}
