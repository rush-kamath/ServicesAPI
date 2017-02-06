package com.frontm.function;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.frontm.db.APIParamsDAO;
import com.frontm.domain.FrontMRequest;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(JUnit4.class)
@Ignore
public class ServicesHandlerIntegrationTest {
	final String domain = "frontm.com";
	final String service = "TestGetXML";
	
	private static AmazonDynamoDB actualDynamoDB;

	@BeforeClass
	public static void initialize() {
		actualDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.withCredentials(new ProfileCredentialsProvider()).build();
	}

	@Test
	public void testCall() throws Exception {
		final FrontMRequest input = new FrontMRequest();
		input.setDomain(domain);
		input.setService(service);
		FrontMRequest.Parameters parameters = new FrontMRequest.Parameters();
		input.setParameters(parameters);
		parameters.setQueryString(Arrays.asList("k=v", "k1=v1,v2"));
		Map<String, Object> body = new HashMap<>();
		body.put("bodyK1", "bodyV1");
		body.put("bodyK2", "bodyV2");
		body.put("t1", new HashMap<String, String>());
		parameters.setBody(body);
		
		ServicesHandler handler = new ServicesHandler();
		APIParamsDAO apiParamsDAO = new APIParamsDAO();
		apiParamsDAO.setDynamoDBMapper(new DynamoDBMapper(actualDynamoDB));
		handler.setApiParamsDao(apiParamsDAO);
		final TestContext testContext = new TestContext();
		testContext.setIdentity(new CognitoIdentity() {
			@Override
			public String getIdentityPoolId() {
				return "poolid";
			}
			
			@Override
			public String getIdentityId() {
				return "id";
			}
		});
		testContext.getLogger().log(handler.handleRequest(input, testContext));
	}

}
