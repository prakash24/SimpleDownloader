package com.example.IDM.download;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class DownloaderCopy implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Downloader.class);

    private long startByte;
    private long endByte;
    private long partSize;
    private final boolean resume;
    private int part;
    private URL url;
    private long downloadedSize;
    private long areadyDownloaded;
    private CountDownLatch latch;

    private final String fileName;

    public DownloaderCopy(URL url, long startByte, long endByte, long partSize,
            int part, boolean resume, CountDownLatch latch) {
        if (startByte >= endByte) {
            throw new RuntimeException("The start byte cannot be larger than "
                    + "the end byte!");
        }

        this.startByte = startByte;
        this.endByte = endByte;
        this.partSize = partSize;
        this.resume = resume;
        this.url = url;
        this.part = part;
        this.downloadedSize = 0;
        this.areadyDownloaded = 0;
        this.latch = latch;

        fileName = "." + (new File(url.toExternalForm()).getName() + ".part"
                + part);

        //thread = new Thread(this, "Part #" + part);

        // If resume a download then set the start byte
        if (resume) {
            try (RandomAccessFile partFile = new RandomAccessFile(fileName, "rw")) {
                areadyDownloaded = partFile.length();
                startByte += areadyDownloaded;
                downloadedSize += areadyDownloaded;
            } catch (IOException ex) {
                // If cannot open the part file, leave the start byte as it is
                // to download the entire part again.
            }
        }
    }

    public HttpURLConnection getHttpConnection() throws IOException {
        // Connect to the URL
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        String downloadRange = "bytes=" + startByte + "-" + endByte;
        conn.setRequestProperty("Range", downloadRange);
        conn.connect();

        // Return the connection.
        return conn;
    }

    public void downloadToFile(HttpURLConnection conn) throws IOException {
        // Get the input stream.
        InputStream is = conn.getInputStream();

        int chunkSize = (int) Math.pow(2, 12); // 4096 => IO Block Size on this machine

        try (DataInputStream dataIn = new DataInputStream(is)) {
            // Get the file's length.
            long contentLength = conn.getContentLengthLong();
            contentLength += areadyDownloaded;

            byte[] dataArray = new byte[chunkSize];
            int result;

            boolean overwrite = true;
            if (resume) {
                overwrite = false;
            }
            
            // While the total downloaded size is still smaller than the 
            // content length from the connection, keep reading data.
            while (downloadedSize < contentLength) {
                //Instant start = mProgress.startDownloadTimeStamp;
                result = dataIn.read(dataArray, 0, chunkSize);
                //Instant stop = Instant.now();
                //long time = Duration.between(stop, start).getNano();

                if (result == -1) {
                    break;
                }

                downloadedSize += result;
                writeToFile(dataArray, result, overwrite);
                overwrite = false;
            }
        }
        latch.countDown();
    }

    public void writeToFile(byte[] bytes, int bytesToWrite, boolean overwrite) throws IOException {
        try (FileOutputStream fout = new FileOutputStream(fileName, !overwrite)) {
            // Write to the output file using FileChannel.
            FileChannel outChannel = fout.getChannel();

            // Wrap the given byte array in a ByteBuffer.
            ByteBuffer data = ByteBuffer.wrap(bytes, 0, bytesToWrite);

            // Write the data.
            outChannel.write(data);
        }
    }
    
    public long getDownloadedSize() {
        return downloadedSize;
    }

    public long getPartSize() {
        return partSize;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection conn = getHttpConnection();
            downloadToFile(conn);
        } catch (IOException ex) {
        }
    }
    
    public String getFileName() {
    	return this.fileName;
    }

    //        //return CompletableFuture.completedFuture("File downloaded wait while you file is being download!! " + url);

  //PowerMock.mockStatic(URLConnector.class);
    //url = new URL(uri);
	
    //URL u = PowerMock.createMock(URL.class);
    //String url = "http://www.sdsgle.com";
    //andReturn(url);
    //expectNew(URL.class).thenReturn(url);
    //HttpURLConnection huc = PowerMock.createMock(HttpURLConnection.class);

	// ******
    //EasyMock.expect(URLConnector.getExternalName(anyObject(URL.class))).andReturn(fileName);
    //EasyMock.expect(URLConnector.openHTTPConnection(anyObject(URL.class))).andReturn(connection);
    //when(URLConnector.getExternalName(anyObject(URL.class))).thenReturn(fileName);
    //when(URLConnector.openHTTPConnection(anyObject(URL.class))).thenReturn(connection);
    
    //EasyMock.expect(URLConnector.getExternalName(anyObject(URL.class))).andReturn(fileName);
    //EasyMock.expect(URLConnector.openHTTPConnection(anyObject(URL.class))).andReturn(connection);
	
    //CountDownLatch latch = new CountDownLatch(OPTIMUM_THREAD_CNT);
//  for (int i = 0; i < optimalNumberOfThreads; i++) {
//  long beginByte = i * partSize;
//  long endByte;
//  if (i == optimalNumberOfThreads - 1) {
//      endByte = contentSize - 1;
//  } else {
//      endByte = (i + 1) * partSize - 1;
//  }
//  long currentPartSize = endByte - beginByte + 1;
//  int partNum = i + 1;
//  Downloader newDownloader = new Downloader(url, beginByte,
//          endByte, currentPartSize, partNum, latch);
//  downloadParts.add(newDownloader);
//  downloaderPool.execute(newDownloader);
//}

    
//  private CountDownLatch countDownLatch;
//private ExecutorService downloaderPool;
  //private URLConnector urlConnector ;
  //url = PowerMock.createMock(URL.class);
//downloaderPool = PowerMock.createMock(ExecutorService.class);
//countDownLatch = PowerMock.createMock(CountDownLatch.class);
//for(int i = 0; i < OPTIMUM_THREAD_CNT_PER_REQ; i++) {
//Capture<Downloader> capturedArgument = Capture.newInstance();
//downloaderPool.execute(anyObject(Downloader.class));
//downloaderPool.execute(EasyMock.capture(capturedArgument));
//EasyMock.expectLastCall().once().andAnswer(
//    new IAnswer() {
//        public Object answer() {
//            Downloader downloaders = (Downloader) EasyMock.getCurrentArguments()[0];
//            downloaders.getLatch().countDown();
//            return null;
//        }
//      }
//    );
//}
    //List<Downloader> downloadTasks = DownloadManager.getDownloaders(
    //       OPTIMUM_THREAD_CNT_PER_REQ, anyObject(URL.class), CONTENT_SIZE, fileName);

}