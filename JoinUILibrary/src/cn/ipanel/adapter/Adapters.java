/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.ipanel.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import cn.ipanel.adapter.AdapterDefinition.BindItem;
import cn.ipanel.adapter.AdapterDefinition.MapItem;
import cn.ipanel.adapter.AdapterDefinition.SelectItem;
import cn.ipanel.adapter.AdapterDefinition.TransformItem;
import cn.ipanel.android.util.JSONUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public class Adapters {
	private static final String ADAPTER_CURSOR = "cursor-adapter";

	/**
	 * <p>
	 * Interface used to bind a {@link android.database.Cursor} column to a
	 * View. This interface can be used to provide bindings for data types not
	 * supported by the standard implementation of {@link Adapters}.
	 * </p>
	 * 
	 * <p>
	 * A binder is provided with a cursor transformation which may or may not be
	 * used to transform the value retrieved from the cursor. The transformation
	 * is guaranteed to never be null so it's always safe to apply the
	 * transformation.
	 * </p>
	 * 
	 * <p>
	 * The binder is associated with a Context but can be re-used with multiple
	 * cursors. As such, the implementation should make no assumption about the
	 * Cursor in use.
	 * </p>
	 * 
	 * @see android.view.View
	 * @see android.database.Cursor
	 * @see Adapters.CursorTransformation
	 */
	public static abstract class CursorBinder {
		/**
		 * <p>
		 * The context associated with this binder.
		 * </p>
		 */
		protected final Context mContext;

		/**
		 * <p>
		 * The transformation associated with this binder. This transformation
		 * is never null and may or may not be applied to the Cursor data during
		 * the {@link #bind(android.view.View, android.database.Cursor, int)}
		 * operation.
		 * </p>
		 * 
		 * @see #bind(android.view.View, android.database.Cursor, int)
		 */
		protected final CursorTransformation mTransformation;

		/**
		 * <p>
		 * Creates a new Cursor binder.
		 * </p>
		 * 
		 * @param context
		 *            The context associated with this binder.
		 * @param transformation
		 *            The transformation associated with this binder. This
		 *            transformation may or may not be applied by the binder and
		 *            is guaranteed to not be null.
		 */
		public CursorBinder(Context context, CursorTransformation transformation) {
			mContext = context;
			mTransformation = transformation;
		}

		/**
		 * <p>
		 * Binds the specified Cursor column to the supplied View. The binding
		 * operation can query other Cursor columns as needed. During the
		 * binding operation, values retrieved from the Cursor may or may not be
		 * transformed using this binder's cursor transformation.
		 * </p>
		 * 
		 * @param view
		 *            The view to bind data to.
		 * @param cursor
		 *            The cursor to bind data from.
		 * @param columnIndex
		 *            The column index in the cursor where the data to bind
		 *            resides.
		 * 
		 * @see #mTransformation
		 * 
		 * @return True if the column was successfully bound to the View, false
		 *         otherwise.
		 */
		public abstract boolean bind(View view, Cursor cursor, int columnIndex);
	}

	/**
	 * <p>
	 * Interface used to transform data coming out of a
	 * {@link android.database.Cursor} before it is bound to a
	 * {@link android.view.View}.
	 * </p>
	 * 
	 * <p>
	 * Transformations are used to transform text-based data (in the form of a
	 * String), or to transform data into a resource identifier. A default
	 * implementation is provided to generate resource identifiers.
	 * </p>
	 * 
	 * @see android.database.Cursor
	 * @see Adapters.CursorBinder
	 */
	public static abstract class CursorTransformation {
		/**
		 * <p>
		 * The context associated with this transformation.
		 * </p>
		 */
		protected final Context mContext;

		/**
		 * <p>
		 * Creates a new Cursor transformation.
		 * </p>
		 * 
		 * @param context
		 *            The context associated with this transformation.
		 */
		public CursorTransformation(Context context) {
			mContext = context;
		}

		/**
		 * <p>
		 * Transforms the specified Cursor column into a String. The
		 * transformation can simply return the content of the column as a
		 * String (this is known as the identity transformation) or manipulate
		 * the content. For instance, a transformation can perform text
		 * substitutions or concatenate other columns with the specified column.
		 * </p>
		 * 
		 * @param cursor
		 *            The cursor that contains the data to transform.
		 * @param columnIndex
		 *            The index of the column to transform.
		 * 
		 * @return A String containing the transformed value of the column.
		 */
		public abstract String transform(Cursor cursor, int columnIndex);

		/**
		 * <p>
		 * Transforms the specified Cursor column into a resource identifier.
		 * The default implementation simply interprets the content of the
		 * column as an integer.
		 * </p>
		 * 
		 * @param cursor
		 *            The cursor that contains the data to transform.
		 * @param columnIndex
		 *            The index of the column to transform.
		 * 
		 * @return A resource identifier.
		 */
		public int transformToResource(Cursor cursor, int columnIndex) {
			return cursor.getInt(columnIndex);
		}
	}

	/**
	 * <p>
	 * Loads the {@link android.widget.CursorAdapter} defined in the specified
	 * XML resource. The content of the adapter is loaded from the content
	 * provider identified by the supplied URI.
	 * </p>
	 * 
	 * <p>
	 * <strong>Note:</strong> If the supplied {@link android.content.Context} is
	 * an {@link android.app.Activity}, the cursor returned by the content
	 * provider will be automatically managed. Otherwise, you are responsible
	 * for managing the cursor yourself.
	 * </p>
	 * 
	 * <p>
	 * The format of the XML definition of the cursor adapter is documented at
	 * the top of this page.
	 * </p>
	 * 
	 * @param context
	 *            The context to load the XML resource from.
	 * @param id
	 *            The identifier of the XML resource declaring the adapter.
	 * @param uri
	 *            The URI of the content provider.
	 * @param parameters
	 *            Optional parameters to pass to the CursorAdapter, used to
	 *            substitute values in the selection expression.
	 * 
	 * @return A {@link android.widget.CursorAdapter}
	 * 
	 * @throws IllegalArgumentException
	 *             If the XML resource does not contain a valid
	 *             &lt;cursor-adapter /&gt; definition.
	 * 
	 * @see android.content.ContentProvider
	 * @see android.widget.CursorAdapter
	 * @see #loadAdapter(android.content.Context, int, Object[])
	 */
	public static CursorAdapter loadCursorAdapter(Context context, String json, String uri,
			String selection, Object... parameters) {

		XmlCursorAdapter adapter = (XmlCursorAdapter) loadAdapter(context, json, ADAPTER_CURSOR,
				parameters);

		if (uri != null) {
			adapter.setUri(uri);
		}
		if (selection != null)
			adapter.setSelection(selection);
		adapter.load();

		return adapter;
	}

	/**
	 * <p>
	 * Loads the {@link android.widget.CursorAdapter} defined in the specified
	 * XML resource. The content of the adapter is loaded from the specified
	 * cursor. You are responsible for managing the supplied cursor.
	 * </p>
	 * 
	 * <p>
	 * The format of the XML definition of the cursor adapter is documented at
	 * the top of this page.
	 * </p>
	 * 
	 * @param context
	 *            The context to load the XML resource from.
	 * @param id
	 *            The identifier of the XML resource declaring the adapter.
	 * @param cursor
	 *            The cursor containing the data for the adapter.
	 * @param parameters
	 *            Optional parameters to pass to the CursorAdapter, used to
	 *            substitute values in the selection expression.
	 * 
	 * @return A {@link android.widget.CursorAdapter}
	 * 
	 * @throws IllegalArgumentException
	 *             If the XML resource does not contain a valid
	 *             &lt;cursor-adapter /&gt; definition.
	 * 
	 * @see android.content.ContentProvider
	 * @see android.widget.CursorAdapter
	 * @see android.database.Cursor
	 * @see #loadAdapter(android.content.Context, int, Object[])
	 */
	public static CursorAdapter loadCursorAdapter(Context context, String adapterJson,
			Cursor cursor, Object... parameters) {

		XmlCursorAdapter adapter = (XmlCursorAdapter) loadAdapter(context, adapterJson,
				ADAPTER_CURSOR, parameters);

		if (cursor != null) {
			adapter.changeCursor(cursor);
		}

		return adapter;
	}

	/**
	 * <p>
	 * Loads the adapter defined in the specified XML resource. The XML
	 * definition of the adapter must follow the format definition of one of the
	 * supported adapter types described at the top of this page.
	 * </p>
	 * 
	 * <p>
	 * <strong>Note:</strong> If the loaded adapter is a
	 * {@link android.widget.CursorAdapter} and the supplied
	 * {@link android.content.Context} is an {@link android.app.Activity}, the
	 * cursor returned by the content provider will be automatically managed.
	 * Otherwise, you are responsible for managing the cursor yourself.
	 * </p>
	 * 
	 * @param context
	 *            The context to load the XML resource from.
	 * @param json
	 *            Adapter definition json.
	 * @param parameters
	 *            Optional parameters to pass to the adapter.
	 * 
	 * @return An adapter instance.
	 * 
	 * @see #loadCursorAdapter(android.content.Context, int,
	 *      android.database.Cursor, Object[])
	 * @see #loadCursorAdapter(android.content.Context, int, String, Object[])
	 */
	public static BaseAdapter loadAdapter(Context context, String json, Object... parameters) {
		final BaseAdapter adapter = loadAdapter(context, json, null, parameters);
		if (adapter instanceof ManagedAdapter) {
			((ManagedAdapter) adapter).load();
		}
		return adapter;
	}

	/**
	 * Loads an adapter from the specified XML resource. The optional assertName
	 * can be used to exit early if the adapter defined in the XML resource is
	 * not of the expected type.
	 * 
	 * @param context
	 *            The context to associate with the adapter.
	 * @param json
	 *            Adapter definition json.
	 * @param assertName
	 *            The mandatory name of the adapter in the XML document. Ignored
	 *            if null.
	 * @param parameters
	 *            Optional parameters passed to the adapter.
	 * 
	 * @return An instance of {@link android.widget.BaseAdapter}.
	 * @throws JSONException
	 */
	private static BaseAdapter loadAdapter(Context context, String json, String assertName,
			Object... parameters) {
		try {
			return createAdapterFromXml(context, new JSONObject(json), parameters, assertName);
		} catch (JSONException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"Invalid adaper definition json");
			iae.initCause(e);
			throw iae;
		}
	}

	/**
	 * Generates an adapter using the specified XML parser. This method is
	 * responsible for choosing the type of the adapter to create based on the
	 * content of the XML parser.
	 * 
	 * This method will generate an {@link IllegalArgumentException} if
	 * <code>assertName</code> is not null and does not match the root tag of
	 * the XML document.
	 */
	private static BaseAdapter createAdapterFromXml(Context c, JSONObject attrs,
			Object[] parameters, String assertName) {

		BaseAdapter adapter = null;

		AdapterDefinition definition = JSONUtil.fromJSON(attrs, AdapterDefinition.class);

		if (ADAPTER_CURSOR.equals(definition.name)) {
			adapter = createCursorAdapter(c, definition, parameters);
		}
		return adapter;

	}

	/**
	 * Creates an XmlCursorAdapter using an XmlCursorAdapterParser.
	 */
	private static XmlCursorAdapter createCursorAdapter(Context c, AdapterDefinition attrs,
			Object[] parameters) {

		return new XmlCursorAdapterParser(c, attrs).parse(parameters);
	}

	/**
	 * Parser that can generate XmlCursorAdapter instances. This parser is
	 * responsible for handling all the attributes and child nodes for a
	 * &lt;cursor-adapter /&gt;.
	 */
	private static class XmlCursorAdapterParser {
		private static final String ADAPTER_CURSOR_AS_STRING = "string";
		private static final String ADAPTER_CURSOR_AS_IMAGE = "image";
		private static final String ADAPTER_CURSOR_AS_TAG = "tag";
		private static final String ADAPTER_CURSOR_AS_IMAGE_URI = "image-uri";
		private static final String ADAPTER_CURSOR_AS_DRAWABLE = "drawable";
		private final Context mContext;
		// private final XmlPullParser mParser;
		private final AdapterDefinition mDefinition;
		// private final int mId;

		private final HashMap<String, CursorBinder> mBinders;
		private final ArrayList<String> mFrom;
		private final ArrayList<Integer> mTo;
		private final CursorTransformation mIdentity;
		private final Resources mResources;

		public XmlCursorAdapterParser(Context c, AdapterDefinition definition) {
			mContext = c;
			mDefinition = definition;

			mResources = mContext.getResources();
			mBinders = new HashMap<String, CursorBinder>();
			mFrom = new ArrayList<String>();
			mTo = new ArrayList<Integer>();
			mIdentity = new IdentityTransformation(mContext);
		}

		public XmlCursorAdapter parse(Object[] parameters) {

			String uri = mDefinition.uri;
			String selection = mDefinition.selection;
			String sortOrder = mDefinition.sortOrder;
			int layout = mResources.getIdentifier(mDefinition.layout, "layout",
					mContext.getPackageName());
			if (layout == 0) {
				throw new IllegalArgumentException("The layout specified in " + mDefinition.layout
						+ " does not exist");
			}

			for (BindItem bindItem : mDefinition.bindItemList) {
				parseBindTag(bindItem);
			}

			for (SelectItem selectItem : mDefinition.selectItemList) {
				parseSelectTag(selectItem);
			}

			String[] fromArray = mFrom.toArray(new String[mFrom.size()]);
			int[] toArray = new int[mTo.size()];
			for (int i = 0; i < toArray.length; i++) {
				toArray[i] = mTo.get(i);
			}

			String[] selectionArgs = null;
			if (parameters != null) {
				selectionArgs = new String[parameters.length];
				for (int i = 0; i < selectionArgs.length; i++) {
					selectionArgs[i] = (String) parameters[i];
				}
			}

			return new XmlCursorAdapter(mContext, layout, uri, fromArray, toArray, selection,
					selectionArgs, sortOrder, mBinders);
		}

		private void parseSelectTag(SelectItem item) {

			String fromName = item.column;
			if (fromName == null) {
				throw new IllegalArgumentException("A select item in " + mDefinition.name
						+ " does not have a 'column' attribute");
			}

			mFrom.add(fromName);
			mTo.add(View.NO_ID);
		}

		private void parseBindTag(BindItem item) {

			String fromName = item.from;

			int toName = mResources.getIdentifier(item.to, "id", mContext.getPackageName());

			String asType = item.as;

			mFrom.add(fromName);
			mTo.add(toName);
			mBinders.put(fromName, findBinder(item, asType));
		}

		private CursorBinder findBinder(BindItem item, String type) {
			final Context context = mContext;
			CursorTransformation transformation = mIdentity;

			final boolean isDrawable = ADAPTER_CURSOR_AS_DRAWABLE.equals(type);

			transformation = findTransformation(item.transformItem);
			if (item.mapList != null && item.mapList.size() > 0) {
				if (!(transformation instanceof MapTransformation)) {
					transformation = new MapTransformation(context);
				}
			}
			for (MapItem mi : item.mapList) {
				findMap((MapTransformation) transformation, mi, isDrawable);
			}

			if (ADAPTER_CURSOR_AS_STRING.equals(type)) {
				return new StringBinder(context, transformation);
			} else if (ADAPTER_CURSOR_AS_TAG.equals(type)) {
				return new TagBinder(context, transformation);
			} else if (ADAPTER_CURSOR_AS_IMAGE.equals(type)) {
				return new ImageBinder(context, transformation);
			} else if (ADAPTER_CURSOR_AS_IMAGE_URI.equals(type)) {
				return new ImageUriBinder(context, transformation);
			} else if (isDrawable) {
				return new DrawableBinder(context, transformation);
			} else {
				return createBinder(type, transformation);
			}
		}

		private CursorBinder createBinder(String type, CursorTransformation transformation) {
			if (mContext.isRestricted())
				return null;

			try {
				final Class<?> klass = Class.forName(type, true, mContext.getClassLoader());
				if (CursorBinder.class.isAssignableFrom(klass)) {
					final Constructor<?> c = klass.getDeclaredConstructor(Context.class,
							CursorTransformation.class);
					return (CursorBinder) c.newInstance(mContext, transformation);
				}
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Cannot instanciate binder type in "
						+ mDefinition.name + ": " + type, e);
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException("Cannot instanciate binder type in "
						+ mDefinition.name + ": " + type, e);
			} catch (InvocationTargetException e) {
				throw new IllegalArgumentException("Cannot instanciate binder type in "
						+ mDefinition.name + ": " + type, e);
			} catch (InstantiationException e) {
				throw new IllegalArgumentException("Cannot instanciate binder type in "
						+ mDefinition.name + ": " + type, e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Cannot instanciate binder type in "
						+ mDefinition.name + ": " + type, e);
			}

			return null;
		}

		private void findMap(MapTransformation transformation, MapItem mapItem, boolean drawable) {

			String from = mapItem.fromValue;

			if (!drawable) {
				String to = mapItem.toValue;
				transformation.addStringMapping(from, to);
			} else {
				int to = mResources.getIdentifier(mapItem.toValue, "drawable",
						mContext.getPackageName());
				if (to == 0) {
					throw new IllegalArgumentException("A map item in " + mapItem.toValue
							+ " does not have match a drawable");
				}
				transformation.addResourceMapping(from, to);
			}
		}

		private CursorTransformation findTransformation(TransformItem item) {
			CursorTransformation transformation = null;

			String className = item.withClass;
			if (className == null) {
				String expression = item.withExpression;
				transformation = createExpressionTransformation(expression);
			} else if (!mContext.isRestricted()) {
				try {
					final Class<?> klas = Class.forName(className, true, mContext.getClassLoader());
					if (CursorTransformation.class.isAssignableFrom(klas)) {
						final Constructor<?> c = klas.getDeclaredConstructor(Context.class);
						transformation = (CursorTransformation) c.newInstance(mContext);
					}
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Cannot instanciate transform type in "
							+ mDefinition.name + ": " + className, e);
				} catch (NoSuchMethodException e) {
					throw new IllegalArgumentException("Cannot instanciate transform type in "
							+ mDefinition.name + ": " + className, e);
				} catch (InvocationTargetException e) {
					throw new IllegalArgumentException("Cannot instanciate transform type in "
							+ mDefinition.name + ": " + className, e);
				} catch (InstantiationException e) {
					throw new IllegalArgumentException("Cannot instanciate transform type in "
							+ mDefinition.name + ": " + className, e);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException("Cannot instanciate transform type in "
							+ mDefinition.name + ": " + className, e);
				}
			}

			if (transformation == null) {
				throw new IllegalArgumentException("A transform item in " + mDefinition.name
						+ " must have a 'withClass' or " + "'withExpression' attribute");
			}

			return transformation;
		}

		private CursorTransformation createExpressionTransformation(String expression) {
			return new ExpressionTransformation(mContext, expression);
		}
	}

	/**
	 * Interface used by adapters that require to be loaded after creation.
	 */
	private static interface ManagedAdapter {
		/**
		 * Loads the content of the adapter, asynchronously.
		 */
		void load();
	}

	/**
	 * Implementation of a Cursor adapter defined in XML. This class is a thin
	 * wrapper of a SimpleCursorAdapter. The main difference is the ability to
	 * handle CursorBinders.
	 */
	private static class XmlCursorAdapter extends SimpleCursorAdapter implements ManagedAdapter {
		private Context mContext;
		private String mUri;
		private String mSelection;
		private final String[] mSelectionArgs;
		private final String mSortOrder;
		private final int[] mTo;
		private final String[] mFrom;
		private final String[] mColumns;
		private final CursorBinder[] mBinders;
		private AsyncTask<Void, Void, Cursor> mLoadTask;

		@SuppressWarnings("deprecation")
		XmlCursorAdapter(Context context, int layout, String uri, String[] from, int[] to,
				String selection, String[] selectionArgs, String sortOrder,
				HashMap<String, CursorBinder> binders) {

			super(context, layout, null, from, to);
			mContext = context;
			mUri = uri;
			mFrom = from;
			mTo = to;
			mSelection = selection;
			mSelectionArgs = selectionArgs;
			mSortOrder = sortOrder;
			mColumns = new String[from.length + 1];
			// This is mandatory in CursorAdapter
			mColumns[0] = "_id";
			System.arraycopy(from, 0, mColumns, 1, from.length);

			CursorBinder basic = new StringBinder(context, new IdentityTransformation(context));
			final int count = from.length;
			mBinders = new CursorBinder[count];

			for (int i = 0; i < count; i++) {
				CursorBinder binder = binders.get(from[i]);
				if (binder == null)
					binder = basic;
				mBinders[i] = binder;
			}
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final int count = mTo.length;
			final int[] to = mTo;
			final CursorBinder[] binders = mBinders;

			for (int i = 0; i < count; i++) {
				final View v = view.findViewById(to[i]);
				if (v != null) {
					// Not optimal, the column index could be cached
					binders[i].bind(v, cursor, cursor.getColumnIndex(mFrom[i]));
				}
			}
		}

		public void load() {
			if (mUri != null) {
				mLoadTask = new QueryTask().execute();
			}
		}

		void setUri(String uri) {
			mUri = uri;
		}

		void setSelection(String selection) {
			mSelection = selection;
		}

		@Override
		public void changeCursor(Cursor c) {
			if (mLoadTask != null && mLoadTask.getStatus() != QueryTask.Status.FINISHED) {
				mLoadTask.cancel(true);
				mLoadTask = null;
			}
			super.changeCursor(c);
		}

		class QueryTask extends AsyncTask<Void, Void, Cursor> {
			@SuppressWarnings("deprecation")
			@Override
			protected Cursor doInBackground(Void... params) {
				if (mContext instanceof Activity) {
					return ((Activity) mContext).managedQuery(Uri.parse(mUri), mColumns,
							mSelection, mSelectionArgs, mSortOrder);
				} else {
					return mContext.getContentResolver().query(Uri.parse(mUri), mColumns,
							mSelection, mSelectionArgs, mSortOrder);
				}
			}

			@Override
			protected void onPostExecute(Cursor cursor) {
				if (!isCancelled()) {
					XmlCursorAdapter.super.changeCursor(cursor);
				}
			}
		}
	}

	/**
	 * Identity transformation, returns the content of the specified column as a
	 * String, without performing any manipulation. This is used when no
	 * transformation is specified.
	 */
	private static class IdentityTransformation extends CursorTransformation {
		public IdentityTransformation(Context context) {
			super(context);
		}

		@Override
		public String transform(Cursor cursor, int columnIndex) {
			return cursor.getString(columnIndex);
		}
	}

	/**
	 * An expression transformation is a simple template based replacement
	 * utility. In an expression, each segment of the form
	 * <code>{([^}]+)}</code> is replaced with the value of the column of name
	 * $1.
	 */
	private static class ExpressionTransformation extends CursorTransformation {
		private final ExpressionNode mFirstNode = new ConstantExpressionNode("");
		private final StringBuilder mBuilder = new StringBuilder();

		public ExpressionTransformation(Context context, String expression) {
			super(context);

			parse(expression);
		}

		private void parse(String expression) {
			ExpressionNode node = mFirstNode;
			int segmentStart;
			int count = expression.length();

			for (int i = 0; i < count; i++) {
				char c = expression.charAt(i);
				// Start a column name segment
				segmentStart = i;
				if (c == '{') {
					while (i < count && (c = expression.charAt(i)) != '}') {
						i++;
					}
					// We've reached the end, but the expression didn't close
					if (c != '}') {
						throw new IllegalStateException("The transform expression contains a "
								+ "non-closed column name: "
								+ expression.substring(segmentStart + 1, i));
					}
					node.next = new ColumnExpressionNode(expression.substring(segmentStart + 1, i));
				} else {
					while (i < count && (c = expression.charAt(i)) != '{') {
						i++;
					}
					node.next = new ConstantExpressionNode(expression.substring(segmentStart, i));
					// Rewind if we've reached a column expression
					if (c == '{')
						i--;
				}
				node = node.next;
			}
		}

		@Override
		public String transform(Cursor cursor, int columnIndex) {
			final StringBuilder builder = mBuilder;
			builder.delete(0, builder.length());

			ExpressionNode node = mFirstNode;
			// Skip the first node
			while ((node = node.next) != null) {
				builder.append(node.asString(cursor));
			}

			return builder.toString();
		}

		static abstract class ExpressionNode {
			public ExpressionNode next;

			public abstract String asString(Cursor cursor);
		}

		static class ConstantExpressionNode extends ExpressionNode {
			private final String mConstant;

			ConstantExpressionNode(String constant) {
				mConstant = constant;
			}

			@Override
			public String asString(Cursor cursor) {
				return mConstant;
			}
		}

		static class ColumnExpressionNode extends ExpressionNode {
			private final String mColumnName;
			private Cursor mSignature;
			private int mColumnIndex = -1;

			ColumnExpressionNode(String columnName) {
				mColumnName = columnName;
			}

			@Override
			public String asString(Cursor cursor) {
				if (cursor != mSignature || mColumnIndex == -1) {
					mColumnIndex = cursor.getColumnIndex(mColumnName);
					mSignature = cursor;
				}

				return cursor.getString(mColumnIndex);
			}
		}
	}

	/**
	 * A map transformation offers a simple mapping between specified String
	 * values to Strings or integers.
	 */
	private static class MapTransformation extends CursorTransformation {
		private final HashMap<String, String> mStringMappings;
		private final HashMap<String, Integer> mResourceMappings;

		public MapTransformation(Context context) {
			super(context);
			mStringMappings = new HashMap<String, String>();
			mResourceMappings = new HashMap<String, Integer>();
		}

		void addStringMapping(String from, String to) {
			mStringMappings.put(from, to);
		}

		void addResourceMapping(String from, int to) {
			mResourceMappings.put(from, to);
		}

		@Override
		public String transform(Cursor cursor, int columnIndex) {
			final String value = cursor.getString(columnIndex);
			final String transformed = mStringMappings.get(value);
			return transformed == null ? value : transformed;
		}

		@Override
		public int transformToResource(Cursor cursor, int columnIndex) {
			final String value = cursor.getString(columnIndex);
			final Integer transformed = mResourceMappings.get(value);
			try {
				return transformed == null ? Integer.parseInt(value) : transformed;
			} catch (NumberFormatException e) {
				return 0;
			}
		}
	}

	/**
	 * Binds a String to a TextView.
	 */
	private static class StringBinder extends CursorBinder {
		public StringBinder(Context context, CursorTransformation transformation) {
			super(context, transformation);
		}

		@Override
		public boolean bind(View view, Cursor cursor, int columnIndex) {
			if (view instanceof TextView) {
				final String text = mTransformation.transform(cursor, columnIndex);
				((TextView) view).setText(text);
				return true;
			}
			return false;
		}
	}

	/**
	 * Binds an image blob to an ImageView.
	 */
	private static class ImageBinder extends CursorBinder {
		public ImageBinder(Context context, CursorTransformation transformation) {
			super(context, transformation);
		}

		@Override
		public boolean bind(View view, Cursor cursor, int columnIndex) {
			if (view instanceof ImageView) {
				final byte[] data = cursor.getBlob(columnIndex);
				((ImageView) view).setImageBitmap(BitmapFactory.decodeByteArray(data, 0,
						data.length));
				return true;
			}
			return false;
		}
	}

	private static class TagBinder extends CursorBinder {
		public TagBinder(Context context, CursorTransformation transformation) {
			super(context, transformation);
		}

		@Override
		public boolean bind(View view, Cursor cursor, int columnIndex) {
			final String text = mTransformation.transform(cursor, columnIndex);
			view.setTag(text);
			return true;
		}
	}

	/**
	 * Binds an image URI to an ImageView.
	 */
	private static class ImageUriBinder extends CursorBinder {
		public ImageUriBinder(Context context, CursorTransformation transformation) {
			super(context, transformation);
		}

		@Override
		public boolean bind(View view, Cursor cursor, int columnIndex) {
			if (view instanceof ImageView) {
				((ImageView) view).setImageURI(Uri.parse(mTransformation.transform(cursor,
						columnIndex)));
				return true;
			}
			return false;
		}
	}

	/**
	 * Binds a drawable resource identifier to an ImageView.
	 */
	private static class DrawableBinder extends CursorBinder {
		public DrawableBinder(Context context, CursorTransformation transformation) {
			super(context, transformation);
		}

		@Override
		public boolean bind(View view, Cursor cursor, int columnIndex) {
			if (view instanceof ImageView) {
				final int resource = mTransformation.transformToResource(cursor, columnIndex);
				if (resource == 0)
					return false;

				((ImageView) view).setImageResource(resource);
				return true;
			}
			return false;
		}
	}
}
