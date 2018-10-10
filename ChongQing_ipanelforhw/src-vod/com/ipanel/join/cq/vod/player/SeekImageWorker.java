package com.ipanel.join.cq.vod.player;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;

public class SeekImageWorker {
	ExecutorService mPool = Executors.newFixedThreadPool(3);
	
	private long lastTaskId = -1;
	private long displayedTaskId = -1;
	
	Handler uiHandler = new Handler(Looper.getMainLooper()){

		@Override
		public void handleMessage(Message msg) {
			imageView.setImageBitmap((Bitmap) msg.obj);
		}
		
	};
	
	ImageView imageView;
	
	public SeekImageWorker(ImageView imageView){
		this.imageView = imageView;
	}
	
	@Override
	protected void finalize() throws Throwable {
		mPool.shutdownNow();
		super.finalize();
	}

	List<Future<?>> pendingTask = new ArrayList<Future<?>>();
	public synchronized void load(String url){
		lastTaskId++;
		Iterator<Future<?>> it = pendingTask.iterator();
		while(it.hasNext()){
			it.next().cancel(false);
			it.remove();
		}
		Future<?> task = mPool.submit(new LoadTask(lastTaskId, url));
		pendingTask.add(task);
	}
	
	private class LoadTask implements Runnable{
		long taskId;
		String url;
		
		public LoadTask(long id, String url){
			this.taskId = id;
			this.url = url;
		}

		@Override
		public void run() {
			try{
				URLConnection connection = new URL(url).openConnection();
				connection.setConnectTimeout(2000);
				connection.setReadTimeout(3000);
				
				InputStream inputStream = connection.getInputStream();
				Bitmap bmp = BitmapFactory.decodeStream(inputStream);
				if(taskId >= displayedTaskId){
					Message msg = uiHandler.obtainMessage();
					msg.obj = bmp;
					msg.sendToTarget();
					displayedTaskId = taskId;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
}
