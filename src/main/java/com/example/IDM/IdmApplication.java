package com.example.IDM;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class IdmApplication {

    private static final int runnerThreadPoolSize = 32;

    public static void main(String[] args) {
        SpringApplication.run(IdmApplication.class, args);
    }
    @Bean(name="cachedThreadPool", destroyMethod = "shutdown")
    public ExecutorService cachedThreadPool() {
        ExecutorService executorService = Executors.newFixedThreadPool(runnerThreadPoolSize);
        return executorService;
    }
}
