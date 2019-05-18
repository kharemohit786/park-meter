package com.parking.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQSConsumer<T> {
	
	final Class<T> typeParameterClass;
	private final String queueName;
	private final AmazonSQS sqsClient;
	private final ObjectMapper objectMapper;
	
	public SQSConsumer(String queueName, ObjectMapper objectMapper,Class<T> typeParameterClass) {
		super();
		this.queueName = queueName;
		this.objectMapper = objectMapper;
		this.typeParameterClass = typeParameterClass;
        this.sqsClient = AmazonSQSClient.builder().build();
	}
	
	private String getQueueUrl(){
		String myQueueUrl = sqsClient.createQueue(queueName).getQueueUrl();
    		return myQueueUrl;
    }
	
	private AmazonSQS getSqsClient() {
		return sqsClient;
	}
	
	public List<T> receiveMessages(int noOfMessages) {
		Optional<List<T>> msg = recieveMultiple(noOfMessages);
		if(msg.isPresent()){
			return msg.get();
		}
		return null;
	}
	
	private Optional<List<T>> recieveMultiple(int noOfMessages) {

        ReceiveMessageResult response = getSqsClient().receiveMessage(new ReceiveMessageRequest(getQueueUrl())
                .withMaxNumberOfMessages(noOfMessages).withWaitTimeSeconds(20));

        switch (response.getMessages().size()) {
            case 0:
                return Optional.empty();
            default:
                return Optional.ofNullable(decodeMessagesAndDeleteFromQueue(response.getMessages()));
        }

	}
	
	private List<T> decodeMessagesAndDeleteFromQueue(List<Message> messages) {
		delete(messages);
		return messages.stream().map(this::constructNewRetuenObject).filter(nb -> nb != null).collect(Collectors.toList());
	}
	
	private void delete(List<Message> messages) {
		List<DeleteMessageBatchRequestEntry> receiptHandleList = messages.stream().map(Message::getReceiptHandle).map(handle -> new DeleteMessageBatchRequestEntry(UUID.randomUUID().toString(), handle)).collect(Collectors.toList());
		getSqsClient().deleteMessageBatch(getQueueUrl(), receiptHandleList);
	}
	
	private T constructNewRetuenObject(Message message) {
		try {
			return objectMapper.readValue(message.getBody(), typeParameterClass);
		} catch (IOException e) {
			log.error("Could not deserialize message : {} , from the queue {}, because of exception", message.getBody(), queueName, e);
			return null;
		}
	}
}
