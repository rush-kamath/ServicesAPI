package com.frontm.function;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.mockito.Matchers.any;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.frontm.db.AgentStateDAO;
import com.frontm.db.CommandsDAO;
import com.frontm.domain.AgentGuardLambdaInput;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.db.AgentState;
import com.frontm.domain.db.Command;
import com.frontm.domain.db.Conversation;
import com.frontm.exception.FrontMException;
import com.frontm.exception.FrontMException.FRONTM_ERROR_CODE;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AgentState.class, AWSLambdaAsyncClientBuilder.class })
public class ServicesHandlerTest {
	@Mock
	AgentStateDAO agentStateDao;
	@Mock
	CommandsDAO commandsDao;
	@Mock AWSLambdaAsync lambdaAsync;
	@Mock InvokeResult invokeResult; 
	

	final String instanceId = "instanceId";
	final String commandName = "commandName";
	final String creatorInstanceId = "creatorInstanceId";
	final String context = "role1";
	final List<String> contexts = Arrays.asList(context);

	private String callHandlerWithMocks(AgentGuardLambdaInput input) {
		ServicesHandler handler = new ServicesHandler();
		handler.setAgentStateDao(agentStateDao);
		handler.setCommandsDao(commandsDao);

		return handler.handleRequest(input, new TestContext());
	}

	private AgentGuardLambdaInput populateAgentGuardLambdaInput() {
		final AgentGuardLambdaInput input = new AgentGuardLambdaInput();
		input.setCommand(commandName);
		input.setCreatorInstanceId(creatorInstanceId);
		input.setContexts(contexts);
		final FrontMRequest parameters = new FrontMRequest();
		parameters.setDomain("domain");
		parameters.setService("service");
		parameters.setUserUuid("userUuid");
		final Conversation conversation = new Conversation();
		parameters.setConversation(conversation);
		conversation.setUuid("uuid");
		input.setParameters(parameters);
		return input;
	}

	private AgentState createNewAgentState() {
		final AgentState state = new AgentState();
		state.setInstanceId(instanceId);
		state.setCreatorInstanceId(creatorInstanceId);
		state.setContexts(contexts);
		return state;
	}

	@Before
	public void before() {
		mockStatic(AgentState.class);
		mockStatic(AWSLambdaAsyncClientBuilder.class);
		when(AgentState.createNewAgent(creatorInstanceId, contexts)).thenReturn(createNewAgentState());
		when(AWSLambdaAsyncClientBuilder.defaultClient()).thenReturn(lambdaAsync);
	}

	@Test
	public void testInvalidRequest() {
		final AgentGuardLambdaInput input = new AgentGuardLambdaInput();
		assertEquals("{\"instanceId\":null,\"error\":\"3\"}", callHandlerWithMocks(input));

		input.setCommand(commandName);
		assertEquals("{\"instanceId\":null,\"error\":\"3\"}", callHandlerWithMocks(input));

		input.setCreatorInstanceId(creatorInstanceId);
		assertEquals("{\"instanceId\":null,\"error\":\"3\"}", callHandlerWithMocks(input));

		input.setContexts(contexts);
		assertEquals("{\"instanceId\":null,\"error\":\"3\"}", callHandlerWithMocks(input));
		
		final FrontMRequest parameters = new FrontMRequest();
		input.setParameters(parameters);
		assertEquals("{\"instanceId\":null,\"error\":\"3\"}", callHandlerWithMocks(input));
		
		parameters.setDomain("domain");
		assertEquals("{\"instanceId\":null,\"error\":\"3\"}", callHandlerWithMocks(input));
		
		parameters.setService("service");
		assertEquals("{\"instanceId\":null,\"error\":\"3\"}", callHandlerWithMocks(input));
		
		parameters.setUserUuid("userUuid");
		assertEquals("{\"instanceId\":null,\"error\":\"3\"}", callHandlerWithMocks(input));
		
		final Conversation conversation = new Conversation();
		parameters.setConversation(conversation);
		assertEquals("{\"instanceId\":null,\"error\":\"3\"}", callHandlerWithMocks(input));
		
		conversation.setUuid("");
		assertEquals("{\"instanceId\":null,\"error\":\"3\"}", callHandlerWithMocks(input));
	}

	@Test
	public void testNullInstanceIdInvalidCommand() throws FrontMException {
		when(commandsDao.getCommandFromDB(commandName)).thenThrow(new FrontMException(FRONTM_ERROR_CODE.UNAVAILABLE_COMMAND));

		AgentGuardLambdaInput input = populateAgentGuardLambdaInput();
		String returnVal = callHandlerWithMocks(input);

		verify(agentStateDao, times(0)).saveAgentStateInDB(createNewAgentState());
		verify(commandsDao, times(1)).getCommandFromDB(commandName);
		assertEquals("{\"instanceId\":null,\"error\":\"2\"}", returnVal);

	}
	
	@Test
	public void testInvalidInstanceId() throws FrontMException {
		when(agentStateDao.getAgentStateFromDB(instanceId)).thenThrow(new FrontMException(FRONTM_ERROR_CODE.UNAVAILABLE_INSTANCE_ID));

		AgentGuardLambdaInput input = populateAgentGuardLambdaInput();
		input.setInstanceId(instanceId);
		assertEquals("{\"instanceId\":null,\"error\":\"1\"}", callHandlerWithMocks(input));
	}
	
	@Test
	public void testValidFlow() throws FrontMException {
		when(commandsDao.getCommandFromDB(commandName)).thenReturn(new Command(commandName));
		when(lambdaAsync.invoke(any(InvokeRequest.class))).thenReturn(invokeResult);
		when(invokeResult.getStatusCode()).thenReturn(202);

		AgentGuardLambdaInput input = populateAgentGuardLambdaInput();
		assertEquals("{\"instanceId\":\"instanceId\",\"error\":\"0\"}", callHandlerWithMocks(input));
	}
}
