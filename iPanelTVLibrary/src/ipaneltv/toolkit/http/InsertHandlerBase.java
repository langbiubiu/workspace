package ipaneltv.toolkit.http;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Handler;

public class InsertHandlerBase {
	Context context;
	Object tag;
	Handler handler;
	String authority;
	ArrayList<ContentProviderOperation> operations;
	
	public InsertHandlerBase(Context context, Handler handler,
			String authority, ArrayList<ContentProviderOperation> operations) {
		this.context = context;
		this.handler = handler;
		this.authority = authority;
		this.operations = operations;
	}

	Runnable proc = new Runnable() {
		@Override
		public void run() {
			try {
				InsertHandler inh = in;
				if (inh != null)
					inh.onInsertStart();
				context.getContentResolver().applyBatch(authority, operations);
				/*if (c == null) {
//					onCursorNotFound();
				} else {
					if (h != null)
						h.onQueryProcess();
					onCursorStart(c);
					if (c.moveToFirst()) {
						do {
							onRecordFound(c);
						} while (c.moveToNext());
					}
				}*/
//				onCursorEnd(c);
				if (inh != null)
					inh.onInsertEnd();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	public void postInsert() {
		try {
			if (handler == null)
				proc.run();
			else
				handler.post(proc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	InsertHandler in;

	public void setInsertHandler(InsertHandler h) {
		in = h;
	}
}
