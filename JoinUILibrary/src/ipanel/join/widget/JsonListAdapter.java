package ipanel.join.widget;

import java.util.List;

import ipanel.join.configuration.Action;
import ipanel.join.configuration.Bind;
import ipanel.join.configuration.Screen;
import ipanel.join.configuration.Value;
import ipanel.join.configuration.ViewInflater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class JsonListAdapter extends BaseAdapter {
	JSONArray mJsonArray;
	Screen mScreen;
	List<Bind> extBinds;

	public JsonListAdapter(JSONArray jsa, Screen sc, List<Bind> extBinds){
		this.mJsonArray = jsa;
		this.mScreen = sc;
		this.extBinds = extBinds;
	}
	
	@Override
	public int getCount() {
		return mJsonArray.length();
	}

	@Override
	public JSONObject getItem(int position) {
		try {
			return mJsonArray.getJSONObject(position);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		bindFields(position);
		if(convertView == null){
			convertView = ViewInflater.inflateView(parent.getContext(), null, mScreen.getView(), extBinds);
		} else {
			bindSubViews(convertView);
		}
		bindClickEvent(convertView, getItem(position));
		return convertView;
	}
	
	private void bindClickEvent(View root, final JSONObject item) {
		if(root instanceof IConfigView){
			final ipanel.join.configuration.View data = ((IConfigView)root).getViewData();
			if(data.hasClickAction()){
				root.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						ActionUtils.handleAction(v, data, Action.EVENT_ONCLICK, item);
						
					}
				});
			}
		}
		if(root instanceof ViewGroup){
			ViewGroup vg = (ViewGroup) root;
			int count = vg.getChildCount();
			for(int i=0;i<count;i++){
				bindClickEvent(vg.getChildAt(i), item);
			}
		}
	}

	private void bindFields(int position){
		for(Bind nb: extBinds){
			if(nb.getTarget() != null && Value.TYPE_FIELD.equals(nb.getValue().getType())){
				JSONObject item = getItem(position);
				String fieldName = nb.getValue().getFieldName();
				if(item != null && item.has(fieldName)){
					try {
						nb.getValue().setvalue(item.get(fieldName).toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void bindSubViews(View root){
		if(root instanceof IConfigRebind){
			((IConfigRebind)root).rebind(extBinds);
		}
		if(root instanceof ViewGroup){
			ViewGroup vg = (ViewGroup) root;
			int count = vg.getChildCount();
			for(int i=0;i<count;i++){
				bindSubViews(vg.getChildAt(i));
			}
		}
	}

}
