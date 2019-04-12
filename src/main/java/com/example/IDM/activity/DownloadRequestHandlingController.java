package com.example.IDM.activity;

import org.springframework.web.bind.annotation.RestController;

import com.example.IDM.download.DownloadManager;
import com.example.IDM.types.DownloadFailedException;
import com.example.IDM.util.aspect.Timed;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class DownloadRequestHandlingController implements ErrorController{

    @Autowired
    DownloadManager downloadManager;
    
    private static final String PATH = "/error";

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public String download(@RequestParam String url, @RequestParam(required = false) Integer threadCount) throws 
                    ExecutionException, DownloadFailedException, InterruptedException {
        return downloadManager.download(url, threadCount);
    }
    
    @RequestMapping(value = "/error")
    public String download() {
        return "Something Went Wong!! Please check the URL or contact app admin";
    }
    
    @Override
    public String getErrorPath() {
        return PATH;
    }
}