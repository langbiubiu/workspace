package com.ipanel.join.chongqing.live.manager;

import java.util.List;

import android.net.Uri;

import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.data.BookData;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;

/**
 * 节目预约管理类
 * */
public abstract class BookManager{
	
	// 节目预约的一些常量
	public final static int BOOK_LIMIT_NUMBER = 60;
	public final static int BOOK_CONFICT_TIME = 30 * 1000;
	public final static Uri BOOK_CONTENT_URI = Uri
			.parse("content://"+Constant.AUTHORITY+"/bookchannels");
	/**
	 * 计算下一次节目预约
	 * */
	public abstract void caculateNextBook();
	/**
	 * 删除一个节目预约
	 * */
	public abstract boolean deleteBook(long time,int service);
	/**
	 * 添加一个节目预约
	 * */
	public abstract boolean addBook(LiveChannel channel ,LiveProgramEvent event);
	/**
	 * 添加一个节目预约
	 * */
	public abstract boolean addBook(BookData bd);
	/**
	 * 某个节目是否已被预约
	 * */
	public abstract boolean isProgramBooked(long time,int i);
	/**
	 * 预约的个数是否达到最大值
	 * */
	public abstract boolean isMaxBookCount();
	/**
	 * 指定时间点是否已经有了其他预约节目
	 * */
	public abstract BookData hasConflictBook(long time);
	
	public abstract List<BookData> queryBookData();
	
	public abstract void setBookEventChangeListener(BookEventChangeListener l);
	
	public abstract void reMoveBookEventChangeListener(BookEventChangeListener l);
	
	public interface BookEventChangeListener {
		public void onBookEventChange();
	}

}
