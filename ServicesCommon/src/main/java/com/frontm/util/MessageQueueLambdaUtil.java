package com.frontm.util;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.MessageQueue;
import com.frontm.domain.MessageQueue.Item;
import com.frontm.domain.MessageQueue.Item.Content;

public class MessageQueueLambdaUtil extends LambdaUtil {
	private static final Logger logger = Logger.getLogger(MessageQueueLambdaUtil.class);

	private static final String MSG_Q_CONTENT_TYPE = "150";
	private static final String MSG_Q_RET_VAL = "NONE";
	private static final String MSG_Q_CREATED_BY = "AgentM";

	public static void invokeLambda(FrontMRequest request, final InvokeRequest invokeRequest) throws Exception {
		final AWSLambdaAsync client = AWSLambdaAsyncClientBuilder.defaultClient();
		InvokeResult invokeResult = null;
		try {
			invokeResult = client.invoke(invokeRequest);
		} catch (Exception e) {
			final InvokeRequest errorInput = logAndCreateErrorLambdaInput(request, e,
					"Error while invoking mesage queue lambda");
			invokeResult = client.invoke(errorInput);
		}
		logger.info("After calling message queue lambda: " + invokeResult.getStatusCode());
	}

	public static InvokeRequest logAndCreateErrorLambdaInput(FrontMRequest request, Exception e, String logMsg)
			throws JsonProcessingException {
		final String errorMessage = e.getMessage();
		logger.info(logMsg + ": " + errorMessage);
		return createLambdaInput(request, errorMessage, false);
	}

	public static InvokeRequest createLambdaInput(FrontMRequest request, final String webServiceResponse,
			boolean isOKResponse) throws JsonProcessingException {
		MessageQueue messageQueue = createMessageQueueResponse(request, webServiceResponse, isOKResponse);
		final String msgQueueJson = new ObjectMapper().writeValueAsString(messageQueue);
		logger.info(msgQueueJson);

		return createInvokeRequest(System.getenv("MSG_FUNCTION"), msgQueueJson);
	}

	private static MessageQueue createMessageQueueResponse(FrontMRequest frontMRequest, final String webServiceResponse,
			boolean isOKResponse) {
		final Content content = new Content();
		content.setContentType(MSG_Q_CONTENT_TYPE);
		if (isOKResponse) {
			content.setDetails(webServiceResponse);
		} else {
			content.setError(webServiceResponse);
		}

		final Item item = new Item();
		item.setCreatedOn(System.currentTimeMillis());
		item.setContent(content);
		item.setNotifyToOwner(true);
		item.setCreatedBy(MSG_Q_CREATED_BY);
		item.setUserUuid(frontMRequest.getUserUuid());
		item.setConversation(frontMRequest.getConversation().getUuid());
		item.setPush(frontMRequest.getPush());

		final MessageQueue messageQueue = new MessageQueue();
		messageQueue.setTableName(System.getenv("MSG_Q_TABLE_NAME"));
		messageQueue.setReturnValues(MSG_Q_RET_VAL);
		messageQueue.setItem(item);
		return messageQueue;
	}
}
