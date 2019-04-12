package com.example.IDM.download;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(EasyMockRunner.class)
public class DownloadTaskRunnerTest {

	@TestSubject
	DownloadTaskRunner downloadTaskRunner = new DownloadTaskRunner();
	
    @Mock private ExecutorService downloaderPool;

    private URL url;
    private final String uri = "https://speed.hetzner.de/100MB.bin";
    private final String fileName = "100MB.bin";
    private static final long CONTENT_SIZE = 1024L;

    @Before
    public void setup() {
    	downloadTaskRunner.setDownloaderPool(downloaderPool);
    }
	
    @Ignore
    @Test
	public void testExecutionLatch() throws InterruptedException, Exception {
        url = PowerMock.createNiceMockAndExpectNew(URL.class, uri);
		CountDownLatch latch = new CountDownLatch(2);
		List<Downloader> tasks = new ArrayList<>();
		tasks.add(new Downloader(url, 0, CONTENT_SIZE/2 -1, CONTENT_SIZE/2, 0, fileName + '0') {
			@Override
			public void run() {
				//super.run();
				latch.countDown();
			}
		});
		tasks.add(new Downloader(url, CONTENT_SIZE/2, CONTENT_SIZE - 1, CONTENT_SIZE/2, 1, fileName + '1') {
			@Override
			public void run() {
				latch.countDown();
			}
		});
        downloaderPool.execute(tasks.get(0));
        EasyMock.expectLastCall().once();
        downloaderPool.execute(tasks.get(1));
        EasyMock.expectLastCall().once();
		downloadTaskRunner.executeTasks(tasks);
		latch.await();
	}
}


