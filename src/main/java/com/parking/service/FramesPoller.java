package com.parking.service;

import com.parking.aws.SQSConsumer;
import com.parking.model.NotificationBean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class FramesPoller {

	@Qualifier("syncUserExecutor")
	private final ExecutorService syncUserExecutor;
	private final SQSConsumer<NotificationBean> sqsConsumer;
	private final DetectLicensePlate detectLicensePlate;
	
	public void executeTasks(int noOfThreads) {
		try {
			List<NotificationBean> notifications = sqsConsumer.receiveMessages(noOfThreads);
			if(null == notifications) {
				return;
			}
			for (NotificationBean notificationBean : notifications) {
				syncUserExecutor.submit(()-> migrateAndSyncFromNotification(notificationBean));
			}
		} catch (Exception e) {
			log.error("Exception while syncing account from queue", e);
		}
	}
	
	private void migrateAndSyncFromNotification(NotificationBean notificationBean) {
			log.info("Notification for location {}", notificationBean.getLocationId());
			detectLicensePlate.prcessFrames(notificationBean);
	}
}
