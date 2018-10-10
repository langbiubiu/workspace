package com.ipanel.join.chongqing.live.manager;

import java.util.List;

import android.net.Uri;

import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.data.BookData;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;

/**
 * ��ĿԤԼ������
 * */
public abstract class BookManager{
	
	// ��ĿԤԼ��һЩ����
	public final static int BOOK_LIMIT_NUMBER = 60;
	public final static int BOOK_CONFICT_TIME = 30 * 1000;
	public final static Uri BOOK_CONTENT_URI = Uri
			.parse("content://"+Constant.AUTHORITY+"/bookchannels");
	/**
	 * ������һ�ν�ĿԤԼ
	 * */
	public abstract void caculateNextBook();
	/**
	 * ɾ��һ����ĿԤԼ
	 * */
	public abstract boolean deleteBook(long time,int service);
	/**
	 * ���һ����ĿԤԼ
	 * */
	public abstract boolean addBook(LiveChannel channel ,LiveProgramEvent event);
	/**
	 * ���һ����ĿԤԼ
	 * */
	public abstract boolean addBook(BookData bd);
	/**
	 * ĳ����Ŀ�Ƿ��ѱ�ԤԼ
	 * */
	public abstract boolean isProgramBooked(long time,int i);
	/**
	 * ԤԼ�ĸ����Ƿ�ﵽ���ֵ
	 * */
	public abstract boolean isMaxBookCount();
	/**
	 * ָ��ʱ����Ƿ��Ѿ���������ԤԼ��Ŀ
	 * */
	public abstract BookData hasConflictBook(long time);
	
	public abstract List<BookData> queryBookData();
	
	public abstract void setBookEventChangeListener(BookEventChangeListener l);
	
	public abstract void reMoveBookEventChangeListener(BookEventChangeListener l);
	
	public interface BookEventChangeListener {
		public void onBookEventChange();
	}

}
