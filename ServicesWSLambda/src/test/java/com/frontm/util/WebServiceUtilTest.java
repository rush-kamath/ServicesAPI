package com.frontm.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.net.URI;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.db.APIParameters;
import com.frontm.exception.FrontMException;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ClientBuilder.class, ConvertXMLToJson.class})
public class WebServiceUtilTest {

	@Mock Client client;
	@Mock WebTarget webTarget;
	@Mock Invocation.Builder invocationBuilder;
	@Mock URI uri;
	@Mock Response response;
	@Mock ObjectNode convertedResponse;

	final String domain = "frontm.com";
	final String service = "TestGet";
	private final String url = "ServiceUrl";
	private final String object = "object";
	private final String qryStr1 = "key1==value1";
	private final String qryStr2 = "key2==value2";

	@Before
	public void before() {
		mockStatic(ClientBuilder.class);
		mockStatic(ConvertXMLToJson.class);
		when(ClientBuilder.newClient()).thenReturn(client);
	}


	@Test
	public void testWebServiceUrlCreation() throws Exception {
		FrontMRequest request = new FrontMRequest();
		WebServiceUtil.addParamsToWebTarget(request, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(0)).queryParam(anyString(), anyObject());
		resetMocks();
		
		request.setObject(object);
		WebServiceUtil.addParamsToWebTarget(request, webTarget);
		verify(webTarget, times(1)).path(object);
		verify(webTarget, times(0)).queryParam(anyString(), anyObject());
		resetMocks();
		
		request.setObject(null);
		request.setQueryString(null);
		WebServiceUtil.addParamsToWebTarget(request, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(0)).queryParam(anyString(), anyObject());
		resetMocks();
		
		String queryString = "invalidQryStr";
		request.setQueryString(queryString);
		WebServiceUtil.addParamsToWebTarget(request, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(0)).queryParam(anyString(), anyObject());
		resetMocks();
		
		queryString = "a=b";
		request.setQueryString(queryString);
		WebServiceUtil.addParamsToWebTarget(request, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(0)).queryParam(anyString(), anyObject());
		resetMocks();
		
		queryString = qryStr1;
		request.setQueryString(queryString);
		WebServiceUtil.addParamsToWebTarget(request, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(1)).queryParam("key1", "value1");
		resetMocks();
		
		queryString = qryStr1+"&";
		request.setQueryString(queryString);
		WebServiceUtil.addParamsToWebTarget(request, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(1)).queryParam("key1", "value1&");
		resetMocks();
		
		queryString = qryStr1+"&"+qryStr2;
		request.setQueryString(queryString);
		WebServiceUtil.addParamsToWebTarget(request, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(1)).queryParam("key1", "value1&key2");
		resetMocks();
		
		when(webTarget.queryParam("key1", "value1")).thenReturn(webTarget);
		queryString = qryStr1 + "&&" + qryStr2;
		request.setQueryString(queryString);
		WebServiceUtil.addParamsToWebTarget(request, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(1)).queryParam("key1", "value1");
		verify(webTarget, times(1)).queryParam("key2", "value2");
		resetMocks();

		when(webTarget.queryParam(anyString(), anyObject())).thenReturn(webTarget);
		when(webTarget.path(object)).thenReturn(webTarget);
		request.setObject(object);
		request.setQueryString(queryString);
		WebServiceUtil.addParamsToWebTarget(request, webTarget);
		verify(webTarget, times(1)).path(object);
		verify(webTarget, times(2)).queryParam(anyString(), anyObject());
	}

	
	@Test
	public void testWebserviceCallAuthentication() {
		final APIParameters apiParameters = new APIParameters(domain, service);
		apiParameters.setMethod("GET");
		apiParameters.setFormat("JSON");
		apiParameters.setUrl(url);

		final FrontMRequest input = new FrontMRequest();
		input.setDomain(domain);
		input.setService(service);
		
		when(client.target(url)).thenReturn(webTarget);
		when(webTarget.request(MediaType.APPLICATION_JSON)).thenReturn(invocationBuilder);
		when(webTarget.getUri()).thenReturn(uri);
		when(uri.toString()).thenReturn(url);

		WebServiceUtil.createWebserviceCall(input, apiParameters);
		verify(webTarget, times(1)).request(MediaType.APPLICATION_JSON);
		verify(invocationBuilder, times(0)).property(anyString(), anyString());
		
		final String userName = "userName";
		input.setUsername(userName);
		WebServiceUtil.createWebserviceCall(input, apiParameters);
		verify(client, times(0)).register(any(HttpAuthenticationFeature.class));
		
		final String password = "password";
		input.setPassword(password);
		WebServiceUtil.createWebserviceCall(input, apiParameters);
		verify(client, times(1)).register(any(HttpAuthenticationFeature.class));
		
		apiParameters.setFormat("XML");
		when(webTarget.request(MediaType.APPLICATION_XML)).thenReturn(invocationBuilder);
		
		WebServiceUtil.createWebserviceCall(input, apiParameters);
		verify(webTarget, times(1)).request(MediaType.APPLICATION_XML);
	}
	
