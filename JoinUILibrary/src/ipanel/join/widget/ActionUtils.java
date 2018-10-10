package ipanel.join.widget;

import java.lang.reflect.Field;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.ipanel.android.Logger;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.FocusFinder;
import android.view.ViewGroup;
import ipanel.join.configuration.Action;
import ipanel.join.configuration.Bind;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.Screen;
import ipanel.join.configuration.UnhandledActionException;
import ipanel.join.configuration.Utils;
import ipanel.join.configuration.Value;
import ipanel.join.configuration.View;

public class ActionUtils {
	public static final String PROP_INTENT = "intent";

	public static class FragmentAnimationTag{
		int inAnimation;
		int outAnimation;
		
		public FragmentAnimationTag(int inAnim, int outAnim){
			this.inAnimation = inAnim;
			this.outAnimation = outAnim;
		}
	}
	// 判断View的事件：Intent 事件 和 replace事件
	public static void handleAction(android.view.View view, View data,
			String type){
		handleAction(view, data, type, null);
	}
	
	public static void handleAction(android.view.View view, View data,
			String type, Object extraData) {
		Logger.d(" " + data.getId() + " " + type);
		int handleCount = 0;
		for (Action act : data.getAction()) {
			if (type.equals(act.getEvent())) {
				if (Action.OP_INTENT.equals(act.getOperation())) {
					handleIntent(view, act, extraData);
					handleCount++;
				} else if (Action.OP_REPLACE.equals(act.getOperation())) {
					handleReplace(view, act);
					handleCount++;
				} else if (Action.OP_ADD.equals(act.getOperation())) {
					handleAdd(view, act);
					handleCount++;
				} else if (Action.OP_OPEN.equals(act.getOperation())){
					handleOpen(view, act);
					handleCount++;
				} else if (Action.OP_CLOSE.equals(act.getOperation())){
					handleClose(view, act);
					handleCount++;
				}
			}
		}
		if(handleCount == 0)
			ConfigState.getInstance().notifyException(new UnhandledActionException(type));
	}

