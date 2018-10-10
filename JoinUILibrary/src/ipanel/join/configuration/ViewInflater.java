package ipanel.join.configuration;

import ipanel.join.widget.IConfigViewGroup;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.view.InflateException;
import android.view.ViewGroup;

public class ViewInflater {
    static final Class<?>[] mConstructorSignature = new Class[] {
        Context.class, View.class};
    private static final HashMap<String, Constructor<? extends android.view.View>> sConstructorMap =
            new HashMap<String, Constructor<? extends android.view.View>>();

	public static android.view.View inflateView(Context context, android.view.View parent, View data){
		return inflateView(context, parent, data, null);
	}
	
	public static android.view.View inflateView(Context context, android.view.View parent, View data, List<Bind> extBinds){
		try {
			data.applyExtBinds(extBinds);
			android.view.View view = createView(context, data);
			if(view.getId() == android.view.View.NO_ID)
				view.setId(generateViewId());
			if(parent instanceof IConfigViewGroup){
				IConfigViewGroup group = (IConfigViewGroup) parent;
				view.setLayoutParams(group.genConfLayoutParams(data));
				ViewGroup pg = (ViewGroup) parent;
				pg.addView(view);
			}
			for(View vd : data.getView()){
				inflateView(context, view, vd, extBinds);
			}
			return view;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new InflateException("failed to inflate "+data.getId()+" - "+data.getClazz());
	}
	
	public static android.view.View createView(Context context, View data) throws ClassNotFoundException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
		Constructor<? extends android.view.View> constructor = sConstructorMap.get(data.getClazz());
		Class<? extends android.view.View> clazz = null;
		if(constructor == null){
			ClassLoader classLoader = ConfigState.getInstance().getClassLoader();
			if(classLoader == null)
				classLoader = context.getClassLoader();
			clazz = classLoader.loadClass(data.getClazz()).asSubclass(android.view.View.class);
			constructor = clazz.getConstructor(mConstructorSignature);
			sConstructorMap.put(data.getClazz(), constructor);
		}
		
		return constructor.newInstance(new Object[]{context, data});
	}
	
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
	
	private static HashMap<String, Integer> sIDMap = new HashMap<String, Integer>();
	
	public static int getId(String idStr){
		Integer id = sIDMap.get(idStr);
		if(id == null){
			synchronized(sIDMap){
				id = generateViewId();
				sIDMap.put(idStr, id);
			}
		}
		return id;
	}

	/**
	 * Generate a value suitable for use in {@link #setId(int)}.
	 * This value will not collide with ID values generated at build time by aapt for R.id.
	 *
	 * @return a generated ID value
	 */
	public static int generateViewId() {
	    for (;;) {
	        final int result = sNextGeneratedId.get();
	        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
	        int newValue = result + 1;
	        if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
	        if (sNextGeneratedId.compareAndSet(result, newValue)) {
	            return result;
	        }
	    }
	}	
}
