package com.mamiyaotaru.voxelmap.persistent;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadManager {
	static final int concurrentThreads = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
	static final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
	public static ThreadPoolExecutor executorService = new ThreadPoolExecutor(concurrentThreads, concurrentThreads, 0L, TimeUnit.MILLISECONDS, queue);

	static {
		executorService.setThreadFactory(new ThreadManager.NamedThreadFactory("Voxelmap WorldMap Calculation Thread"));
	}

	public static void emptyQueue() {
		for (Runnable runnable : queue) {
			if (runnable instanceof FutureTask) {
				((FutureTask) runnable).cancel(false);
			}
		}

		executorService.purge();
	}

	private static class NamedThreadFactory implements ThreadFactory {
		private final String name;
		private final AtomicInteger threadCount = new AtomicInteger(1);

		public NamedThreadFactory(String name) {
			this.name = name;
		}

		@Override
		public Thread newThread(Runnable runnable) {
			return new Thread(runnable, this.name + " " + this.threadCount.getAndIncrement());
		}
	}
}
