package com.ipanel.join.chongqing.live.navi;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.uuids.db.FujianDatabase;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.net.telecast.NetworkDatabase;
import android.os.Handler;
import android.util.Log;
import cn.ipanel.android.LogHelper;

import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;
import com.ipanel.join.chongqing.live.LiveApp;

public class FaivoratesCommitor {
	private final static String TAG = FaivoratesCommitor.class.getName();
	public static final int GROUP_ID = Constant.FAVORITE_COLUME_ID;
	public static final int USER_ID = android.os.Process.myUid();
	public static final String NAME = "__faivorites";
	static final String DEL_SELECTTION = "1 = 1 AND "+FujianDatabase.FujianGroups.GROUP_ID + "=? and "
			+ FujianDatabase.FujianGroups.USER_ID + "=?";

	FaivoratesCommitor() {
	}

	public void commitFaivoritesGroup(final Context context, final LiveGroup g,
			final List<ChannelKey> list, Handler handler) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				procCommitFaivoritesGroup(context, g, list);
			}
		});
	}

	protected void onCommitFailed() {
	}

	private void procCommitFaivoritesGroup(Context context, LiveGroup g, List<ChannelKey> list) {
		Uri uri, dbUri;
		dbUri = LiveApp.getInstance().getNetworkDatabaseUri();
		uri = Uri.withAppendedPath(dbUri, NetworkDatabase.Groups.TABLE_NAME);
		Log.d(TAG,"uri = "+uri.toString()+"  dbUri = "+dbUri.toString());
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		String[] args = new String[] { String.valueOf(GROUP_ID), String.valueOf(USER_ID) };
		ops.add(ContentProviderOperation.newDelete(uri).withSelection(DEL_SELECTTION, args)
				.build());
		LogHelper.i("---------------> add favorite "+list.size());
		LogHelper.i("---------------> add favorite "+DEL_SELECTTION);

		for (int i = 0; i < list.size(); i++) {
			ChannelKey key = list.get(i);
			ContentValues value = new ContentValues();
			value.put(FujianDatabase.FujianGroups.GROUP_ID, GROUP_ID);
			value.put(FujianDatabase.FujianGroups.FREQUENCY, key.getFrequency());
			value.put(FujianDatabase.FujianGroups.PROGRAM_NUMBER, key.getProgram());
			value.put(FujianDatabase.FujianGroups.USER_ID, USER_ID);
			value.put(FujianDatabase.FujianGroups.GROUP_NAME, NAME);
			ops.add(ContentProviderOperation.newInsert(uri).withValues(value).build());
		}
		try {
			g.setList(list);
			LiveApp.getInstance().appCtx.getContentResolver().applyBatch(dbUri.getAuthority(), ops);
		} catch (Exception e) {
			e.printStackTrace();
			onCommitFailed();
		}
	}
}
