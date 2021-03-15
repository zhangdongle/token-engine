package com.mchain.tokenengine.config;

import java.util.concurrent.*;

public class ThreadPool {

	public static ExecutorService pool = new ThreadPoolExecutor(20, 50, 1000, TimeUnit.MILLISECONDS,
			new SynchronousQueue<Runnable>(), new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread th = new Thread(r, "threadPool:" + r.hashCode());
			return th;
		}
	}, new ThreadPoolExecutor.CallerRunsPolicy() {

	});

	public static void execute(Runnable runnable) {
		pool.execute(runnable);
	}

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println("哈哈哈");
				}
			});
		}

	}
}
