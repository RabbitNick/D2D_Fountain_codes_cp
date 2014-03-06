package cn.fudan.sonic.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingletonThreadPool {

	private static ThreadPoolExecutor threadPool;
	
	private SingletonThreadPool() {
		super();
	}

	public static ThreadPoolExecutor getThreadPool() {
		if(threadPool == null){
			synchronized (SingletonThreadPool.class) {
				if(threadPool == null){
					BlockingQueue<Runnable> bqueue = new ArrayBlockingQueue<Runnable>(20);
					threadPool = new ThreadPoolExecutor(2, 3, 2,
							TimeUnit.MILLISECONDS, bqueue);
				}
			}
		}	
		return threadPool;
	}
}
