package com.ipanel.join.chongqing.live.provider;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.SharedPreferencesMenager;
import com.ipanel.join.chongqing.live.data.BookData;

public class LiveContentProvider extends AbsContentProvider {

	@Override
	protected ProviderConfig createProviderConfig() {
		// TODO Auto-generated method stub
		return new ProviderConfig() {
			@Override
			public int getDataBaseVersion() {
				// TODO Auto-generated method stub
				return 1;
			}
			
			@Override
			public String getDataBaseName() {
				// TODO Auto-generated method stub
				return "live.db";
			}
			
			@Override
			public String getAuthority() {
				// TODO Auto-generated method stub
				return Constant.AUTHORITY;
			}
			
			@Override
			public Class[] createClasses() {
				// TODO Auto-generated method stub
				return new Class[] {BookData.class};
			}
		};
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		String scene =uri.getLastPathSegment();
		
		if("history".equals(scene)){
			String[] colums = new String[] { "name","number","freq","prog","tsid" };
			MatrixCursor cur = new MatrixCursor(colums);
			SharedPreferencesMenager mSharedPreferencesMenager=SharedPreferencesMenager.getInstance(getContext());
			cur.addRow(new String[]{mSharedPreferencesMenager.getSaveChannelName(),mSharedPreferencesMenager.getSaveChannel()+"",mSharedPreferencesMenager.getSaveFreq()+"",mSharedPreferencesMenager.getSaveProg()+"",mSharedPreferencesMenager.getSaveTSID()+""});
			return cur;
		}
		return super.query(uri, projection, selection, selectionArgs, sortOrder);
	}
	
	@Override
	public boolean onCreate() {
		return super.onCreate();
	}

}
