package cn.ipanel.android.widget;

public abstract class ArrayLoopPagerAdapter<T> extends ArrayPagerAdapter<T> {

	@Override
	public int getCount() {
		int real = super.getCount();
		if (real > 0)
			return Integer.MAX_VALUE;
		return real;
	}

	public int getRealCount() {
		return super.getCount();
	}
}
