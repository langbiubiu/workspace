package ipaneltv.toolkit.entitlement;

import ipaneltv.toolkit.IPanelLog;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;


/**
 * 使用者须调用或重写onInit方法
 * @author Administrator
 *
 */
public class EntitlementContentProvider extends ContentProvider{

	private static final String TAG = "EntitlementContentProvider";
	private String authority ;
	private String contentType ;
	private String contentItemType ;
	private EntitlementProviderBase epb;
	private SQLiteOpenHelper dbHelper;
	private static int myPid ;
	private static int myUid ;

	@Override
	public boolean onCreate() {
		onInit(authority,contentType,contentItemType,dbHelper);
		epb = new EntitlementProviderBase(authority, contentType, contentItemType);
		if(epb!=null){
			myPid = Process.myPid();
			myUid = Process.myUid();
			return true;
		}
		return false;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (checkIllegal(Binder.getCallingPid(), Binder.getCallingUid())) {
			throw new IllegalThreadStateException(
					"Illegal access thread,this method is not allow access from other process!");
		}
		return epb.delete(dbHelper.getWritableDatabase(), uri, selection, selectionArgs, getContext());
	}

	@Override
	public String getType(Uri uri) {
		return epb.getUriType(uri);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (checkIllegal(Binder.getCallingPid(), Binder.getCallingUid())) {
			throw new IllegalThreadStateException(
					"Illegal access thread,this method is not allow access from other process!");
		}
		return epb.insert(dbHelper.getWritableDatabase(), uri, values, getContext());
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		IPanelLog.i(TAG, "epb  :"+epb+"  dbHelper :"+dbHelper +"  uri  :"+uri);
		Cursor cursor = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		IPanelLog.i(TAG, "can get db ");
		cursor = epb.query(db, uri, projection, selection, selectionArgs, sortOrder);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (checkIllegal(Binder.getCallingPid(), Binder.getCallingUid())) {
			throw new IllegalThreadStateException(
					"Illegal access thread,this method is not allow access from other process!");
		}
		return epb.update(dbHelper.getWritableDatabase(), uri, values, selection, selectionArgs, getContext());
		
	}
	
	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		SQLiteDatabase db = dbHelper.getWritableDatabase();  
        db.beginTransaction();  
        try{  
            ContentProviderResult[]results = super.applyBatch(operations);  
            db.setTransactionSuccessful();  
            return results;  
        }finally {  
            db.endTransaction();  
        }  
	}


	
	private boolean checkIllegal(int pid, int uid) {
		if (myPid != pid || uid != myUid) {
			return true;
		}
		return false;
	}
	
	/**
	 * 初始化,子类需要复写该方法
	 * @param authority
	 * @param contentType
	 * @param contentItemType
	 * @param dbHelper
	 */
	protected void onInit(String authority, String contentType,String contentItemType,SQLiteOpenHelper dbHelper){
		this.authority = authority;
		this.contentType = contentType;
		this.contentItemType = contentItemType;
		this.dbHelper = dbHelper;
	}
	

	
}
