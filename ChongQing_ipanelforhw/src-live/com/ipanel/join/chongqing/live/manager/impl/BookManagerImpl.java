package com.ipanel.join.chongqing.live.manager.impl;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;
import cn.ipanel.android.LogHelper;
import com.ipanel.join.chongqing.live.LiveApp;
import com.ipanel.join.chongqing.live.book.Alarms;
import com.ipanel.join.chongqing.live.data.BookData;
import com.ipanel.join.chongqing.live.manager.BookManager;
import com.ipanel.join.chongqing.live.manager.IManager;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;
import com.ipanel.join.chongqing.live.provider.TableUtils;

public class BookManagerImpl extends BookManager {

	private List<BookData> mBookEvent = new ArrayList<BookData>();

	private List<BookEventChangeListener> mChangeListenerList = new ArrayList<BookManager.BookEventChangeListener>();

	private Context mContext;

	public BookManagerImpl(Context context) {
		this.mContext = context;
		LiveApp.getInstance().post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mContext.getContentResolver().registerContentObserver(
						BOOK_CONTENT_URI, false, mBookChange);
			}
		});

	}

	@Override
	public void caculateNextBook() {
		// TODO Auto-generated method stub
		Alarms.setNextAlert(mContext);
	}

	@Override
	public boolean isProgramBooked(long time, int i) {
		// TODO Auto-generated method stub
		for (BookData book : mBookEvent) {
			if (book.getStart_time().equals(time + "")
					&& book.getProgram_number() == i) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean deleteBook(long time, int service) {
		// TODO Auto-generated method stub
		try {
			TableUtils.delete(mContext, BOOK_CONTENT_URI, "program_number="
					+ "'" + service + "'" + " AND " + "start_time=" + "'"
					+ time + "'", null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public boolean addBook(LiveChannel channel, LiveProgramEvent event) {
		// TODO Auto-generated method stub
		BookData book = new BookData();
		book.setChannel_name(channel.getName());
		book.setChannel_number(channel.getChannelNumber() + "");
		book.setDuration(event.getEnd() - event.getStart() + "");
		book.setEvent_name(event.getName());
		book.setFrequency(channel.getChannelKey().getFrequency());
		book.setProgram_number(channel.getChannelKey().getProgram());
		book.setStart_time(event.getStart() + "");
		try {
			TableUtils.insert(mContext, BOOK_CONTENT_URI, book);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isMaxBookCount() {
		return mBookEvent.size() >= BOOK_LIMIT_NUMBER;

	}

	@Override
	public BookData hasConflictBook(long time) {
		for (int i = 0; i < mBookEvent.size(); i++) {
			if (Math.abs(time
					- Long.parseLong(mBookEvent.get(i).getStart_time())) <= BOOK_CONFICT_TIME) {
				return mBookEvent.get(i);
			}
		}
		return null;
	}

	@Override
	public void setBookEventChangeListener(BookEventChangeListener l) {
		// TODO Auto-generated method stub
		if(l!=null){
			mChangeListenerList.add(l);
		}
	}
	
	@Override
	public void reMoveBookEventChangeListener(BookEventChangeListener l) {
		if (l!=null) {
			mChangeListenerList.remove(l);
		}
		
	}

	private ContentObserver mBookChange = new ContentObserver(new Handler()) {
		public void onChange(boolean selfChange) {
			mBookEvent = TableUtils.query(mContext, BOOK_CONTENT_URI,BookData.class);
			caculateNextBook();
			if (mChangeListenerList != null&&mChangeListenerList.size()>0) {
				for(BookEventChangeListener l:mChangeListenerList){
					l.onBookEventChange();
				}
			}
			LogHelper.i("----book event size change :" + mBookEvent.size());
		};
	};

	@Override
	public List<BookData> queryBookData() {
		// TODO Auto-generated method stub
		List<BookData> datas = new ArrayList<BookData>();
		
		try{
			datas = TableUtils.query(mContext, BOOK_CONTENT_URI,BookData.class);
			return datas;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public boolean addBook(BookData bd) {
		// TODO Auto-generated method stub
//		BookData book = new BookData();
//		book.setChannel_name(bd);
//		book.setChannel_number(channel.getChannelNumber() + "");
//		book.setDuration(event.getEnd() - event.getStart() + "");
//		book.setEvent_name(event.getName());
//		book.setFrequency(channel.getChannelKey().getFrequency());
//		book.setProgram_number(channel.getChannelKey().getProgram());
//		book.setStart_time(event.getStart() + "");
		try {
			TableUtils.insert(mContext, BOOK_CONTENT_URI, bd);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}



}
