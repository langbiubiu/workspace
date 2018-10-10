package ipanel.join.widget;

import ipanel.join.configuration.View;
import android.view.ViewGroup.LayoutParams;

public interface IConfigViewGroup extends IConfigView{
	public static final String PROPERTY_LAYOUT_PARAMS = "LayoutParams";
	public LayoutParams genConfLayoutParams(View data);
}
