package com.example.IDM.download;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.Setter;

@Component
public class DownloadTaskRunner {

	@Autowired
    @Qualifier("cachedThreadPool")
    @Setter private ExecutorService downloaderPool;

	public void executeTasks(List<Downloader> tasks) throws InterruptedException{
		CountDownLatch latch = new CountDownLatch(tasks.size());
		for (Downloader task : tasks) {
			task.setLatch(latch);
			downloaderPool.execute(task);
		}
        latch.await();
	}
}
