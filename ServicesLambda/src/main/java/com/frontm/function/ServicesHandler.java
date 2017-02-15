package com.frontm.function;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontm.db.APIParamsDAO;
import com.frontm.domain.APIParameters;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.FrontMRequest.Parameters;
import com.frontm.domain.ServicesWSInput;
import com.frontm.exception.FrontMException;

// TODO list
/* 
 * 4. add logic for XML body processing - check with G
*/
public class ServicesHandler implements RequestHandler<FrontMRequest, String> {
	private static final Logger logger = Logger.getLogger(ServicesHandler.class);

	static final String MISSING_SERVICE_DOMAIN_MESSAGE = "Domain and Service are required inputs. One or both are empty in the request";
	static final String MISSING_USER_UUID_MESSAGE = "User uuid is a required input. It is empty in the request";
	static final String INVALID_FORMAT_IN_DB = "Incorrect data in DB table APIParams. Only XML and JSON formats supported currently";
	static final String INVALID_METHOD_IN_DB = "Incorrect data in DB table APIParams. Only GET and POST methods supported currently";
	static final String MISSING_MAPPING_FOR_XML_FORMAT = "Incorrect data in DB table APIParams. XML format requires mapping information for JSON conversion";
	
	private APIParamsDAO apiParamsDao;

	@Override
	public String handleRequest(FrontMRequest input, Context context) {
		String returnMsg = null;
		logger.info("Input parameters in the request: " + input);

		try {
			validateInput(input);

			APIParameters apiParams = getApiParamsDao().getApiParamsFromDB(input);
			logger.debug("Details from the DB" + apiParams.toString());
			validateApiParams(apiParams);

			// call the webservice lambda
			InvokeRequest invokeRequest = createLambdaInput(input, apiParams);
			final InvokeResult invoke = AWSLambdaAsyncClientBuilder.defaultClient().invoke(invokeRequest);

			logger.info("After calling services ws function: " + invoke.getStatusCode());
			returnMsg = "Services API processing is in progress";

		} catch (FrontMException e) {
			logger.error(e.getMessage());
			returnMsg = e.getMessage();
		} catch (Exception e) {
			logger.error("Error occured:", e);
			returnMsg = e.getMessage();
		}

		return returnMsg;
	}

	private InvokeRequest createLambdaInput(FrontMRequest input, APIParameters apiParams)
			throws JsonProcessingException {
		final ServicesWSInput wsInput = new ServicesWSInput(input, apiParams);
		final String wsInputJson = new ObjectMapper().writeValueAsString(wsInput);
		logger.info(wsInputJson);

		InvokeRequest invokeRequest = new InvokeRequest();
		invokeRequest.setFunctionName(System.getenv("WS_FUNCTION"));
		invokeRequest.setPayload(wsInputJson);
		invokeRequest.setInvocationType(InvocationType.Event);
		return invokeRequest;
	}

	private boolean isEmpty(String string) {
		return string == null || string.trim().isEmpty();
	}

	private void validateInput(FrontMRequest input) throws FrontMException {
		if (isEmpty(input.getDomain()) || isEmpty(input.getService())) {
			throw new FrontMException(MISSING_SERVICE_DOMAIN_MESSAGE);
		}
		
		final Parameters parameters = input.getParameters();
		if(parameters == null || isEmpty(parameters.getUserUuid())) {
			throw new FrontMException(MISSING_USER_UUID_MESSAGE);
		}
	}

	private void validateApiParams(APIParameters apiParams) throws FrontMException {
		if (!apiParams.isXMLFormat() && !apiParams.isJsonFormat()) {
			throw new FrontMException(INVALID_FORMAT_IN_DB);
		}

		if (!apiParams.isGetMethod() && !apiParams.isPostMethod()) {
			throw new FrontMException(INVALID_METHOD_IN_DB);
		}
		
		if(apiParams.isXMLFormat() && apiParams.getMapping() == null) {
			throw new FrontMException(MISSING_MAPPING_FOR_XML_FORMAT);
		}
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