	@Test
	public void testCallWebservice() {
		final APIParameters apiParameters = new APIParameters(domain, service);
		apiParameters.setMethod("GET");
		apiParameters.setFormat("JSON");
		apiParameters.setUrl(url);

		final FrontMRequest input = new FrontMRequest();
		input.setDomain(domain);
		input.setService(service);

		try {
			WebServiceUtil.callWebservice(input, apiParameters, invocationBuilder);
			verify(invocationBuilder, times(1)).get();
			resetMocks();
			
			apiParameters.setMethod("POST");
			WebServiceUtil.callWebservice(input, apiParameters, invocationBuilder);
			verify(invocationBuilder, times(1)).post(anyObject());
			resetMocks();
		} catch (FrontMException e) {
			fail("No exception should occur");
		}
		
		final String errorMsg = "Expected exception";
		when(invocationBuilder.get()).thenThrow(new ProcessingException(errorMsg));
		try {
			apiParameters.setMethod("GET");
			WebServiceUtil.callWebservice(input, apiParameters, invocationBuilder);
			fail("Exception should occur");
		} catch (FrontMException e) {
			assertEquals(errorMsg, e.getMessage());
		}
	}
	
	@Test
	public void testGetWebServiceResponse() throws Exception {
		final APIParameters apiParameters = new APIParameters(domain, service);
		apiParameters.setFormat("JSON");

		String wsResponse = "{\"k1\"=\"v1\"}";
		when(response.readEntity(String.class)).thenReturn(wsResponse);
		when(response.getStatus()).thenReturn(200);
		assertEquals(wsResponse, WebServiceUtil.getWebServiceResponse(apiParameters, response));

		try {
			when(response.getStatus()).thenReturn(500);
			when(response.getStatusInfo()).thenReturn(Status.INTERNAL_SERVER_ERROR);
			WebServiceUtil.getWebServiceResponse(apiParameters, response);
			fail("An exception should be thrown.");
		} catch(FrontMException e) {
			
		}
	}
	
	@Test
	public void testParseWebServiceResponse() throws Exception {
		final APIParameters apiParameters = new APIParameters(domain, service);
		apiParameters.setFormat("JSON");

		String jsonResponse = "{\"k1\"=\"v1\"}";
		assertEquals(jsonResponse, WebServiceUtil.parseWebServiceResponse(apiParameters, jsonResponse));

		apiParameters.setFormat("XML");

		String xmlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><geonames><status/></geonames>";
		when(ConvertXMLToJson.convert(xmlResponse, null)).thenReturn(convertedResponse);
		when(convertedResponse.toString()).thenReturn(jsonResponse);
		assertEquals(jsonResponse, WebServiceUtil.parseWebServiceResponse(apiParameters, xmlResponse));
	}
	
	@Test(expected = FrontMException.class)
	public void testParseWebServiceError() throws Exception {
		when(response.getStatus()).thenReturn(401);
		when(response.getStatusInfo()).thenReturn(Response.Status.UNAUTHORIZED);
		WebServiceUtil.getWebServiceResponse(new APIParameters(domain, service), response);
	}

	private void resetMocks() {
		reset(invocationBuilder);
		reset(webTarget);
	}
}