	private static void handleReplace(android.view.View view, Action act) {
		if (view.getContext() instanceof FragmentActivity) {
			FragmentActivity activity = (FragmentActivity) view.getContext();
			android.view.ViewGroup container = (ViewGroup) Utils.findViewByConfigId(
					view.getRootView(), (String) act.getContainer());
			Screen target = ConfigState.getInstance().getConfiguration()
					.findScreenById((String) act.getTarget());
			if (container != null && target != null) {
				Logger.d("handleReplace target: "+target.getId());
				try{
					try {
						activity.getSupportFragmentManager().popBackStack(null,
								FragmentManager.POP_BACK_STACK_INCLUSIVE);
					} catch (Exception e) {
						e.printStackTrace();
					}
					FragmentTransaction transaction = activity
							.getSupportFragmentManager().beginTransaction();
					if (view.getTag() instanceof FragmentAnimationTag) {
						FragmentAnimationTag tag = (FragmentAnimationTag) view
								.getTag();
						transaction.setCustomAnimations(tag.inAnimation,
								tag.outAnimation);
					}
					transaction.replace(container.getId(),
							ScreenFragment.createFragment(target))
							.commitAllowingStateLoss();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void handleOpen(android.view.View view, Action act) {
		if (view.getContext() instanceof FragmentActivity) {
			FragmentActivity activity = (FragmentActivity) view.getContext();
			Screen target = ConfigState.getInstance().getConfiguration()
					.findScreenById((String) act.getTarget());
			if(target != null){
				ScreenDialog.createDialog(target, act).show(activity.getSupportFragmentManager(), target.getId());
			}
		}
	}
	
	private static void handleClose(android.view.View view, Action act) {
		if (view.getContext() instanceof FragmentActivity) {
			FragmentActivity activity = (FragmentActivity) view.getContext();
			Fragment f = activity.getSupportFragmentManager().findFragmentByTag(act.getTarget());
			if(f instanceof DialogFragment){
				((DialogFragment)f).dismissAllowingStateLoss();
			}
		}
	}
	
	
	private static void handleAdd(android.view.View view, Action act) {
		if (view.getContext() instanceof FragmentActivity) {
			FragmentActivity activity = (FragmentActivity) view.getContext();
			android.view.ViewGroup container = (ViewGroup) Utils.findViewByConfigId(
					view.getRootView(), (String) act.getContainer());
			Screen target = ConfigState.getInstance().getConfiguration()
					.findScreenById((String) act.getTarget());
			if (container != null && target != null) {
				android.view.View focus = container.getFocusedChild();
				if (focus != null) {
					focus = FocusFinder.getInstance().findNextFocus(
							(ViewGroup) container.getRootView(), null,
							android.view.View.FOCUS_FORWARD);
					if (focus != null)
						focus.requestFocus();
				}
				activity.getSupportFragmentManager()
						.beginTransaction()
						.replace(container.getId(),
								ScreenFragment.createFragment(target))
						.addToBackStack(null).commitAllowingStateLoss();
			}
		}
	}

	private static void handleIntent(android.view.View view, Action act,
			Object extraData) {
		Bind bd = act.getBindByName(PROP_INTENT);
		if (bd != null) {
			try {
				String str;
				if (Value.TYPE_TAG.equals(bd.getValue().getType())) {
					str = view.getTag().toString();
				} else {
					str = bd.getValue().getvalue();
				}

				if (extraData instanceof JSONObject) {
					Bind replace = act.getBindByName("replace");
					JSONArray jsa = new JSONArray(replace.getValue().getvalue());
					JSONObject exData = (JSONObject) extraData;
					for (int i = 0; i < jsa.length(); i++) {
						String token = jsa.getJSONObject(i).getString("token");
						String field = jsa.getJSONObject(i).getString("field");
						str = str.replace(token, exData.getString(field));
					}
				}

				JSONObject json = new JSONObject(str);
				
				Logger.d("intent: "+str);
				Intent i = new Intent();
				if (json.has("action"))
					i.setAction(json.getString("action"));
				if (json.has("package")) {
					if (json.has("className"))
						i.setClassName(json.getString("package"),
								json.getString("className"));
					else if(i.getAction() == null){
						Intent li = view.getContext().getPackageManager().getLaunchIntentForPackage(json.getString("package"));
						if(li != null)
							i = li;
					} else{
						i.setPackage(json.getString("package"));
					}
				}
				if (json.has("data"))
					i.setData(Uri.parse(json.getString("data")));
				if (json.has("type"))
					i.setType(json.getString("type"));
				if (json.has("category"))
					i.addCategory(json.getString("category"));
				if (json.has("extra")) {
					JSONObject extra = json.getJSONObject("extra");
					Iterator<?> it = extra.keys();
					while (it.hasNext()) {
						String name = (String) it.next();
						Object value = extra.get(name);
						if (value instanceof Integer) {
							i.putExtra(name, ((Integer) value).intValue());
						} else if (value instanceof Long) {
							i.putExtra(name, ((Long) value).longValue());
						} else if (value instanceof Boolean) {
							i.putExtra(name, ((Boolean) value).booleanValue());
						} else if (value instanceof Double) {
							i.putExtra(name, ((Double) value).doubleValue());
						} else if (value instanceof String) {
							i.putExtra(name, (String) value);
						}
					}
				}
				
				if(json.optBoolean("broadcast")){
					view.getContext().sendBroadcast(i);
				}else{
					if(!json.optBoolean("noNewTask")){
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					}
					
					if(json.has("flags")){
						i.addFlags(parseFlags(json.getString("flags")));
					}
					if (ConfigState.getInstance().getGlobalIntentIntercepter() == null
							|| !ConfigState.getInstance().getGlobalIntentIntercepter().handleIntent(i))
						view.getContext().startActivity(i);
				}

			} catch (ActivityNotFoundException e) {
				ConfigState.getInstance().notifyException(e);
				e.printStackTrace();
			} catch (JSONException e) {
				ConfigState.getInstance().notifyException(e);
				e.printStackTrace();
			}
		}
	}

	private static int parseFlags(String str) {
		int flag = 0;
		String[] secs = str.split("\\|");
		for (String s : secs) {
			try {
				Field f = Intent.class.getDeclaredField(s);
				flag |= f.getInt(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return flag;
	}
}
