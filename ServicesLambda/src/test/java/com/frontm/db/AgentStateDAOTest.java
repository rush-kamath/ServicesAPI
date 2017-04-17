package com.frontm.db;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;

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
import com.frontm.domain.db.AgentState;
import com.frontm.exception.FrontMException;
import com.frontm.exception.FrontMException.FRONTM_ERROR_CODE;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class AgentStateDAOTest {
	@Mock DynamoDBMapper mockMapper;
	@Mock PaginatedQueryList<AgentState> results;
	final String instanceId = "instanceId";

	private static AmazonDynamoDB actualDynamoDB;

	@BeforeClass
	public static void initialize() {
		actualDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.withCredentials(new ProfileCredentialsProvider()).build();
	}

	private AgentState callDaoWithMocks(String instanceId) throws FrontMException {
		AgentStateDAO dao = new AgentStateDAO();
		dao.setDynamoDBMapper(mockMapper);

		return dao.getAgentStateFromDB(instanceId);
	}

	@Test
	public void testUnavailableInstanceId() {
		when(mockMapper.query(eq(AgentState.class), any(DynamoDBQueryExpression.class))).thenReturn(results);
		when(results.isEmpty()).thenReturn(true);

		try {
			callDaoWithMocks(instanceId);
		} catch (FrontMException e) {
			assertEquals(FRONTM_ERROR_CODE.UNAVAILABLE_INSTANCE_ID, e.getErrorCode());
		}
	}

	@Test
	public void testAgentStateInDB() throws Exception {
		AgentState agentState = new AgentState();

		when(mockMapper.query(eq(AgentState.class), any(DynamoDBQueryExpression.class))).thenReturn(results);
		when(results.isEmpty()).thenReturn(false);
		when(results.get(0)).thenReturn(agentState);

		assertEquals(agentState, callDaoWithMocks(instanceId));
	}

	private AgentState callDaoWithRealDB(String instanceId) throws FrontMException {
		AgentStateDAO dao = getDaoWithRealDB();
		return dao.getAgentStateFromDB(instanceId);
	}

	public AgentStateDAO getDaoWithRealDB() {
		AgentStateDAO dao = new AgentStateDAO();
		dao.setDynamoDBMapper(new DynamoDBMapper(actualDynamoDB));
		return dao;
	}

	@Test
	public void testUnavailableAgentStateRealDB() {
		try {
			callDaoWithRealDB(instanceId);
		} catch (FrontMException e) {
			assertEquals(FRONTM_ERROR_CODE.UNAVAILABLE_INSTANCE_ID, e.getErrorCode());
		}
	}

	@Test
	public void testAvailableService() throws Exception {
		String[] contexts = {"role1", "role2"};
		AgentState agentState = AgentState.createNewAgent("creatorInstanceId", Arrays.asList(contexts));
		getDaoWithRealDB().saveAgentStateInDB(agentState);
		
		AgentState stateFromDB = callDaoWithRealDB(agentState.getInstanceId());
		assertEquals(agentState.getInstanceId(), stateFromDB.getInstanceId());
		assertEquals(agentState.getCreatorInstanceId(), stateFromDB.getCreatorInstanceId());
	}
}
