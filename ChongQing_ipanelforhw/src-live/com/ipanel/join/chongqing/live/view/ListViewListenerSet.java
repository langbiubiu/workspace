package com.ipanel.join.chongqing.live.view;

public class ListViewListenerSet {
	public interface AnimInStartListener {
		public void onAnimInStart();
	}

	public interface AnimOutStartListener {
		public void onAnimOutStart();
	}

	public interface AnimInEndListener {
		public void onAnimInEnd();
	}

	public interface AnimOutEndListener {
		public void onAnimOutEnd();
	}

	public interface ListOnEnterListener {
		public void onEnter(int focus);
	}

	public interface ListFocusChangeListener {
		public void onFocusChange(int focus);
	}

	public interface ListPreFocusChangeListener {
		public void onPreFocusChange(int focus);
	}

	public interface ListDataEmptyListener {
		public void onEmpty();
	}

	public interface ListDataLoadingListener {
		public void onLoading();
	}

	public interface ListKeyPressgListener {
		public boolean onKeyPress(int keyCode);
	}

	public interface ListDataLoadingCallBack {
		public void onLoadingComplete(Object o);
	}

	public interface ListDataLoadTimeUp {
		public void onTimeUp();
	}

	public interface ListKeyUpListener {
		public boolean onKeyUp(int keyCode);
	}

	public interface ListSetDataListener {
		public void onSetData(int size);
	}

	public interface ListOnFocusChangeFinish {
		public void onFinish();
	}
}
