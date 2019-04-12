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

import com.example.IDM.util.aspect.Timed;

public class Downloader implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Downloader.class);
    // => IO Block Size on this machine
    private static final int systemIOBlockSize = 4096; 
    private long startByte;
    private long endByte;
    private long partSize;
    private final boolean resume;
    private URL url;
    private long downloadedSize;
    private long areadyDownloaded;
    private CountDownLatch latch;

    private String fileName;
    
    public Downloader(URL url, long startByte, long endByte, long partSize,
            int part, String outputFileName) {
        if (startByte >= endByte) {
            throw new RuntimeException("The start byte cannot be larger than "
                    + "the end byte!");
        }

        this.startByte = startByte;
        this.endByte = endByte;
        this.partSize = partSize;
        this.resume = false;
        this.url = url;
        this.downloadedSize = 0;
        this.areadyDownloaded = 0;
        this.fileName = "." + outputFileName + ".part" + part;
    }

    @Timed
    private void downloadToFile(HttpURLConnection conn) throws IOException {
        InputStream is = conn.getInputStream();
        int chunkSize = systemIOBlockSize; 

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
            while (downloadedSize < contentLength) {
                result = dataIn.read(dataArray, 0, chunkSize);
                if (result == -1) {
                    break;
                }
                downloadedSize += result;
                writeToFile(dataArray, result, overwrite);
                overwrite = false;
            }
        }
        finally {
            latch.countDown();
        }
    }

    public void writeToFile(byte[] bytes, int bytesToWrite, boolean overwrite) throws IOException {
        try (FileOutputStream fout = new FileOutputStream(fileName, !overwrite)) {
            FileChannel outChannel = fout.getChannel();
            ByteBuffer data = ByteBuffer.wrap(bytes, 0, bytesToWrite);
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
    
    public CountDownLatch getLatch() {
    	return this.latch;
    }
    
    public String getFileName() {
    	return this.fileName;
    }
    
    public void setLatch(CountDownLatch latch) {
        this.latch=latch;
    }
    
    private HttpURLConnection getHttpConnection() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        String downloadRange = "bytes=" + startByte + "-" + endByte;
        conn.setRequestProperty("Range", downloadRange);
        conn.connect();
        return conn;
    }
    
    public void resumeDownload() {
        try (RandomAccessFile partFile = new RandomAccessFile(this.fileName, "rw")) {
            areadyDownloaded = partFile.length();
            startByte += areadyDownloaded;
            downloadedSize += areadyDownloaded;
        } catch (IOException ex) {
            // If cannot open the part file, leave the start byte as it is
            // to download the entire part again.
        }
        
    }

}