package com.frontm.db;

import static com.frontm.exception.FrontMException.FRONTM_ERROR_CODE.UNAVAILABLE_INSTANCE_ID;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.frontm.domain.db.AgentState;
import com.frontm.exception.FrontMException;
import org.apache.log4j.Logger;

public class AgentStateDAO extends BaseDAO {
	private static final Logger logger = Logger.getLogger(AgentStateDAO.class);

	public AgentState getAgentStateFromDB(String instanceId) throws FrontMException {
		Map<String, AttributeValue> queryValues = new HashMap<String, AttributeValue>();
		queryValues.put(":val1", new AttributeValue().withS(instanceId));
		DynamoDBQueryExpression<AgentState> queryExpression = new DynamoDBQueryExpression<AgentState>()
				.withKeyConditionExpression("instanceId = :val1")
				.withExpressionAttributeValues(queryValues);

		PaginatedQueryList<AgentState> results = getMapper().query(AgentState.class, queryExpression);
		if (results.isEmpty()) {
			logger.info("Instance ID not found: " + instanceId);
			throw new FrontMException(UNAVAILABLE_INSTANCE_ID);
		}

		return results.get(0);
	}

	public void saveAgentStateInDB(AgentState agentState) {
		getMapper().save(agentState);
	}
}
