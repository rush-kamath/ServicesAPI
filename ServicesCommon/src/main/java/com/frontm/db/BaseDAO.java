package com.frontm.db;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class BaseDAO {
	private DynamoDBMapper mapper;
	private void initializeDynamoDB() {
		AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
		this.mapper = new DynamoDBMapper(dynamoDB);
	}
	
	protected DynamoDBMapper getMapper() {
		if(this.mapper == null) {
			initializeDynamoDB();
		}
		return mapper;
	}
	
	// for testing only
	void setDynamoDBMapper(DynamoDBMapper mapper) {
		this.mapper = mapper;
	}

}