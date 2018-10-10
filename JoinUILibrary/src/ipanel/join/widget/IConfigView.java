package ipanel.join.widget;

import ipanel.join.configuration.View;

public interface IConfigView {
	public View getViewData();
	public void onAction(String type);
	public boolean showFocusFrame();
	public void setShowFocusFrame(boolean show);
}
