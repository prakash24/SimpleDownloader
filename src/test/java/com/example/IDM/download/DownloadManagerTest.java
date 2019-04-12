package com.example.IDM.download;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.example.IDM.types.DownloadFailedException;

import org.easymock.TestSubject;
import org.easymock.Capture;
import org.easymock.EasyMock;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.List;

import org.powermock.api.easymock.PowerMock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(URL.class)
public class DownloadManagerTest {

    @TestSubject
    DownloadManager sut = new DownloadManager();

    private HttpURLConnection connection;

    private RandomAccessFile outputFile;
    
    private FileChannel mainChannel;
    
    private DownloadTaskRunner downloadTaskRunner;
    
    private URL url;

    private final String uri = "https://speed.hetzner.de/100MB.bin";
    private static final String FILE_NAME = "100MB.bin";
    private static final int OPTIMUM_THREAD_CNT_PER_REQ = 16;
    private static final long CONTENT_SIZE = 1024 * 1024L;
    private static String OUTPUT_FILE = DownloadManager.outputDir + FILE_NAME;

    
    @Before
    public void setup() throws Exception{
        connection = PowerMock.createMock(HttpURLConnection.class);
        downloadTaskRunner = PowerMock.createMock(DownloadTaskRunner.class);
        sut.setDownloadTaskRunner(downloadTaskRunner);
        url = PowerMock.createNiceMockAndExpectNew(URL.class, uri);
        outputFile = PowerMock.createNiceMockAndExpectNew(RandomAccessFile.class, OUTPUT_FILE, "rw");
        mainChannel = PowerMock.createNiceMockAndExpectNew(FileChannel.class);
    }

    @Test
    public void download_success() throws IOException, InterruptedException, DownloadFailedException, Exception{
        PowerMock.expectNew(URL.class, uri).andReturn(url);
        EasyMock.expect(url.toExternalForm()).andReturn(FILE_NAME).once();
        EasyMock.expect(url.openConnection()).andReturn(connection).once();
                List<Downloader> downloaders = DownloadManager.getDownloaders(OPTIMUM_THREAD_CNT_PER_REQ, url, CONTENT_SIZE, FILE_NAME);
        Capture<List<Downloader>> capturedArgument = Capture.newInstance();
       
        downloadTaskRunner.executeTasks(EasyMock.capture(capturedArgument));
        EasyMock.expectLastCall().once();
        EasyMock.replay(connection);
        
        EasyMock.expect(outputFile.getChannel()).andReturn(mainChannel).once();
        long startPosition = 0;
        for (int i = 0; i < downloaders.size(); i++) {
            String partName = downloaders.get(i).getFileName();
            
            RandomAccessFile partFile = PowerMock.createNiceMockAndExpectNew(RandomAccessFile.
            		class, partName, "rw");
            long partSize = 65536 * i; //1024*1024/16 // downloaders.get(i).getDownloadedSize();
            FileChannel partFileChannel = PowerMock.createMock(FileChannel.class);
            EasyMock.expect(partFile.getChannel()).andReturn(partFileChannel);
            long transferedBytes = 65536;
            EasyMock.expect(mainChannel.transferFrom(partFileChannel, startPosition, partSize)).
            		andReturn(transferedBytes);
        }
        
        for(int i=0;i<downloaders.size() ;i++) {
            File file = PowerMock.createNiceMockAndExpectNew(File.class, downloaders.get(i).getFileName());
            EasyMock.expect(file.delete()).andReturn(true);
        
        }
        
        String result = sut.download(uri, OPTIMUM_THREAD_CNT_PER_REQ);
        EasyMock.verify(connection);
        assertEquals(result, "File downloaded. Find on disk!!");
        
    }
}
