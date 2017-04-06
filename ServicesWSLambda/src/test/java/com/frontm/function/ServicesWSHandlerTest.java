package com.frontm.function;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.frontm.db.APIParamsDAO;
import com.frontm.db.CacheTableDAO;
import com.frontm.db.ConversationDAO;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.db.APIParameters;
import com.frontm.domain.db.Conversation;
import com.frontm.exception.FrontMException;
import com.frontm.util.JaxbParserUtil;
import com.frontm.util.JsonFilterUtil;
import com.frontm.util.WebServiceUtil;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ WebServiceUtil.class, AWSLambdaAsyncClientBuilder.class, JaxbParserUtil.class,
		CacheTableDAO.class, JsonFilterUtil.class })
public class ServicesWSHandlerTest {
	@Mock
	APIParamsDAO apiParamsDAO;
	@Mock
	APIParameters apiParams;
	@Mock
	AWSLambdaAsync lambdaAsync;
	@Mock
	InvokeResult invokeResult;
	@Mock
	Builder invocationBuilder;
	@Mock
	Response response;
	@Mock
	ConversationDAO conversationDAO;

	String webServiceResponse = "webServiceResponse";

	private void callHandlerWithMocks(FrontMRequest input) {
		ServicesWSHandler handler = new ServicesWSHandler();
		handler.setApiParamsDao(apiParamsDAO);
		handler.setConversationDao(conversationDAO);

		handler.handleRequest(input, new TestContext());
	}

	@Before
	public void before() throws Exception {
		mockStatic(WebServiceUtil.class);
		mockStatic(AWSLambdaAsyncClientBuilder.class);
		mockStatic(JaxbParserUtil.class);
		mockStatic(CacheTableDAO.class);
		mockStatic(JsonFilterUtil.class);

		when(apiParamsDAO.getApiParamsFromDB(any(FrontMRequest.class))).thenReturn(apiParams);
		when(WebServiceUtil.createWebserviceCall(any(FrontMRequest.class), any(APIParameters.class)))
				.thenReturn(invocationBuilder);
		when(WebServiceUtil.callWebservice(any(FrontMRequest.class), any(APIParameters.class), any(Builder.class)))
				.thenReturn(response);
		when(WebServiceUtil.getWebServiceResponse(apiParams, response)).thenReturn(webServiceResponse);
		when(AWSLambdaAsyncClientBuilder.defaultClient()).thenReturn(lambdaAsync);
		when(lambdaAsync.invoke(any(InvokeRequest.class))).thenReturn(invokeResult);
		when(invokeResult.getStatusCode()).thenReturn(202);
	}

	@Test
	public void testBuildCacheCommand() throws FrontMException {
		final FrontMRequest request = new FrontMRequest();
		request.setCommandName(ServicesWSHandler.BUILD_CACHE_COMMAND);
		
		final Conversation conversation = new Conversation();
		request.setConversation(conversation);
		
		final List<Map<String, String>> xmlContents = new ArrayList<>();

		when(apiParams.isXMLFormat()).thenReturn(true);
		when(apiParams.isPostMethod()).thenReturn(true);
		when(apiParams.getTableName()).thenReturn("tableName");
		when(apiParams.getClassName()).thenReturn("className");
		when(JaxbParserUtil.parseXMLToDBCacheItems(any(), any(), any())).thenReturn(xmlContents);

		callHandlerWithMocks(request);
		verify(conversationDAO, times(0)).saveConversation(conversation);
		verifyStatic(times(1));
		JaxbParserUtil.parseXMLToDBCacheItems(any(), any(), any());
		verifyStatic(times(1));
		CacheTableDAO.insertItemsIntoDB(any(), any());

		conversation.setConversationOwner("conversationOwner");
		callHandlerWithMocks(request);
		verify(conversationDAO, times(0)).saveConversation(conversation);

		reset(conversationDAO);
		conversation.setClosed(true);
		callHandlerWithMocks(request);
		verify(conversationDAO, times(1)).saveConversation(conversation);
		
		reset(conversationDAO);
		conversation.setClosed(null);
		conversation.setBot("bot");
		callHandlerWithMocks(request);
		verify(conversationDAO, times(1)).saveConversation(conversation);

		reset(conversationDAO);
		conversation.setBot(null);
		final ArrayList<String> channels = new ArrayList<>();
		conversation.setOnChannels(channels);
		callHandlerWithMocks(request);
		verify(conversationDAO, times(0)).saveConversation(conversation);
		
		reset(conversationDAO);
		channels.add("channel");
		conversation.setOnChannels(channels);
		conversation.setParticipants(channels);
		callHandlerWithMocks(request);
		verify(conversationDAO, times(1)).saveConversation(conversation);
	}

