package com.ipanel.join.chongqing.live.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import android.os.Handler;
import android.os.Message;

import cn.ipanel.android.LogHelper;

public class TaskManager {
	private static TaskManager mInstance;
	private Stack<Task> tasks = new Stack<Task>();
	private List<Task> olds = new ArrayList<Task>();
	private Object mute = new Object();
	private TaskComparor comparor = new TaskComparor();// the sort comparor;
	private TaskHandler handler;// the function how to handle task;
	private Task current = new Task(0, 0, -1);// the task that is
													// handling;
	private Task defaults = new Task(0, 0, -1);// the task that is
	private boolean readyFlag = false;
	private boolean listenerFlag = false;

	// handling;
	public static synchronized TaskManager getInstance() {
		if (mInstance == null)
			mInstance = new TaskManager();
		return mInstance;
	}
	
	public Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case 0:
				LogHelper.i("time up for tast :");
				Task t=(Task) msg.obj;
				if(msg.obj!=null){
					LogHelper.i("task info : ");
					LogHelper.i("type "+t.type);
					LogHelper.i("id "+t.id);
				}
				next();
				break;
			}
		};
	};

	private TaskManager() {
		new TaskThread().start();
	}

	public void setTaskHandler(TaskHandler handler) {
		this.handler = handler;
	}

	public void insertTask(Task task, int start, int end, int size) {// add one
		synchronized (mute) {
			if (!tasks.contains(task) && !current.equals(task)) {
				olds.clear();
				int length = tasks.size();
				for (int i = 0; i < length; i++) {
					if (!tasks.get(i).valid(start, end, size)) {
						olds.add(tasks.get(i));
					}
				}
				for (Task t : olds) {
					LogHelper.i("remove task: " + t.id);
					tasks.remove(t);
				}
				LogHelper.i("will insert task , index " + task.id);
				tasks.add(task);

			} else {

			}
			Collections.sort(tasks, comparor);
		}
		notifyTask();

	}

	public void insertTask(Task task) {// add one task that get all events of a
										// channel;
		synchronized (mute) {
			if (!tasks.contains(task) && !current.equals(task)) {
				olds.clear();
				tasks.clear();
				LogHelper.i("remove all task for task epg event ,prog:" + task.id);
				tasks.add(task);
			}
		}
		notifyTask();
	}

	public void clearTask() {// add one task that get all events of a
		// channel;
		synchronized (mute) {
			olds.clear();
			tasks.clear();
		}
		notifyTask();
	}

	public void intsertTasks(Task[] in_tasks, int start, int size) {
		synchronized (mute) {
			olds.clear();
			tasks.clear();
			for (int i = 0; i < size; i++) {
				tasks.push(in_tasks[size]);
			}
			Collections.sort(tasks, comparor);
		}
		notifyTask();
	}

	public void next() {
		LogHelper.e("next task");
		current = defaults;
		notifyTask();

	}

	public void notifyTask() {
		if (tasks.isEmpty() || !readyFlag
				|| !current.equals(defaults)) {
			LogHelper.i("tasks.isEmpty():"+tasks.isEmpty());
			LogHelper.i("readyFlag:"+readyFlag);
			LogHelper.i("listenerFlag:"+listenerFlag);
			LogHelper.i("current.equals(defaults) :"+current.equals(defaults));

		} else {
			LogHelper.i("notify the task ");
			synchronized (lock) {
				lock.notify();
			}
		}
	}

	public void readyOk() {
		LogHelper.e("tv ready ok");
		readyFlag = true;
		notifyTask();
	}

	public void listenerOk() {
		LogHelper.e("listener ready ok");
		listenerFlag = true;
		notifyTask();
	}

	public void listenerNo() {
		LogHelper.e("listener ready no");
		listenerFlag = false;
	}

	public void unReady() {
		LogHelper.e("tv release");
		readyFlag = false;
	}

	public Task getTopTask() {
		if (tasks.isEmpty()) {
			return null;
		} else {
			return tasks.pop();
		}
	}

	public void resetManager() {
		synchronized (mute) {
			LogHelper.i("reset task manager");
			tasks.clear();
			current = defaults;
		}
		notifyTask();
	}

	private Object lock = new Object();

	class TaskThread extends Thread {
		boolean runFlag = true;

		@Override
		public void run() {
			while (runFlag) {
				try {
					synchronized (lock) {
						lock.wait();
					}
					synchronized (mute) {
						mHandler.removeMessages(0);
						Task t = tasks.pop();
						LogHelper.i("pop a task of type: " + t.type + " and id: "
								+ t.id);
						current = t;
						handler.doTask(t);
						Message msg=mHandler.obtainMessage(0);
						msg.obj=t;
						mHandler.sendMessageDelayed(msg, 5000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			super.run();
		}
	}

	class TaskComparor implements Comparator<Object> {

		public int compare(Object arg0, Object arg1) {
			Task program0 = (Task) arg0;
			Task program1 = (Task) arg1;
			if (program0.type == program1.type) {
				return -program0.id + program1.id;
			} else {
				return program0.type - program1.type;
			}
		}
	}

	public interface TaskHandler {
		public void doTask(Task task);
	}
}
