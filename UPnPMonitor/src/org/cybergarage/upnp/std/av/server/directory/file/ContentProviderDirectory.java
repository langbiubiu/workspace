package org.cybergarage.upnp.std.av.server.directory.file;

import java.io.File;

import org.cybergarage.upnp.std.av.server.object.ContentNode;
import org.cybergarage.upnp.std.av.server.object.item.file.FileItemNode;
import org.cybergarage.upnp.std.av.server.object.item.file.FileItemNodeList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class ContentProviderDirectory extends FileDirectory {
	public enum MediaType {
		Videos, Musics, Photos
	}

	private Context mContext;

	private MediaType mType;

	public ContentProviderDirectory(Context context, MediaType type, String name) {
		super(name);
		this.mContext = context.getApplicationContext();
		this.mType = type;
	}

	@Override
	protected FileItemNodeList getCurrentDirectoryItemNodeList() {
		FileItemNodeList itemNodeList = new FileItemNodeList();
		switch (mType) {
		case Musics:
			loadAudio(itemNodeList);
			break;
		case Photos:
			loadPhotos(itemNodeList);
			break;
		case Videos:
			loadVideo(itemNodeList);
			break;
		default:
			break;

		}
		return itemNodeList;
	}

	private void loadVideo(FileItemNodeList itemNodeList) {
		ContentResolver cr = mContext.getContentResolver();

		Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

		String[] columns = new String[] { MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID };

		try {
			Cursor cursor = cr.query(uri, columns, null, null, MediaStore.MediaColumns.DATE_ADDED
					+ " desc");

			loadItems(itemNodeList, cursor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void loadItems(FileItemNodeList itemNodeList, Cursor cursor) {
		if (cursor == null)
			return;
		Log.i("DLNA", "file count :"+(cursor==null?0:cursor.getCount()));
		while (cursor.moveToNext()) {
			File file = new File(cursor.getString(0));
			int id = cursor.getInt(1);
			if (file.exists() && file.isFile()) {
				FileItemNode itemNode = createCompareItemNode(file);
				if (itemNode == null)
					continue;
				itemNode.setAttribute(ContentNode.ID, id);
				itemNodeList.add(itemNode);
			}
		}

		cursor.close();
	}

	private void loadPhotos(FileItemNodeList itemNodeList) {
		ContentResolver cr = mContext.getContentResolver();

		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		String[] columns = new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };

		try {
			Cursor cursor = cr.query(uri, columns, null, null, MediaStore.MediaColumns.DATE_ADDED
					+ " desc");

			loadItems(itemNodeList, cursor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadAudio(FileItemNodeList itemNodeList) {
		ContentResolver cr = mContext.getContentResolver();

		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

		String[] columns = new String[] { MediaStore.Audio.Media.DATA, MediaStore.Audio.Media._ID };

		try {
			Cursor cursor = cr.query(uri, columns, MediaStore.Audio.Media.DURATION + ">?",
					new String[] { "10000" }, null);
			loadItems(itemNodeList, cursor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
