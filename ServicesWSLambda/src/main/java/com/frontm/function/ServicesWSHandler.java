package com.frontm.function;

import static com.frontm.util.WebServiceUtil.createWebserviceCall;
import static com.frontm.util.WebServiceUtil.getWebserviceResponse;
import static com.frontm.util.WebServiceUtil.parseWebServiceResponse;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontm.domain.APIParameters;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.FrontMRequest.Parameters;
import com.frontm.domain.MessageQueue;
import com.frontm.domain.ServicesWSInput;


public class ServicesWSHandler implements RequestHandler<ServicesWSInput, Void> {
	private static final Logger logger = Logger.getLogger(ServicesWSHandler.class);
	private static final String CONTENT_TYPE_150 = "150";
	
	@Override
	public Void handleRequest(ServicesWSInput input, Context context) {
		logger.debug("Input parameters in the request: " + input);

		try {
			// call the webservice
			final APIParameters apiParams = input.getApiParameters();
			final FrontMRequest request = input.getRequest();

			Invocation.Builder invocationBuilder = createWebserviceCall(request, apiParams);
			Response response = getWebserviceResponse(request, apiParams, invocationBuilder);
			final String webServiceResponse = parseWebServiceResponse(apiParams, response);

			InvokeRequest invokeRequest = createLambdaInput(input, webServiceResponse);
			final InvokeResult invoke = AWSLambdaAsyncClientBuilder.defaultClient().invoke(invokeRequest);
			logger.info("After calling message queue function: " + invoke.getStatusCode());
		} catch (Exception e) {
			logger.error("Error occured:", e);
		}
		return null;
	}

	private InvokeRequest createLambdaInput(ServicesWSInput input, final String webServiceResponse)
			throws JsonProcessingException {
		MessageQueue messageQueue = createMessageQueueResponse(input.getRequest(), webServiceResponse);
		final String msgQueueJson = new ObjectMapper().writeValueAsString(messageQueue);
		logger.info(msgQueueJson);

		InvokeRequest invokeRequest = new InvokeRequest();
		invokeRequest.setFunctionName(System.getenv("MSG_FUNCTION"));
		invokeRequest.setPayload(msgQueueJson);
		invokeRequest.setInvocationType(InvocationType.Event);
		return invokeRequest;
	}

	private MessageQueue createMessageQueueResponse(FrontMRequest frontMRequest, final String webServiceResponse) {
		final MessageQueue messageQueue = new MessageQueue();
		messageQueue.setContentType(CONTENT_TYPE_150);
		messageQueue.setContent(webServiceResponse);
		messageQueue.setCreatedOn(System.currentTimeMillis());

		final Parameters parameters = frontMRequest.getParameters();
		if(parameters != null) {
			messageQueue.setCreatedBy(parameters.getUserUuid());
			messageQueue.setUserUuid(parameters.getUserUuid());
			messageQueue.setConversation(parameters.getConversationId());
			messageQueue.setPush(parameters.getPush());
		}
		return messageQueue;
	}
}