package com.ipanel.chongqing_ipanelforhw.downloading.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * �̳߳ظ����࣬����Ӧ�ó����ֻ��һ���̳߳�ȥ�����̡߳� �������ú����߳���������߳����������߳̿�״̬����ʱ�䣬�������г������Ż��̳߳ء�
 * ��������ݶ��ǲο�Android��AsynTask������ݡ�
 * 
 * @author zhoulc
 * 
 */
public class ThreadPoolUtils {

	private ThreadPoolUtils() {

	}

	/* �̳߳غ����߳��� */
	private static int CORE_POOL_SIZE = 3;

	/* �̳߳�����߳��� */
	private static int MAX_POOL_SIZE = 30;

	/* �����߳̿�״̬����ʱ�� */
	private static int KEEP_ALIVE_TIME = 10000;

	/* �������У��������̶߳������ã�������������������£��ŻῪ�������߳� */
	private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(
			40);

	/* �̹߳��� */
	private static ThreadFactory threadFactory = new ThreadFactory() {
		private final AtomicInteger integer = new AtomicInteger();

		@Override
		public Thread newThread(Runnable r) {
			// TODO Auto-generated method stub
			return new Thread(r, "myThreadPool thread:"
					+ integer.getAndIncrement());
		}
	};

	/* �̳߳� */
	private static ThreadPoolExecutor threadPool;

	static {
		threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
				KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue, threadFactory);
	}

	/**
	 * ���̳߳��г�ȡ�̣߳�ִ��ָ����Runnable����
	 * 
	 * @param runnable
	 */
	public static void execute(Runnable runnable) {
		if (!threadPool.isShutdown()) {
			threadPool.execute(runnable);
		}
	}

	public static void removeTask(Runnable task) {
		threadPool.remove(task);
	}
}