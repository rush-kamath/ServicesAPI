package com.frontm.function;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.frontm.db.APIParamsDAO;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.db.APIParameters;
import com.frontm.domain.db.Conversation;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AWSLambdaAsyncClientBuilder.class, AmazonDynamoDBClientBuilder.class })
public class ServicesCacheHandlerTest {
	@Mock
	APIParamsDAO apiParamsDAO;
	@Mock
	APIParameters apiParams;
	@Mock
	AWSLambdaAsync lambdaAsync;
	@Mock
	InvokeResult invokeResult;
	@Mock
	AmazonDynamoDBClientBuilder dbClientBuilder;
	@Mock
	AmazonDynamoDB dynamoDB;

	private void callHandlerWithMocks(FrontMRequest input) {
		ServicesCacheHandler handler = new ServicesCacheHandler();
		handler.setApiParamsDao(apiParamsDAO);
		handler.handleRequest(input, new TestContext());
	}

	@Before
	public void before() throws Exception {
		mockStatic(AWSLambdaAsyncClientBuilder.class);
		mockStatic(AmazonDynamoDBClientBuilder.class);

		when(apiParamsDAO.getApiParamsFromDB(any(FrontMRequest.class))).thenReturn(apiParams);
		when(AWSLambdaAsyncClientBuilder.defaultClient()).thenReturn(lambdaAsync);
		when(AmazonDynamoDBClientBuilder.standard()).thenReturn(dbClientBuilder);
		when(dbClientBuilder.build()).thenReturn(dynamoDB);
		when(lambdaAsync.invoke(any(InvokeRequest.class))).thenReturn(invokeResult);
		when(invokeResult.getStatusCode()).thenReturn(202);
	}

	@Test
	public void testValidations() {
		FrontMRequest request = new FrontMRequest();
		request.setConversation(new Conversation());
		
		when(apiParams.getTableName()).thenReturn(null);
		callHandlerWithMocks(request);
		verify(dynamoDB, times(0)).query(any());
		
		when(apiParams.getTableName()).thenReturn("");
		callHandlerWithMocks(request);
		verify(dynamoDB, times(0)).query(any());
	}
	
	@Test
	public void testCreateQueryRequest() {
		final FrontMRequest request = new FrontMRequest();
		final ServicesCacheHandler servicesCacheHandler = new ServicesCacheHandler();

		QueryRequest qryRequest = servicesCacheHandler.createQueryRequest("cacheTableName", request);
		assertEquals(null, qryRequest.getFilterExpression());
		assertEquals(null, qryRequest.getExpressionAttributeNames());
		assertEquals(1, qryRequest.getExpressionAttributeValues().size());
		
		request.setQueryString("k1==v1");
		qryRequest = servicesCacheHandler.createQueryRequest("cacheTableName", request);
		assertEquals("#k1=:val1", qryRequest.getFilterExpression());
		assertEquals("k1", qryRequest.getExpressionAttributeNames().get("#k1"));
		assertEquals(1, qryRequest.getExpressionAttributeNames().size());
		assertEquals(2, qryRequest.getExpressionAttributeValues().size());
		assertEquals("v1", qryRequest.getExpressionAttributeValues().get(":val1").getS());
		
		request.setQueryString("k1==v1 && k2==v2");
		qryRequest = servicesCacheHandler.createQueryRequest("cacheTableName", request);
		assertEquals("#k1=:val1 and #k2=:val2", qryRequest.getFilterExpression());
		assertEquals("k1", qryRequest.getExpressionAttributeNames().get("#k1"));
		assertEquals("k2", qryRequest.getExpressionAttributeNames().get("#k2"));
		assertEquals(3, qryRequest.getExpressionAttributeValues().size());
		assertEquals("v1", qryRequest.getExpressionAttributeValues().get(":val1").getS());
		assertEquals("v2", qryRequest.getExpressionAttributeValues().get(":val2").getS());
	}
	
	@Test
	public void testCreateJsonArray() throws JsonProcessingException {
		final List<Map<String, AttributeValue>> items = new ArrayList<>();
		final ServicesCacheHandler servicesCacheHandler = new ServicesCacheHandler();
		assertEquals("[]", servicesCacheHandler.createJsonArray(items));
		
		Map<String, AttributeValue> item = new HashMap<>();
		item.put("k1", new AttributeValue().withS("v1"));
		item.put("k2", new AttributeValue().withS("v2"));
		items.add(item);
		assertEquals("[{\"k1\":\"v1\",\"k2\":\"v2\"}]", servicesCacheHandler.createJsonArray(items));
		
		items.add(item);
		assertEquals("[{\"k1\":\"v1\",\"k2\":\"v2\"},{\"k1\":\"v1\",\"k2\":\"v2\"}]", servicesCacheHandler.createJsonArray(items));
	}
}
