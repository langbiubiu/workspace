package ipanel.join.widget;

import java.math.BigInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.ipanel.android.net.cache.JSONApiHelper;
import cn.ipanel.android.net.cache.JSONApiHelper.CallbackType;
import cn.ipanel.android.net.cache.JSONApiHelper.StringResponseListener;

import ipanel.join.configuration.Action;
import ipanel.join.configuration.Bind;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.Screen;
import ipanel.join.configuration.View;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.widget.AdapterView;

public class ListView extends android.widget.ListView implements IConfigView, android.widget.AdapterView.OnItemSelectedListener, android.widget.AdapterView.OnItemClickListener {
	private View mData;

	public ListView(Context context, View data) {
		super(context);
		this.mData = data;

		PropertyUtils.setCommonProperties(this, data);
		
		Bind bind = mData.getBindByName("selector");
		if(bind != null && bind.matchTarget(data.getId())){
			this.setSelector(new ColorDrawable(new BigInteger(bind.getValue().getvalue(),
					16).intValue()));
		}

		bind = mData.getBindByName("itemCanFocus");
		if(bind != null && bind.matchTarget(data.getId())){
			this.setItemsCanFocus(Boolean.parseBoolean(bind.getValue().getvalue()));
		}
		
		bind = mData.getBindByName("JsonListAdapter");
		if (bind != null) {
			try {
				JSONObject root = new JSONObject(bind.getValue().getvalue());
				String scid = root.getString("screenId");
				Screen sc = ConfigState.getInstance().getConfiguration()
						.findScreenById(scid);
				if (root.has("itemsUrl")) {
					loadItems(root.getString("itemsUrl"), sc);
				} else {
					JsonListAdapter adapter = new JsonListAdapter(
							root.getJSONArray("items"), sc, mData.getBind());
					setAdapter(adapter);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		setOnItemSelectedListener(this);
		setOnItemClickListener(this);
	}

	private void loadItems(String url, final Screen sc) {
		JSONApiHelper.callJSONAPI(getContext(), CallbackType.ForceUpdate, url,
				null, new StringResponseListener() {

					@Override
					public void onResponse(String content) {
						if (content != null) {
							try {
								JsonListAdapter adapter = new JsonListAdapter(
										new JSONArray(content), sc, mData
												.getBind());
								setAdapter(adapter);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}

					}
				});

	}

	@Override
	public View getViewData() {
		return mData;
	}

	@Override
	public void onAction(String type) {
		ActionUtils.handleAction(this, mData, type);

	}

	private boolean mShowFocusFrame = false;

	@Override
	public boolean showFocusFrame() {
		return mShowFocusFrame;
	}

	@Override
	public void setShowFocusFrame(boolean show) {
		mShowFocusFrame = show;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, android.view.View view,
			int position, long id) {
		ConfigState.getInstance().getFrameListener().updateFrame();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		ConfigState.getInstance().getFrameListener().updateFrame();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, android.view.View view,
			int position, long id) {
		ActionUtils.handleAction(this, mData, Action.EVENT_ONITEMCLICK, getItemAtPosition(position));
	}
}
