package cn.ipanel.android.util;

import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.TextView;

public class NumberInputGroup {

	public interface OnInputCompleteListener {
		public void onInputComplete(NumberInputGroup group);
	}

	public boolean disableEdit = false;

	TextView[] digits;

	View.OnClickListener clickListener;

	View.OnFocusChangeListener focusListener;

	OnKeyListener keyListener;

	OnInputCompleteListener completeListener;

	public NumberInputGroup(View root, int... ids) {
		if (ids != null) {
			digits = new TextView[ids.length];
			for (int i = 0; i < ids.length; i++) {
				digits[i] = (TextView) root.findViewById(ids[i]);
			}
			init();
		}
	}

	public NumberInputGroup(TextView... digits) {
		this.digits = digits;
		if (digits != null) {
			init();
		}
	}

	public void setClickListener(View.OnClickListener l) {
		this.clickListener = l;
	}

	public void setOnKeyListener(OnKeyListener l) {
		this.keyListener = l;
	}

	public void setOnFocusChangeListener(View.OnFocusChangeListener l) {
		this.focusListener = l;
	}

	public void setOnInputCompleteListener(OnInputCompleteListener l) {
		this.completeListener = l;
	}

	View.OnClickListener _clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (clickListener != null)
				clickListener.onClick(v);

		}
	};

	View.OnFocusChangeListener _focusListener = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (disableEdit && hasFocus) {
				moveToLast();
			}
			if (focusListener != null)
				focusListener.onFocusChange(v, hasFocus);

		}
	};

	OnKeyListener _keyListener = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyListener != null && keyListener.onKey(v, keyCode, event))
				return true;

			if (v instanceof TextView) {
				TextView tv = (TextView) v;
				switch (keyCode) {
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
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						CharSequence old = tv.getText();
						tv.setText(String.valueOf(keyCode - KeyEvent.KEYCODE_0));
						if (TextUtils.isEmpty(tv.getText())) {
							tv.setText(old);
						} else {
							if (!moveToNext() && completeListener != null) {
								completeListener.onInputComplete(NumberInputGroup.this);
							}
						}
					}
					break;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					if (!disableEdit)
						return false;
					break;
				case KeyEvent.KEYCODE_DPAD_LEFT:
					if (!disableEdit)
						return false;
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						deleteLast();
					}
					break;
				case KeyEvent.KEYCODE_DPAD_UP:
					if (disableEdit)
						return false;
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						for (int i = 1; i < 10; i++) {
							if (changeValue(tv, i))
								break;
						}
					}
					break;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					if (disableEdit)
						return false;
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						for (int i = -1; i > -10; i--) {
							if (changeValue(tv, i))
								break;
						}
					}
					break;
				default:
					return false;
				}
				return true;
			}

			return false;
		}
	};

	private void init() {

		for (TextView et : digits) {
			et.setFilters(new InputFilter[] { new InputFilterMinMax(0, 9) });
			et.setOnKeyListener(_keyListener);
			et.setOnClickListener(_clickListener);
			et.setFocusable(true);
			et.setFocusableInTouchMode(true);
			et.setOnFocusChangeListener(_focusListener);
		}

	}

	public boolean moveToFirst() {
		return moveTo(0);
	}

	public TextView[] getTextViews() {
		return digits;
	}

	public TextView getTextView(int index) {
		if (digits != null && index < digits.length && index >= 0)
			return digits[index];
		return null;
	}

	public boolean moveTo(int index) {
		if (digits != null && digits.length > index) {
			digits[index].requestFocus();
			return true;
		}
		return false;
	}

	public boolean moveToLast() {
		if (digits != null) {
			boolean foundLast = false;
			View last = null;
			for (TextView tv : digits) {
				foundLast = !TextUtils.isEmpty(tv.getText());
				last = tv;
				if (!foundLast) {
					break;
				}
			}
			if (last != null) {
				if (!last.hasFocus())
					last.requestFocus();
				return true;
			}
		}
		return false;
	}

	public boolean deleteLast() {
		boolean foundLast = false;
		if (digits != null) {
			for (int i = digits.length - 1; i >= 0; i--) {
				foundLast = !TextUtils.isEmpty(digits[i].getText());
				if (foundLast) {
					digits[i].setText("");
					digits[i].requestFocus();
					break;
				}
			}
		}
		return foundLast;
	}

	public boolean moveToNext() {
		if (digits != null) {
			boolean foundFocus = false;
			for (TextView tv : digits) {
				if (foundFocus) {
					tv.requestFocus();
					return true;
				}
				foundFocus = tv.isFocused();
			}
		}
		return false;
	}

	protected boolean changeValue(TextView tv, int i) {
		int val = 0;
		try {
			val = Integer.parseInt(tv.getText().toString());
		} catch (Exception e) {
		}
		int origianl = val;
		val = (val + i) % 10;
		if (val < 0)
			val += 10;
		tv.setText(String.valueOf(val));
		if (TextUtils.isEmpty(tv.getText())) {
			tv.setText(String.valueOf(origianl));
			return false;
		}
		return true;
	}
	
	public void clear(){
		if(digits != null){
			for(TextView tv : digits){
				tv.setText("");
			}
		}
	}

	public void setNumbers(String str) {
		if (str == null)
			str = "";
		if (digits != null) {
			for (int i = digits.length - 1; i >= 0; i--) {
				int idx = str.length() - 1 - i;
				if (idx >= 0)
					digits[i].setText(String.valueOf(str.charAt(i)));
				else
					digits[i].setText("0");
				if (TextUtils.isEmpty(digits[i].getText())) {
					digits[i].setText("0");
				}
			}
		}
	}

	public String getNumbers() {
		return getNumbers(true);
	}

	public String getNumbers(boolean appendZero) {
		StringBuffer sb = new StringBuffer();
		if (digits != null) {
			for (TextView tv : digits) {
				CharSequence text = tv.getText();
				if (appendZero && TextUtils.isEmpty(text))
					sb.append(0);
				else
					sb.append(text);
			}
		}
		return sb.toString();
	}
}
