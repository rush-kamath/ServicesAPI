package com.frontm.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.frontm.util.MessageQueueLambdaUtil.createLambdaInput;
import static com.frontm.util.MessageQueueLambdaUtil.invokeLambda;
import static com.frontm.util.MessageQueueLambdaUtil.logAndCreateErrorLambdaInput;
import static com.frontm.util.StringUtil.isEmpty;
import static com.frontm.util.StringUtil.processQueryString;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.frontm.db.APIParamsDAO;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.db.APIParameters;
import com.frontm.exception.FrontMException;

public class ServicesCacheHandler implements RequestHandler<FrontMRequest, Void> {
	private static final Logger logger = Logger.getLogger(ServicesCacheHandler.class);
	private static final String GET_CACHE_REQUIRED_FILEDS = "Incorrect data in DB table APIParams. Table is a required field for GetFromCache Command";
	private APIParamsDAO apiParamsDao;

	@Override
	public Void handleRequest(FrontMRequest request, Context context) {
		logger.info("Input parameters in the request: " + request);
		InvokeRequest invokeRequest = null;
		try {
			invokeRequest = processInput(request);
			invokeLambda(request, invokeRequest);
		} catch (Exception e) {
			logger.error("Error occured:", e);
		}
		return null;
	}

	private void doValidations(APIParameters apiParams) throws FrontMException {
		if(isEmpty(apiParams.getTableName())) {
			throw new FrontMException(GET_CACHE_REQUIRED_FILEDS);
		}
	}
	
	private InvokeRequest processInput(FrontMRequest request) throws JsonProcessingException {
		InvokeRequest invokeRequest;
		try {
			final APIParameters apiParams = getApiParamsDao().getApiParamsFromDB(request);
			logger.debug("Details from the DB" + apiParams.toString());
			doValidations(apiParams);

			final String resultsJson = getDataFromCacheTable(apiParams.getTableName(), request);
			invokeRequest = createLambdaInput(request, resultsJson, true);
		} catch (FrontMException e) {
			invokeRequest = logAndCreateErrorLambdaInput(request, e, "Error while getting details from DB cache");
		}
		return invokeRequest;
	}

	private String getDataFromCacheTable(String cacheTableName, FrontMRequest request) throws JsonProcessingException {
		final QueryRequest queryRequest = createQueryRequest(cacheTableName, request);
		final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
		final List<Map<String, AttributeValue>> items = dynamoDB.query(queryRequest).getItems();
		logger.info("Number of items fetched: " + items.size());

		return createJsonArray(items);
	}

	QueryRequest createQueryRequest(String cacheTableName, FrontMRequest request) {
		final StringBuilder filterExpression = new StringBuilder();
		final Map<String, String> expressionAttributeNames = new HashMap<>();
		final Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
		expressionAttributeValues.put(":val0", new AttributeValue().withS(request.getInstanceId()));

		final Map<String, String> requestQueryMap = processQueryString(request.getQueryString());
		int fieldNumber = 1;
		for (String key : requestQueryMap.keySet()) {
			final String value = requestQueryMap.get(key);
			final String expAttribName = "#" + key;
			final String expAttribVal = ":val" + fieldNumber;

			expressionAttributeNames.put(expAttribName, key);
			expressionAttributeValues.put(expAttribVal, new AttributeValue().withS(value));
			filterExpression.append((fieldNumber > 1) ? " and " : "").append(expAttribName).append("=")
					.append(expAttribVal);

			logger.info("Added query string: " + key + "=" + value);
			fieldNumber++;
		}

		final QueryRequest queryRequest = new QueryRequest(cacheTableName).withKeyConditionExpression("instanceId = :val0")
				.withExpressionAttributeValues(expressionAttributeValues);
		if(!expressionAttributeNames.isEmpty()) {
			queryRequest.setFilterExpression(filterExpression.toString());
			queryRequest.setExpressionAttributeNames(expressionAttributeNames);
		}
		return queryRequest;
	}

	String createJsonArray(final List<Map<String, AttributeValue>> items) throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		final ArrayNode arrayNode = mapper.createArrayNode();
		items.forEach(map -> {
			final Map<String, Object> simpleMapValue = InternalUtils.toSimpleMapValue(map);
			try {
				arrayNode.add(mapper.readTree(mapper.writeValueAsString(simpleMapValue)));
			} catch (Exception e) {
			}
		});

		return mapper.writeValueAsString(arrayNode);
	}

	public APIParamsDAO getApiParamsDao() {
		if (this.apiParamsDao == null) {
			this.apiParamsDao = new APIParamsDAO();
		}
		return apiParamsDao;
	}
	
	// for testing
	void setApiParamsDao(APIParamsDAO apiParamsDao) {
		this.apiParamsDao = apiParamsDao;
	}
}
