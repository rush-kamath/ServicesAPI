package com.frontm.db;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.frontm.domain.APIParameters;
import com.frontm.domain.FrontMRequest;
import com.frontm.exception.FrontMException;

public class APIParamsDAO {
	private static final String UNAVAILABLE_SERVICE_MESSAGE = "Service %s in Domain %s is not found";

	private DynamoDBMapper mapper;

	public APIParameters getApiParamsFromDB(FrontMRequest input) throws FrontMException {
		if(this.mapper == null) {
			initializeDynamoDB();
		}

		Map<String, AttributeValue> method2QueryValues = new HashMap<String, AttributeValue>();
		method2QueryValues.put(":val1", new AttributeValue().withS(input.getDomain()));
		method2QueryValues.put(":val2", new AttributeValue().withS(input.getService()));
		DynamoDBQueryExpression<APIParameters> queryExpression = new DynamoDBQueryExpression<APIParameters>()
				.withKeyConditionExpression("service = :val2 and #domain = :val1")
				.addExpressionAttributeNamesEntry("#domain", "domain")
				.withExpressionAttributeValues(method2QueryValues);

		PaginatedQueryList<APIParameters> results = mapper.query(APIParameters.class, queryExpression);
		if (results.isEmpty()) {
			throw new FrontMException(String.format(UNAVAILABLE_SERVICE_MESSAGE, input.getService(), input.getDomain()));
		}

		return results.get(0);
	}

	private void initializeDynamoDB() {
		AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
		this.mapper = new DynamoDBMapper(dynamoDB);
	}
	
	// for testing only
	public void setDynamoDBMapper(DynamoDBMapper mapper) {
		this.mapper = mapper;
	}
}
