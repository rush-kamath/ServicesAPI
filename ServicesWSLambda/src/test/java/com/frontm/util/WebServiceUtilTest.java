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
import java.util.Arrays;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.frontm.domain.APIParameters;
import com.frontm.domain.FrontMRequest;
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
	private final String qryStr1 = "key1=value1";
	private final String qryStr2 = "key2=value2";

	@Before
	public void before() {
		mockStatic(ClientBuilder.class);
		mockStatic(ConvertXMLToJson.class);
		when(ClientBuilder.newClient()).thenReturn(client);
	}


	@Test
	public void testWebServiceUrlCreation() throws Exception {
		WebServiceUtil.addParamsToWebTarget(null, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(0)).queryParam(anyString(), anyObject());
		reset(webTarget);
		
		FrontMRequest.Parameters parameters = new FrontMRequest.Parameters();
		WebServiceUtil.addParamsToWebTarget(null, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(0)).queryParam(anyString(), anyObject());
		resetMocks();
		
		parameters.setObject(object);
		WebServiceUtil.addParamsToWebTarget(parameters, webTarget);
		verify(webTarget, times(1)).path(object);
		verify(webTarget, times(0)).queryParam(anyString(), anyObject());
		resetMocks();
		
		parameters.setObject(null);
		String[] strList = {null, null};
		parameters.setQueryString(Arrays.asList(strList));
		WebServiceUtil.addParamsToWebTarget(parameters, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(0)).queryParam(anyString(), anyObject());
		resetMocks();
		
		strList[0] = "invalidQryStr";
		parameters.setQueryString(Arrays.asList(strList));
		WebServiceUtil.addParamsToWebTarget(parameters, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(0)).queryParam(anyString(), anyObject());
		resetMocks();
		
		strList[0] = qryStr1;
		parameters.setQueryString(Arrays.asList(strList));
		WebServiceUtil.addParamsToWebTarget(parameters, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(1)).queryParam("key1", "value1");
		resetMocks();
		
		when(webTarget.queryParam("key1", "value1")).thenReturn(webTarget);
		strList[1] = qryStr2;
		parameters.setQueryString(Arrays.asList(strList));
		WebServiceUtil.addParamsToWebTarget(parameters, webTarget);
		verify(webTarget, times(0)).path(anyObject());
		verify(webTarget, times(1)).queryParam("key1", "value1");
		verify(webTarget, times(1)).queryParam("key2", "value2");
		resetMocks();

		when(webTarget.queryParam(anyString(), anyObject())).thenReturn(webTarget);
		when(webTarget.path(object)).thenReturn(webTarget);
		parameters.setObject(object);
		parameters.setQueryString(Arrays.asList(strList));
		WebServiceUtil.addParamsToWebTarget(parameters, webTarget);
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
	public void testGetWebServiceResponse() {
		final APIParameters apiParameters = new APIParameters(domain, service);
		apiParameters.setMethod("GET");
		apiParameters.setFormat("JSON");
		apiParameters.setUrl(url);

		final FrontMRequest input = new FrontMRequest();
		input.setDomain(domain);
		input.setService(service);

		try {
			WebServiceUtil.getWebserviceResponse(input, apiParameters, invocationBuilder);
			verify(invocationBuilder, times(1)).get();
			resetMocks();
			
			apiParameters.setMethod("POST");
			WebServiceUtil.getWebserviceResponse(input, apiParameters, invocationBuilder);
			verify(invocationBuilder, times(1)).post(anyObject());
			resetMocks();
		} catch (FrontMException e) {
			fail("No exception should occur");
		}
		
		final String errorMsg = "Expected exception";
		when(invocationBuilder.get()).thenThrow(new ProcessingException(errorMsg));
		try {
			apiParameters.setMethod("GET");
			WebServiceUtil.getWebserviceResponse(input, apiParameters, invocationBuilder);
			fail("Exception should occur");
		} catch (FrontMException e) {
			assertEquals(errorMsg, e.getMessage());
		}
	}
	
	@Test
	public void testParseWebServiceResponse() throws Exception {
		final APIParameters apiParameters = new APIParameters(domain, service);
		apiParameters.setFormat("JSON");

		String jsonResponse = "{\"k1\"=\"v1\"}";
		when(response.readEntity(String.class)).thenReturn(jsonResponse);
		when(response.getStatus()).thenReturn(200);
		assertEquals(jsonResponse, WebServiceUtil.parseWebServiceResponse(apiParameters, response));

		apiParameters.setFormat("XML");
		apiParameters.setMapping(null);
		String xmlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><geonames><status/></geonames>";
		when(response.readEntity(String.class)).thenReturn(xmlResponse);
		when(ConvertXMLToJson.convert(xmlResponse, null)).thenReturn(convertedResponse);
		when(convertedResponse.toString()).thenReturn(jsonResponse);
		assertEquals(jsonResponse, WebServiceUtil.parseWebServiceResponse(apiParameters, response));
	}
	
	@Test(expected = FrontMException.class)
	public void testParseWebServiceError() throws Exception {
		when(response.getStatus()).thenReturn(401);
		when(response.getStatusInfo()).thenReturn(Response.Status.UNAUTHORIZED);
		WebServiceUtil.parseWebServiceResponse(new APIParameters(domain, service), response);
	}

	private void resetMocks() {
		reset(invocationBuilder);
		reset(webTarget);
	}
}
