package com.example.IDM.download;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.IDM.types.DownloadFailedException;
import com.example.IDM.types.HttpResult;
import com.example.IDM.util.aspect.Timed;
import com.google.common.annotations.VisibleForTesting;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Setter;

@Service
public class DownloadManager {
    
   
    @Autowired
    @Setter private DownloadTaskRunner downloadTaskRunner;
    
    private static final Logger log = LoggerFactory.getLogger(DownloadManager.class);
    
    @VisibleForTesting
    static final String outputDir = "/tmp/downloaded/";
    
    @Timed
    public String download(String uri, Integer overridenThreadCount) throws DownloadFailedException{
        try {
            URL url = new URL(uri);
            String fileName = new File(url.toExternalForm()).getName();
            HttpResult result = checkURLValidity(url);
            long contentSize = result.contentLength;
            log.info("contentSize {} ", result.contentLength/1024/1024 + "MB" );
            int optimalNumberOfThreads = (overridenThreadCount != null)? overridenThreadCount:findOptimalThreadCount(contentSize, url);
            List<Downloader> downloadParts  = getDownloaders(optimalNumberOfThreads, url, contentSize, fileName); 
            try {
                downloadTaskRunner.executeTasks(downloadParts);
            } catch(Exception ex) {
                log.error("Download failing, will retry once again");
            }
            downloadParts = getUnfinishedParts(downloadParts);
            downloadTaskRunner.executeTasks(downloadParts); //Retry Once more on threads that failed- A simple approach to show retry/resume as use-case/requirement :) is not very clear. 
            joinDownloadedParts(fileName, downloadParts);
            deleteParts(downloadParts);
        } catch (RuntimeException | InterruptedException | IOException ex) {
            log.error("Unable to download file due to failed Network Connection with all retires been done. ", ex);
            throw new DownloadFailedException(ex.getMessage());
        }
        // Notify user to find the file
        return "File downloaded. Find on disk!!";

    }
    
    private void joinDownloadedParts(String fileName, List<Downloader> downloadParts) throws IOException {
        String outputFile = outputDir + fileName;
        
        try (RandomAccessFile mainFile = new RandomAccessFile(outputFile, "rw")) {
            FileChannel mainChannel = mainFile.getChannel();
            long startPosition = 0;

            for (int i = 0; i < downloadParts.size(); i++) {
                String partName = downloadParts.get(i).getFileName();

                try (RandomAccessFile partFile = new RandomAccessFile(partName, "rw")) {
                    long partSize = downloadParts.get(i).getDownloadedSize();
                    FileChannel partFileChannel = partFile.getChannel();
                    long transferedBytes = mainChannel.transferFrom(partFileChannel,
                            startPosition, partSize);

                    startPosition += transferedBytes;

                    if (transferedBytes != partSize) {
                        throw new RuntimeException("Error joining file! At part: "
                                + (i + 1));
                    }
                }
            }
        }
    }
    
    private void  deleteParts(List<Downloader> downloadParts) {
        for(int i=0;i<downloadParts.size() ;i++) {
            try {
                new File(downloadParts.get(i).getFileName()).delete();
            } catch(Exception ex) {
                log.warn("Unable to delete part " + downloadParts.get(i).getFileName());
            }
        }
    }
    
    /*
     * Assume 512 kbps internet download speed. Then in order to download 100 MB file sequentially, 
     * it would take 200 seconds. Now if all of it is wanted in 1 second, it would require 200 threads to be spawne
     * with each thread downloading 512 KB of data chunk or 512Kb/ 4096  = 128 blocks being read in thread.
     * Now spwaning these many number of threads in not feasible, thus take min(calcualated, 16). Thus each 
     * user request (api call) can spawn at max 16 threads. 
     * For now assume it is 16 by default
     * long countOfBlocks = contentSize / 4096; //4096 = block size
     * 
     * Keeping this static for now as this is an area of reading/researching and should be open for discussion.
     */
    
    @VisibleForTesting
    int findOptimalThreadCount(long contentSize, URL url) {
        return 16;
    }
    
    @VisibleForTesting
    static List<Downloader> getDownloaders(int optimalNumberOfThreads, URL url,
    		long contentSize, String fileName) {
        long partSize = contentSize / optimalNumberOfThreads;

        List<Downloader> downloaders = new ArrayList<Downloader>();

        for (int i = 0; i < optimalNumberOfThreads; i++) {
            long beginByte = i * partSize;
            long endByte;
            if (i == optimalNumberOfThreads - 1) {
                endByte = contentSize - 1;
            } else {
                endByte = (i + 1) * partSize - 1;
            }
            long currentPartSize = endByte - beginByte + 1;
            int partNum = i + 1;
            Downloader newDownloader = new Downloader(url, beginByte,
                    endByte, currentPartSize, partNum, fileName);
            downloaders.add(newDownloader);
        }
        return downloaders;
    }
    
    private HttpResult checkURLValidity(URL url) throws ConnectException {
        try {

            //HttpURLConnection conn = (HttpURLConnection) URLConnector.openHTTPConnection(url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("HEAD");
            conn.connect();

            int responseCode = conn.getResponseCode();
            long contentSize = conn.getContentLengthLong();

            HttpResult result = new HttpResult(responseCode, contentSize);
            
            if (contentSize == -1 || responseCode != 200) {
                String errMessage = "Error while checking URL validity!";
                errMessage += "\nResponse code: " + responseCode;
                errMessage += "\nContent size: " + contentSize;
                throw new RuntimeException(errMessage);
            }
            return result;
        } catch (IOException ex) {
            throw new ConnectException(ex.getMessage());
        }
    }
    
    private List<Downloader> getUnfinishedParts(List<Downloader> downloadParts) throws InterruptedException{
        List<Downloader> unfinished = new ArrayList<>();
        for(Downloader downloadThread: downloadParts) {
            if(downloadThread.getPartSize() > downloadThread.getDownloadedSize()) {
                unfinished.add(downloadThread);
                downloadThread.resumeDownload();
            }
        }
        return unfinished;
    }
}