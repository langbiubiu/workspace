package ipaneltv.toolkit.db;

import ipaneltv.toolkit.IPanelLog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

public abstract class CursorHandlerBase  {
	Context context;
	Uri uri;
	String selection = null, projection[] = null, sortOrder = null;
	String[] selectionArgs = null;
	Object tag;
	Handler handler;
	Runnable proc = new Runnable() {
		@Override
		public void run() {
			try {
				QueryHandler h = ch;
				if (h != null)
					h.onQueryStart();
				Cursor c = context.getContentResolver().query(uri, projection, selection,
						selectionArgs, sortOrder);
				IPanelLog.i("navigation", "CursorHandlerBase -->go post run-->uri="+uri);
				if (c == null) {
					onCursorNotFound();
				} else {
					if (h != null)
						h.onQueryProcess();
					onCursorStart(c);
					if (c.moveToFirst()) {
						do {
							onRecordFound(c);
						} while (c.moveToNext());
					}
				}
				onCursorEnd(c);
				if (h != null)
					h.onQueryEnd();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public CursorHandlerBase(Context context, Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder, Handler handler) {
		this.context = context;
		this.uri = uri;
		this.selection = selection;
		this.selectionArgs = selectionArgs;
		this.projection = projection;
		this.sortOrder = sortOrder;
		this.handler = handler;
	}

	public void postQuery() {
		try {
			if (handler == null)
				proc.run();
			else
				handler.post(proc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Uri getUri() {
		return uri;
	}

	public String getSelection() {
		return selection;
	}

	public String[] getProjection() {
		return projection;
	}

	public String[] getSelectionArgs() {
		return selectionArgs;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	public abstract void onCursorNotFound();

	public abstract void onCursorStart(Cursor c);

	public abstract void onRecordFound(Cursor c);

	public abstract void onCursorEnd(Cursor c);

	QueryHandler ch;

	public void setQueryHandler(QueryHandler h) {
		ch = h;
	}
}
