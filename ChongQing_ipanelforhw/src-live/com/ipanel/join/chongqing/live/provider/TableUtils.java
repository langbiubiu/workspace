package com.ipanel.join.chongqing.live.provider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import cn.ipanel.android.LogHelper;

public class TableUtils {
	/**
	 * The unique ID for a row.
	 * <P>
	 * Type: INTEGER (long)
	 * </P>
	 */
	public static final String _ID = "_id";

	/**
	 * 精简版查询记录
	 * */
	public static <E> List<E> query(Context context, Uri uri, Class<E> c) {
		return query(context, uri, c, null, null, null, null);
	}

	/**
	 * 完整版查询记录
	 * */
	public static <E> List<E> query(Context context, Uri uri, Class<E> c,
			String selection, String[] selectionArgs, String sortOrder,
			DecorationEntryListener lis) {
		Cursor cur = context.getContentResolver().query(uri,
				getAllTableColume(c), selection, selectionArgs, sortOrder);
		final List<E> result = new ArrayList<E>();
		if (cur != null && cur.moveToFirst()) {
			do {
				E data = changeCursorToEntry(cur, c, lis);
				if (data != null) {
					result.add(data);
				}
			} while (cur.moveToNext());
		}
		if (cur != null) {
			cur.close();
		}
		LogHelper.d(String.format("get %s records when querying url %s", result.size(),uri.getLastPathSegment()));
		return result;
	}
	/**
	 * 更新记录数据
	 * */
	public static <T> void update(Context context, Uri uri, T value,
			String selection, String[] selectionArgs) {
		ContentValues values = fillContentValues(new ContentValues(), value);
		context.getContentResolver().update(uri, values, selection,
				selectionArgs);
		values.clear();
	}
	/**
	 * 插入记录数据
	 * */
	public static <T> void insert(Context context, Uri uri, T value) {
		ContentValues values = fillContentValues(new ContentValues(), value);
		context.getContentResolver().insert(uri, values);
		values.clear();
	}
	/**
	 * 删除记录数据
	 * */
	public static void delete(Context context, Uri uri, String selection,
			String[] selectionArgs) {
		context.getContentResolver().delete(uri, selection, selectionArgs);
	}

	public static <E> String getTableName(Class<E> c) {
		_Table annotation = c.getAnnotation(_Table.class);
		if (annotation != null) {
			return annotation.table();
		}
		return null;
	}

	public static <E> String[] getAllTableColume(Class<E> c) {
		List<String> list = new ArrayList<String>();
		Field[] fields = c.getDeclaredFields();
		int N = fields == null ? 0 : fields.length;
		for (int i = 0; i < N; i++) {
			Field field = fields[i];
			_Field annotation = field
					.getAnnotation(_Field.class);
			if (annotation != null) {
				list.add(annotation.field());
			}
		}
		String[] result = new String[list.size()];
		return list.toArray(result);
	}

	public static <T> ContentValues fillContentValues(ContentValues values,
			T value) {
		Class c = value.getClass();
		Field[] fields = c.getDeclaredFields();
		int N = fields == null ? 0 : fields.length;
		for (int i = 0; i < N; i++) {
			Field field = fields[i];
			_Field annotation = field
					.getAnnotation(_Field.class);
			if (annotation != null) {
				try {
					boolean accessFlag = field.isAccessible();
					field.setAccessible(true);
					values.put(annotation.field(), field.get(value) + "");
					field.setAccessible(accessFlag);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return values;
	}

	public static <E> String getCreateSQL(Class<E> c) {
		String table = getTableName(c);
		if (TextUtils.isEmpty(table)) {
			throw new IllegalStateException(String.format(
					"you have not specified table name for class %s",
					c.getName()));
		}
		StringBuffer result = new StringBuffer();
		result.append("CREATE TABLE ");
		result.append(table);
		result.append(" ( ");
		result.append(_ID);
		result.append(" INTEGER PRIMARY KEY");
		Field[] fields = c.getDeclaredFields();
		int N = fields == null ? 0 : fields.length;
		for (int i = 0; i < N; i++) {
			Field field = fields[i];
			_Field annotation = field
					.getAnnotation(_Field.class);
			if (annotation != null) {
				String value = annotation.field();
				if (value.equals(_ID)) {
					continue;
				}
				result.append(" , " + value + " TEXT");
			}
		}
		result.append(" ) ");
		return result.toString();
	}

	public static <E> HashMap<String, String> getKeyMap(Class<E> c) {
		HashMap map = new HashMap<String, String>();
		Field[] fields = c.getDeclaredFields();
		int N = fields == null ? 0 : fields.length;
		for (int i = 0; i < N; i++) {
			Field field = fields[i];
			_Field annotation = field
					.getAnnotation(_Field.class);
			if (annotation != null) {
				String value = annotation.field();
				map.put(value, value);
			}
		}
		return map;
	}

	public static <E> String getDropSQL(Class<E> c) {
		String table = getTableName(c);
		if (TextUtils.isEmpty(table)) {
			throw new IllegalStateException(String.format(
					"you have not specified table name for class %s",
					c.getName()));
		}
		return "DROP TABLE IF EXIST " + table;
	}

	public static <E> E changeCursorToEntry(Cursor cur, Class<E> c,
			DecorationEntryListener l) {
		if (cur == null) {
			return null;
		}
		try {
			E result = (E) c.newInstance();
			Field[] fields = c.getDeclaredFields();
			int N = fields == null ? 0 : fields.length;
			for (int i = 0; i < N; i++) {
				Field field = fields[i];
				_Field annotation = field
						.getAnnotation(_Field.class);
				if (annotation != null) {
					String value = annotation.field();
					int cursor_index = cur.getColumnIndex(value);
					if (cursor_index < 0) {
						continue;
					}
					boolean accessFlag = field.isAccessible();
					field.setAccessible(true);
					String type = field.getGenericType().toString();
					// 如果类型是String
					if (type.equals("class java.lang.String")) { // 如果type是类类型，则前面包含"class "，后面跟类名
						field.set(result, cur.getString(cursor_index));
						// 如果类型是Integer
					} else if (type.equals("class java.lang.Integer")
							|| "int".equals(type)) {
						field.set(result, cur.getInt(cursor_index));
						// 如果类型是Long
					} else if (type.equals("class java.lang.Long")
							|| "long".equals(type)) {
						field.set(result, cur.getLong(cursor_index));
						// 如果类型是Double
					} else if (type.equals("class java.lang.Double")
							|| "double".equals(type)) {
						field.set(result, cur.getDouble(cursor_index));
						// 如果类型是Short
					} else if (type.equals("class java.lang.Short")
							|| "short".equals(type)) {
						field.set(result, cur.getShort(cursor_index));
					} else {
						LogHelper.e(String.format(
								"can't fill a fied type : %s", type));
					}
					field.setAccessible(accessFlag);
				}
			}
			if (l != null&&!l.decorationEntry(result, cur)) {
				return null;
			}
			return result;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public interface DecorationEntryListener {
		public <E> boolean decorationEntry(E e, Cursor c);
	}

}
