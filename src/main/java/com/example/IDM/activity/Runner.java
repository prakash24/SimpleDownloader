package com.example.IDM.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.IDM.download.DownloadManager;

import java.util.concurrent.CompletableFuture;

@Component
public class Runner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    private final DownloadManager downloadManagerService;

    public Runner(DownloadManager downloadManagerService) {
        this.downloadManagerService = downloadManagerService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Start the clock
        long start = System.currentTimeMillis();

        // Kick of multiple, asynchronous lookups
        //CompletableFuture<User> page1 = gitHubLookupService.findUser("PivotalSoftware");
        //CompletableFuture<User> page2 = gitHubLookupService.findUser("CloudFoundry");
        
        // Wait until they are all done
        //CompletableFuture.allOf(page1,page2,).join();

        // Print results, including elapsed time
        logger.info("Elapsed time: " + (System.currentTimeMillis() - start));
        //logger.info("--> " + page1.get());
        //logger.info("--> " + page2.get());

    }

}