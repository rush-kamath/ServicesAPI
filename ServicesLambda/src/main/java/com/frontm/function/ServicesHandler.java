package com.frontm.function;

import static com.frontm.exception.FrontMException.FRONTM_ERROR_CODE.MISSING_INPUT;
import static com.frontm.util.StringUtil.isEmpty;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontm.db.AgentStateDAO;
import com.frontm.db.CommandsDAO;
import com.frontm.domain.AgentGuardLambdaInput;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.db.AgentState;
import com.frontm.domain.db.Command;
import com.frontm.domain.db.Conversation;
import com.frontm.exception.FrontMException;
import com.frontm.util.LambdaUtil;

public class ServicesHandler implements RequestHandler<AgentGuardLambdaInput, String> {
	private static final Logger logger = Logger.getLogger(ServicesHandler.class);
	private AgentStateDAO agentStateDao;
	private CommandsDAO commandsDao;

	@Override
	public String handleRequest(AgentGuardLambdaInput input, Context context) {
		String returnMsg = null;
		logger.info("Input parameters in the request: " + input);

		try {
			validateInput(input);

			Command command = getCommandsDao().getCommandFromDB(input.getCommand());
			logger.info("Command Details from the DB" + command);

			AgentState agentState = null;
			final String inputInstanceId = input.getInstanceId();
			if (isEmpty(inputInstanceId)) {
				agentState = AgentState.createNewAgent(input.getCreatorInstanceId(), input.getContexts());
				getAgentStateDao().saveAgentStateInDB(agentState);
			} else {
				agentState = getAgentStateDao().getAgentStateFromDB(inputInstanceId);
			}
			logger.info("AgentState Details: " + agentState);

			// call the command lambda
			InvokeRequest invokeRequest = createCommandLambdaInput(agentState, command, input.getParameters());
			final InvokeResult invoke = AWSLambdaAsyncClientBuilder.defaultClient().invoke(invokeRequest);
			logger.info("After calling command function: " + invoke.getStatusCode());

			returnMsg = createOutputJson(agentState.getInstanceId(), null);

		} catch (Exception e) {
			returnMsg = createOutputJson(null, e);
		}
		return returnMsg;
	}

	private String createOutputJson(String instanceId, Exception exception) {
		int returnErrorCode = 0;
		if (exception != null) {
			if (exception instanceof FrontMException) {
				returnErrorCode = ((FrontMException) exception).getErrorCode().getErrorNumber();
			} else {
				returnErrorCode = FrontMException.FRONTM_ERROR_CODE.UNMAPPED_ERROR.getErrorNumber();
			}
		}

		Map<String, String> map = new HashMap<>();
		map.put("instanceId", instanceId);
		map.put("error", String.valueOf(returnErrorCode));
		try {
			return new ObjectMapper().writeValueAsString(map);
		} catch (JsonProcessingException e) {
			logger.error("Unable to create ouput json: " + e.getMessage());
			return null;
		}
	}

	private InvokeRequest createCommandLambdaInput(AgentState agentState, Command command, FrontMRequest input)
			throws FrontMException {
		try {
			input.setCommandName(command.getCommandName());
			input.setInstanceId(agentState.getInstanceId());
			String lambdaInputJson = new ObjectMapper().writeValueAsString(input);
			logger.info(lambdaInputJson);

			return LambdaUtil.createInvokeRequest(command.getLambdaName(), lambdaInputJson);
		} catch (JsonProcessingException e) {
			throw new FrontMException("Unable to create command lambda input: " + e.getMessage());
		}
	}

	private void validateInput(AgentGuardLambdaInput input) throws FrontMException {
		validateAndThrowException(input.getCommand(), "Command");
		validateAndThrowException(input.getCreatorInstanceId(), "Creator Instance Id");

		if (isEmpty(input.getContexts())) {
			logger.info("Missing input: Contexts");
			throw new FrontMException(MISSING_INPUT);
		}

		final FrontMRequest request = input.getParameters();
		if (request == null) {
			logger.info("Missing input: Parameters");
			throw new FrontMException(MISSING_INPUT);
		}
		
		validateAndThrowException(request.getDomain(), "Domain");
		validateAndThrowException(request.getService(), "Service");
		validateAndThrowException(request.getUserUuid(), "User uuid");
		
		final Conversation conversation = request.getConversation();
		if (conversation == null) {
			logger.info("Missing input: conversation");
			throw new FrontMException(MISSING_INPUT);
		}
		validateAndThrowException(conversation.getUuid(), "conversation uuid");
	}

	private void validateAndThrowException(String inputStr, String fieldName) throws FrontMException {
		if (isEmpty(inputStr)) {
			logger.info("Missing input: " + fieldName);
			throw new FrontMException(MISSING_INPUT);
		}
	}
	
	public AgentStateDAO getAgentStateDao() {
		if (this.agentStateDao == null) {
			this.agentStateDao = new AgentStateDAO();
		}
		return agentStateDao;
	}

	public CommandsDAO getCommandsDao() {
		if (this.commandsDao == null) {
			this.commandsDao = new CommandsDAO();
		}
		return commandsDao;
	}

	// for testing
	void setAgentStateDao(AgentStateDAO apiParamsDao) {
		this.agentStateDao = apiParamsDao;
	}

	void setCommandsDao(CommandsDAO commandsDao) {
		this.commandsDao = commandsDao;
	}
}