	@Test
	public void testDataFromServiceCommand() throws Exception {
		FrontMRequest request = new FrontMRequest();
		request.setCommandName(ServicesWSHandler.GET_DATA_FROM_SERVICE_COMMAND);
		request.setConversation(new Conversation());

		when(apiParams.isJsonFormat()).thenReturn(true);
		when(apiParams.isPostMethod()).thenReturn(true);
		when(WebServiceUtil.parseWebServiceResponse(any(), any())).thenReturn(webServiceResponse);
		when(JsonFilterUtil.filterJson(any(), any())).thenReturn(webServiceResponse);

		callHandlerWithMocks(request);
		verifyStatic(times(1));
		WebServiceUtil.parseWebServiceResponse(any(), any());

		verifyStatic(times(1));
		JsonFilterUtil.filterJson(any(), any());
	}
	
	@Test
	public void testValidations() {
		FrontMRequest request = new FrontMRequest();
		request.setConversation(new Conversation());
		
		// not xml or json format
		when(apiParams.isXMLFormat()).thenReturn(false);
		when(apiParams.isJsonFormat()).thenReturn(false);
		callHandlerWithMocks(request);
		verifyStatic(times(0));
		WebServiceUtil.createWebserviceCall(any(), any());
		
		// not get or post method
		when(apiParams.isJsonFormat()).thenReturn(true);
		when(apiParams.isGetMethod()).thenReturn(false);
		when(apiParams.isPostMethod()).thenReturn(false);
		callHandlerWithMocks(request);
		verifyStatic(times(0));
		WebServiceUtil.createWebserviceCall(any(), any());
		
		// incorrect command name
		when(apiParams.isGetMethod()).thenReturn(true);
		request.setCommandName("Incorrect command");
		callHandlerWithMocks(request);
		verifyStatic(times(0));
		WebServiceUtil.createWebserviceCall(any(), any());
		
		// build cache and json format
		request.setCommandName(ServicesWSHandler.BUILD_CACHE_COMMAND);
		callHandlerWithMocks(request);
		verifyStatic(times(0));
		WebServiceUtil.createWebserviceCall(any(), any());
		
		// build cache and no table/class name
		when(apiParams.isJsonFormat()).thenReturn(false);
		when(apiParams.isXMLFormat()).thenReturn(true);
		callHandlerWithMocks(request);
		verifyStatic(times(0));
		WebServiceUtil.createWebserviceCall(any(), any());
		
		when(apiParams.getTableName()).thenReturn("tableName");
		callHandlerWithMocks(request);
		verifyStatic(times(0));
		WebServiceUtil.createWebserviceCall(any(), any());
		
		// get data from service, xml format and no mapping
		request.setCommandName(ServicesWSHandler.GET_DATA_FROM_SERVICE_COMMAND);
		when(apiParams.isXMLFormat()).thenReturn(true);
		when(apiParams.getMapping()).thenReturn(null);
		callHandlerWithMocks(request);
		verifyStatic(times(0));
		WebServiceUtil.createWebserviceCall(any(), any());
	}
}
