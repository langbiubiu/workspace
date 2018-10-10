package ipaneltv.toolkit;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONStringer;

import android.os.Parcelable;

public class ReflectionInvoker {
	private static final HashMap<InvokeKey, WeakReference<ReflectionInvoker>> invokerStubs = new HashMap<InvokeKey, WeakReference<ReflectionInvoker>>();

	static class InvokeKey {
		String className;
		String methodName;
		String[] paramsClassName;

		InvokeKey(String className, String methodName, String[] paramsClassName) {
			this.className = className;
			this.methodName = methodName;
			this.paramsClassName = paramsClassName;
		}

		@Override
		public int hashCode() {
			int ret = className.hashCode();
			ret = methodName.hashCode() + ret * 37;
			for (int i = 0; i < paramsClassName.length; i++) {
				ret = paramsClassName[i].hashCode() + ret * 37;
			}
			return ret;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof InvokeKey) {
				InvokeKey o = (InvokeKey) obj;
				if (!className.equals(o.className) || !methodName.equals(o.methodName)
						|| paramsClassName.length != o.paramsClassName.length)
					return false;
				for (int i = 0; i < paramsClassName.length; i++) {
					if (!paramsClassName[i].equals(o.paramsClassName[i]))
						return false;
				}
				return true;
			}
			return false;
		}
	}

	static final String TAG = "ReflectionInvoker";
	private Method method;
	@SuppressWarnings("rawtypes")
	private Class clazz;
	@SuppressWarnings("rawtypes")
	private Class parameterClasses[];

	public static ReflectionInvoker getInvoker(String className, String methodName,
			String... argsClassName) {
		synchronized (invokerStubs) {
			ReflectionInvoker i = null;
			try {
				InvokeKey key = new InvokeKey(className, methodName, argsClassName);
				WeakReference<ReflectionInvoker> wi = invokerStubs.get(key);
				if (wi == null ? true : (i = wi.get()) == null) {
					i = new ReflectionInvoker(key);
					invokerStubs.put(key, new WeakReference<ReflectionInvoker>(i));
				}
			} catch (Exception e) {
				IPanelLog.d(TAG, "create createInvoker error:" + e);
				return null;
			}
			return i;
		}
	}

	public static ReflectionInvoker getInvoker(String className, String methodName,
			List<String> argsClassName) {
		String arrayargs[] = new String[argsClassName.size()];
		argsClassName.toArray(arrayargs);
		return getInvoker(className, methodName, arrayargs);
	}

	@SuppressWarnings("rawtypes")
	Class classForNameX(String name) throws Exception {
		if ("int".equals(name))
			return int.class;
		else if ("char".equals(name))
			return char.class;
		else if ("short".equals(name))
			return short.class;
		else if ("byte".equals(name))
			return byte.class;
		else if ("long".equals(name))
			return long.class;
		else if ("float".equals(name))
			return float.class;
		else if ("double".equals(name))
			return double.class;
		else if ("boolean".equals(name))
			return boolean.class;
		return Class.forName(name);
	}

	@SuppressWarnings("unchecked")
	ReflectionInvoker(InvokeKey key) throws Exception {
		clazz = Class.forName(key.className);
		parameterClasses = new Class[key.paramsClassName.length];
		for (int i = 0; i < key.paramsClassName.length; i++)
			parameterClasses[i] = classForNameX(key.paramsClassName[i]);
		method = clazz.getMethod(key.methodName, parameterClasses);
	}

	@SuppressWarnings("rawtypes")
	public Class getTargetClass() {
		return clazz;
	}

	public Method getTargetMethod() {
		return method;
	}

	public int getParameterSize() {
		return parameterClasses.length;
	}

	public Object invokeNoException(Object receiver, Object... args) {
		try {
			return method.invoke(receiver, args);
		} catch (Exception e) {
			IPanelLog.d(TAG, "invokeNoException error:" + e);
			return null;
		}
	}

	public Object invoke(Object receiver, Object... args) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return method.invoke(receiver, args);
	}

	@SuppressWarnings({ "rawtypes" })
	public static String buildInvokeJsonString(String className, String methodName, Object... args) {
		JSONStringer str = new JSONStringer();
		try {
			str.object();
			str.key("cn").value(className);
			str.key("mn").value(methodName);
			str.key("ps").value(args.length);
			str.array();
			for (int i = 0; i < args.length; i++) {
				Class clazz = args[i].getClass();
				if (clazz.isPrimitive() || args[i] instanceof Parcelable
						|| args[i] instanceof String || args[i] instanceof Integer
						|| args[i] instanceof Short || args[i] instanceof Float
						|| args[i] instanceof Double || args[i] instanceof Character) {
					str.value(clazz.getCanonicalName());
				} else {
					throw new RuntimeException("args only support primitives and String");
				}
			}
			str.endArray();
			str.array();
			for (int i = 0; i < args.length; i++) {
				str.value(args[i].getClass().getCanonicalName());
			}
			str.endArray();
			str.endObject();
			return null;
		} catch (JSONException e) {
		}
		return str.toString();
	}
}
