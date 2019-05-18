package com.parking.aws;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQSProducer<T> {
	
	private final String queueName;
	final Class<T> typeParameterClass;
	private final AmazonSQS sqsClient;
	private final ObjectMapper objectMapper;
	
	public SQSProducer(String queueName, ObjectMapper objectMapper,Class<T> typeParameterClass) {
		super();
		this.queueName = queueName;
		this.objectMapper = objectMapper;
		this.typeParameterClass = typeParameterClass;
		this.sqsClient = AmazonSQSClient.builder().build();
	}
	
	private String getQueueUrl(){
		return sqsClient.createQueue(queueName).getQueueUrl();
    }
	
	public String send(T message) throws JsonProcessingException {
		return sendSerializableInternal(message);
	}

	public String sendSerializableInternal(T message) throws JsonProcessingException {
		String encodedMessage;
		encodedMessage = objectMapper.writeValueAsString(message);
		if(encodedMessage.length() > 64 * 1024){
			throw new IllegalStateException();
		}
		log.debug("Serialized Message: {}", encodedMessage);

		SendMessageRequest request = new SendMessageRequest(getQueueUrl(), encodedMessage);
		try {
			return sqsClient.sendMessage(request).getMessageId();
		} catch (AmazonServiceException e) {
			String errorMessage = "Could not sent message to SQS queue: " + queueName
					+ ". Retrying.";
			log.warn(errorMessage, e);
			throw e;
		}
	}
	
}
