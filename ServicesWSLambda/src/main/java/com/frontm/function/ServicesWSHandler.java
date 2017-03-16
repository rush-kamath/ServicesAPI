package com.frontm.function;

import static com.frontm.util.WebServiceUtil.createWebserviceCall;
import static com.frontm.util.WebServiceUtil.getWebserviceResponse;
import static com.frontm.util.WebServiceUtil.parseWebServiceResponse;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.AWSLambdaAsync;
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
import com.frontm.domain.MessageQueue.Item;
import com.frontm.domain.MessageQueue.Item.Content;
import com.frontm.domain.ServicesWSInput;
import com.frontm.exception.FrontMException;
import com.frontm.util.JsonFilterUtil;

public class ServicesWSHandler implements RequestHandler<ServicesWSInput, Void> {
	private static final Logger logger = Logger.getLogger(ServicesWSHandler.class);
	private static final String MSG_Q_CONTENT_TYPE = "150";
	private static final String MSG_Q_RET_VAL = "NONE";
	private static final String MSG_Q_CREATED_BY = "AgentM";

	@Override
	public Void handleRequest(ServicesWSInput input, Context context) {
		logger.debug("Input parameters in the request: " + input);
		try {
			final APIParameters apiParams = input.getApiParameters();
			final FrontMRequest request = input.getRequest();

			Invocation.Builder invocationBuilder = createWebserviceCall(request, apiParams);
			final InvokeRequest invokeRequest = callWSFilterAndCreateLambdaRequest(input, apiParams, invocationBuilder);
			invokeLambda(input, invokeRequest);
		} catch (Exception e) {
			logger.error("Error occured:", e);
		}
		return null;
	}

	private void invokeLambda(ServicesWSInput input, final InvokeRequest invokeRequest) throws Exception {
		final AWSLambdaAsync client = AWSLambdaAsyncClientBuilder.defaultClient();
		InvokeResult invokeResult = null; 
		try {
			invokeResult = client.invoke(invokeRequest);
		} catch (Exception e) {
			final InvokeRequest errorInput = logAndCreateErrorLambdaInput(input, e, "Error while invoking mesage queue lambda");
			invokeResult = client.invoke(errorInput);
		}
		logger.info("After calling message queue lambda: " + invokeResult.getStatusCode());
	}

	private InvokeRequest callWSFilterAndCreateLambdaRequest(ServicesWSInput input, APIParameters apiParams,
			Builder invocationBuilder) throws Exception {
		try {
			final FrontMRequest request = input.getRequest();
			Response response = getWebserviceResponse(request, apiParams, invocationBuilder);
			final String webServiceJson = parseWebServiceResponse(apiParams, response);
			final String filteredJson = filterJson(request, webServiceJson);
			return createLambdaInput(input, filteredJson, true);
		} catch (FrontMException e) {
			return logAndCreateErrorLambdaInput(input, e, "Error while creating mesage queue lambda request");
		}
	}

	private InvokeRequest logAndCreateErrorLambdaInput(ServicesWSInput input, Exception e, String logMsg) throws JsonProcessingException {
		final String errorMessage = e.getMessage();
		logger.info(logMsg + ": " + errorMessage);
		return createLambdaInput(input, errorMessage, false);
	}

	private String filterJson(FrontMRequest request, final String webServiceJson) throws FrontMException {
		final String jsonFilter = request.getFilter();
		if (jsonFilter == null || jsonFilter.isEmpty()) {
			return webServiceJson;
		}
		return JsonFilterUtil.filterJson(webServiceJson, jsonFilter);
	}

	private InvokeRequest createLambdaInput(ServicesWSInput input, final String webServiceResponse,
			boolean isOKResponse) throws JsonProcessingException {
		MessageQueue messageQueue = createMessageQueueResponse(input.getRequest(), webServiceResponse, isOKResponse);
		final String msgQueueJson = new ObjectMapper().writeValueAsString(messageQueue);
		logger.info(msgQueueJson);

		InvokeRequest invokeRequest = new InvokeRequest();
		invokeRequest.setFunctionName(System.getenv("MSG_FUNCTION"));
		invokeRequest.setPayload(msgQueueJson);
		invokeRequest.setInvocationType(InvocationType.Event);
		return invokeRequest;
	}

	private MessageQueue createMessageQueueResponse(FrontMRequest frontMRequest, final String webServiceResponse,
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

		final Parameters parameters = frontMRequest.getParameters();
		if (parameters != null) {
			item.setUserUuid(parameters.getUserUuid());
			item.setConversation(parameters.getConversationId());
			item.setPush(parameters.getPush());
		}

		final MessageQueue messageQueue = new MessageQueue();
		messageQueue.setTableName(System.getenv("MSG_Q_TABLE_NAME"));
		messageQueue.setReturnValues(MSG_Q_RET_VAL);
		messageQueue.setItem(item);
		return messageQueue;
	}
}