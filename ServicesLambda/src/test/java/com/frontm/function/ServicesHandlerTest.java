package com.frontm.function;

import static com.frontm.function.ServicesHandler.INVALID_FORMAT_IN_DB;
import static com.frontm.function.ServicesHandler.MISSING_USER_UUID_MESSAGE;
import static com.frontm.function.ServicesHandler.MISSING_SERVICE_DOMAIN_MESSAGE;
import static com.frontm.function.ServicesHandler.INVALID_METHOD_IN_DB;
import static com.frontm.function.ServicesHandler.MISSING_MAPPING_FOR_XML_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.frontm.db.APIParamsDAO;
import com.frontm.domain.APIParameters;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.FrontMRequest.Parameters;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(MockitoJUnitRunner.class)
public class ServicesHandlerTest {
	@Mock APIParamsDAO apiParamsDao;

	final String domain = "frontm.com";
	final String service = "TestGet";
	final String userUuid = "userUuid";


	private String callHandlerWithMocks(FrontMRequest input, APIParameters apiParameters) {
		try {
			when(apiParamsDao.getApiParamsFromDB(input)).thenReturn(apiParameters);
		} catch (Exception e) {
			fail("This mock expectation should not fail");
		}

		ServicesHandler handler = new ServicesHandler();
		handler.setApiParamsDao(apiParamsDao);

		return handler.handleRequest(input, new TestContext());
	}

	@Test
	public void testInvalidRequest() {
		final FrontMRequest input = new FrontMRequest();
		assertEquals(MISSING_SERVICE_DOMAIN_MESSAGE, callHandlerWithMocks(input, null));

		input.setDomain("");
		assertEquals(MISSING_SERVICE_DOMAIN_MESSAGE, callHandlerWithMocks(input, null));

		input.setDomain("domain");
		input.setService("  ");
		assertEquals(MISSING_SERVICE_DOMAIN_MESSAGE, callHandlerWithMocks(input, null));
		
		input.setService(service);
		assertEquals(MISSING_USER_UUID_MESSAGE, callHandlerWithMocks(input, null));
		
		input.setParameters(new Parameters());
		assertEquals(MISSING_USER_UUID_MESSAGE, callHandlerWithMocks(input, null));
	}

	@Test
	public void testUnsupportedFormatAndMethodInDB() {
		final FrontMRequest input = new FrontMRequest();
		input.setDomain(domain);
		input.setService(service);
		final Parameters parameters = new Parameters();
		parameters.setUserUuid(userUuid);
		input.setParameters(parameters);

		final APIParameters apiParameters = new APIParameters(domain, service);
		assertEquals(INVALID_FORMAT_IN_DB, callHandlerWithMocks(input, apiParameters));

		apiParameters.setMethod("GET");
		apiParameters.setFormat("UNKNOWN");
		assertEquals(INVALID_FORMAT_IN_DB, callHandlerWithMocks(input, apiParameters));

		apiParameters.setFormat("JSON");
		apiParameters.setMethod("");
		assertEquals(INVALID_METHOD_IN_DB, callHandlerWithMocks(input, apiParameters));

		apiParameters.setMethod("PUT");
		apiParameters.setFormat("JSON");
		assertEquals(INVALID_METHOD_IN_DB, callHandlerWithMocks(input, apiParameters));
		
		apiParameters.setMethod("POST");
		apiParameters.setFormat("XML");
		assertEquals(MISSING_MAPPING_FOR_XML_FORMAT, callHandlerWithMocks(input, apiParameters));
	}
}
