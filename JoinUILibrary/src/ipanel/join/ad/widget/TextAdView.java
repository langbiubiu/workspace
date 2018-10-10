package ipanel.join.ad.widget;

import ipanel.join.ad.widget.TextAd.FlyEntry;
import ipanel.join.configuration.Bind;
import ipanel.join.widget.ActionUtils;
import ipanel.join.widget.IConfigView;
import ipanel.join.widget.PropertyUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsoluteLayout;
import cn.ipanel.android.util.IOUtils;
import cn.ipanel.android.util.JSONUtil;
import cn.ipanel.android.widget.ScrollTextView;

public class TextAdView extends ScrollTextView implements IConfigView {
	public static final String PROP_AD_URI = "AdUri";

	public TextAdView(Context context) {
		super(context);
	}

	public TextAdView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private static final String TAG = TextAdView.class.getSimpleName();
	private TextAd mTextAd;
	private Uri mAdUri;
	private String mAdRootUri;
	private int mIndex = 0;
	private FlyEntry mEntry;

	private int mLoopCount = 0;
	private int mLoopLimit = -1;

	ContentObserver mObserver = new ContentObserver(new Handler()) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			//if(getVisibility() == View.VISIBLE)
			updateAdData();
		}

	};

	OnScrollEndListener mScrollEndListener = new OnScrollEndListener() {

		@Override
		public void onScrollEnd() {
			if (mTextAd == null || mTextAd.flyads == null)
				return;
			mIndex++;
			if (mIndex >= mTextAd.flyads.flyEntrys.size()){
				mLoopCount++;
				if (mLoopLimit == -1 || mLoopCount < mLoopLimit)
					updateAdData();
				else{
					pauseScroll();
					setVisibility(View.GONE);
				}
			} else {
				showTextEntry();
			}
		}
	};

	public void setAdUri(String contentUri, int loopLimit) {
		this.mLoopLimit = loopLimit;
		setAdUri(contentUri);
	}

	public void setAdUri(String contentUri) {
		contentUri = contentUri.trim();
		Log.d(TAG, "setAdUri uri="+contentUri);
		mLoopCount = 0;
		getContext().getContentResolver().unregisterContentObserver(mObserver);
		Uri uri = Uri.parse(contentUri);
		mAdUri = uri;
		mAdRootUri = contentUri.substring(0, contentUri.lastIndexOf('/') + 1);
		getContext().getContentResolver().registerContentObserver(Uri.parse(mAdRootUri), true,
				mObserver);
		setOnScrollEndListener(mScrollEndListener);
		updateAdData();
	}

	protected void updateAdData() {
		try {
			InputStream is = getContext().getContentResolver().openInputStream(mAdUri);
			String json = new String(IOUtils.IS2ByteArray(is), "GBK");
			Log.d(TAG, mAdUri.toString());
			Log.d(TAG, json);
			if (json.trim().startsWith("var"))
				json = json.substring(json.indexOf('=') + 1).trim();
			mTextAd = JSONUtil.fromJSON(json, TextAd.class);
			mIndex = 0;
			setVisibility(View.VISIBLE);
			updateAdLayout();
			setupTextView();
			showTextEntry();
		} catch (Exception e) {
			pauseScroll();
			setVisibility(View.GONE);
			e.printStackTrace();
		}
	}

	private void setupTextView() {
		if (mTextAd == null || mTextAd.flyads == null)
			return;
		setSpeed(mTextAd.flyads.speed);
		if(TextAd.TYPE_BOTTOM_TO_TOP.equals(mTextAd.flyads.runType)){
			setScrollDirection(ScrollDirection.BOTTOM_TO_TOP);
		} else {
			setScrollDirection(ScrollDirection.RIGHT_TO_LEFT);
		}
		if (!TextUtils.isEmpty(mTextAd.flyads.fontColor)) {
			int color = Color.BLACK;
			try {
				color = Color.parseColor(mTextAd.flyads.fontColor);
			} catch (Exception e) {
				e.printStackTrace();
			}
			setTextColor(color);
		}
		setTextSize(TypedValue.COMPLEX_UNIT_PX, layoutFactor * Integer.parseInt(mTextAd.flyads.fontSize));
		if (!TextUtils.isEmpty(mTextAd.flyads.backgroundColor)) {
			int color = 0;
			try {
				color = Color.parseColor(mTextAd.flyads.backgroundColor);
			} catch (Exception e) {

			}
			if (!TextUtils.isEmpty(mTextAd.flyads.transparency)) {
				int alpha = Integer.parseInt(mTextAd.flyads.transparency);
				alpha = Math.max(0, alpha);
				alpha = Math.min(255, alpha);
				color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
			}
			setBackgroundColor(color);
		}
		if (!TextUtils.isEmpty(mTextAd.flyads.backgroundImage)) {
			String url = mAdRootUri + mTextAd.flyads.backgroundImage;
			try {
				Bitmap bmp = BitmapFactory.decodeStream(getContext().getContentResolver()
						.openInputStream(Uri.parse(url)));
				setBackgroundDrawable(new BitmapDrawable(getResources(), bmp));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void showTextEntry() {
		if (mTextAd == null || mTextAd.flyads == null || mTextAd.flyads.flyEntrys.size() <= mIndex)
			return;
		mEntry = mTextAd.flyads.flyEntrys.get(mIndex);
		setText(mEntry.content);
		startScroll(Math.max(0, mEntry.cycles - 1));
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		showTextEntry();
		if (mAdUri != null) {
			getContext().getContentResolver().registerContentObserver(Uri.parse(mAdRootUri), true,
					mObserver);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		pauseScroll();
		getContext().getContentResolver().unregisterContentObserver(mObserver);
		super.onDetachedFromWindow();
	}
	
	private boolean mEnableAutoLayout = false;
	private float layoutFactor = 1.0f;
	public void setAutoLayoutEnabled(boolean enable) {
		mEnableAutoLayout = enable;
		updateAdLayout();
	}

	public void setLayoutFactor(float factor){
		this.layoutFactor = factor;
		updateAdLayout();
	}

	public void updateAdLayout() {
		if (mEnableAutoLayout && getParent() instanceof AbsoluteLayout && mTextAd != null) {
			try {
				AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(
						(int) (Integer.parseInt(mTextAd.width) * layoutFactor),
						(int) (Integer.parseInt(mTextAd.height) * layoutFactor),
						(int) (Integer.parseInt(mTextAd.x) * layoutFactor),
						(int) (Integer.parseInt(mTextAd.y) * layoutFactor));
				setLayoutParams(lp);
				requestLayout();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public TextAdView(Context ctx, ipanel.join.configuration.View data) {
		this(ctx);
		this.mView = data;
		PropertyUtils.setCommonProperties(this, data);
		Bind bd = data.getBindByName(PROP_AD_URI);
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
