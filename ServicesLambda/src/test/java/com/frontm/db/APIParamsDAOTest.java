package com.frontm.db;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.frontm.domain.APIParameters;
import com.frontm.domain.FrontMRequest;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class APIParamsDAOTest {
	@Mock
	DynamoDBMapper mockMapper;
	@Mock
	PaginatedQueryList<APIParameters> results;

	final String domain = "frontm.com";
	final String service = "TestGet";

	private static AmazonDynamoDB actualDynamoDB;

	@BeforeClass
	public static void initialize() {
		actualDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.withCredentials(new ProfileCredentialsProvider()).build();
	}

	private APIParameters callDaoWithMocks(FrontMRequest input) throws Exception {
		APIParamsDAO dao = new APIParamsDAO();
		dao.setDynamoDBMapper(mockMapper);

		return dao.getApiParamsFromDB(input);
	}

	@Test
	public void testUnavailableService() {
		when(mockMapper.query(eq(APIParameters.class), any(DynamoDBQueryExpression.class))).thenReturn(results);
		when(results.isEmpty()).thenReturn(true);

		final FrontMRequest input = new FrontMRequest();
		input.setDomain(domain);
		input.setService(service);

		try {
			callDaoWithMocks(input);
		} catch (Exception e) {
			assertEquals("Service " + service + " in Domain " + domain + " is not found", e.getMessage());
		}
	}

	@Test
	public void testNoFormatInDB() throws Exception {
		APIParameters apiParameters = new APIParameters(domain, service);

		when(mockMapper.query(eq(APIParameters.class), any(DynamoDBQueryExpression.class))).thenReturn(results);
		when(results.isEmpty()).thenReturn(false);
		when(results.get(0)).thenReturn(apiParameters);

		final FrontMRequest input = new FrontMRequest();
		input.setDomain(domain);
		input.setService(service);

		assertEquals(apiParameters, callDaoWithMocks(input));
	}

	private APIParameters callDaoWithRealDB(FrontMRequest input) throws Exception {
		APIParamsDAO dao = new APIParamsDAO();
		dao.setDynamoDBMapper(new DynamoDBMapper(actualDynamoDB));

		return dao.getApiParamsFromDB(input);
	}

	@Test
	public void testUnavailableServiceRealDB() {
		final FrontMRequest input = new FrontMRequest();
		input.setDomain(service);
		input.setService(domain);

		try {
			callDaoWithRealDB(input);
		} catch (Exception e) {
			assertEquals("Service " + domain + " in Domain " + service + " is not found", e.getMessage());
		}
	}

	@Test
	public void testAvailableService() throws Exception {
		final String domain = "frontm.com";
		String service = "TestGet";

		final FrontMRequest input = new FrontMRequest();
		input.setDomain(domain);
		input.setService(service);

		APIParameters apiParams = callDaoWithRealDB(input);
		assertEquals(domain, apiParams.getDomain());
		assertEquals(service, apiParams.getService());

		service = "TestGetXML";
		input.setService(service);

		apiParams = callDaoWithRealDB(input);
		assertEquals(domain, apiParams.getDomain());
		assertEquals(service, apiParams.getService());

		service = "GXGetResellerSites";
		input.setService(service);

		apiParams = callDaoWithRealDB(input);
		assertEquals(domain, apiParams.getDomain());
		assertEquals(service, apiParams.getService());
	}
}
