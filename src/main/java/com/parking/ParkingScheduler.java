package com.parking;

import com.parking.service.FramesPoller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class ParkingScheduler {

    private final FramesPoller framePoller;
    private static final int noOfMessages = 5;
    @Qualifier("syncUserExecutor")
    private final ExecutorService syncUserExecutor;

    @Scheduled(fixedRateString = "1000")
    public void executeSqsPolling() {
        int freeThreads = noOfMessages - ((ThreadPoolExecutor) syncUserExecutor).getActiveCount();
        if (freeThreads > 0) {
            framePoller.executeTasks(freeThreads);
        }
    }
}