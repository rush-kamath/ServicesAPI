package com.frontm.function;

import static com.frontm.util.MessageQueueLambdaUtil.createLambdaInput;
import static com.frontm.util.MessageQueueLambdaUtil.invokeLambda;
import static com.frontm.util.MessageQueueLambdaUtil.logAndCreateErrorLambdaInput;
import static com.frontm.util.WebServiceUtil.callWebservice;
import static com.frontm.util.WebServiceUtil.createWebserviceCall;
import static com.frontm.util.WebServiceUtil.getWebServiceResponse;
import static com.frontm.util.WebServiceUtil.parseWebServiceResponse;
import static com.frontm.util.StringUtil.isEmpty;

import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.frontm.db.APIParamsDAO;
import com.frontm.db.CacheTableDAO;
import com.frontm.db.ConversationDAO;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.db.APIParameters;
import com.frontm.domain.db.Conversation;
import com.frontm.exception.FrontMException;
import com.frontm.util.JaxbParserUtil;
import com.frontm.util.JsonFilterUtil;

public class ServicesWSHandler implements RequestHandler<FrontMRequest, Void> {
	private static final Logger logger = Logger.getLogger(ServicesWSHandler.class);
	
	static final String BUILD_CACHE_COMMAND = "BuildCache";
	static final String GET_DATA_FROM_SERVICE_COMMAND = "GetDataFromService";
	private static final String INVALID_FORMAT_IN_DB = "Incorrect data in DB table APIParams. Only XML and JSON formats supported currently";
	private static final String INVALID_METHOD_IN_DB = "Incorrect data in DB table APIParams. Only GET and POST methods supported currently";
	private static final String MISSING_MAPPING_FOR_XML_FORMAT = "Incorrect data in DB table APIParams. XML format requires mapping information for JSON conversion";
	private static final String BUILD_CACHE_JSON = "Build cache command for webservices with JSON response is currently not supported";
	private static final String BUILD_CACHE_REQUIRED_FILEDS = "Incorrect data in DB table APIParams. Table and class name are required fields for Build Cache Command";
	private static final String UNKNOWN_COMMAND = "Only " + BUILD_CACHE_COMMAND + " and " + GET_DATA_FROM_SERVICE_COMMAND + " commands supported in this lambda";

	private APIParamsDAO apiParamsDao;
	private ConversationDAO conversationDao; 

	@Override
	public Void handleRequest(FrontMRequest request, Context context) {
		logger.info("Input parameters in the request: " + request);
		try {
			InvokeRequest invokeRequest = processInput(request);
			invokeLambda(request, invokeRequest);
		} catch (Exception e) {
			logger.error("Error occured:", e);
		}
		return null;
	}

	private InvokeRequest processInput(FrontMRequest request) throws Exception, JsonProcessingException {
		InvokeRequest invokeRequest = null;
		try {
			APIParameters apiParams = getApiParamsDao().getApiParamsFromDB(request);
			logger.debug("Details from the DB" + apiParams.toString());
			doValidations(apiParams, request);

			final Invocation.Builder invocationBuilder = createWebserviceCall(request, apiParams);
			final Response response = callWebservice(request, apiParams, invocationBuilder);
			final String webServiceResponse = getWebServiceResponse(apiParams, response);

			switch (request.getCommandName()) {
			case BUILD_CACHE_COMMAND:
				invokeRequest = buildCacheAndLambdaInput(webServiceResponse, apiParams, request);

				break;
			case GET_DATA_FROM_SERVICE_COMMAND:
				invokeRequest = processWSRespAndCreateLambdaInput(webServiceResponse, apiParams, request);
				break;
			}
		} catch (FrontMException e) {
			invokeRequest = logAndCreateErrorLambdaInput(request, e, "Error while processing");
		}
		return invokeRequest;
	}

	private InvokeRequest processWSRespAndCreateLambdaInput(final String webServiceResponse, APIParameters apiParams,
			FrontMRequest request) throws Exception {
		final String webServiceJson = parseWebServiceResponse(apiParams, webServiceResponse);
		final String filteredJson = JsonFilterUtil.filterJson(webServiceJson, request.getFilter());
		return createLambdaInput(request, filteredJson, true);
	}

	private InvokeRequest buildCacheAndLambdaInput(String webServiceResponse, APIParameters apiParams,
			FrontMRequest request) throws FrontMException, JsonProcessingException {
		final Conversation conversation = request.getConversation();
		if(conversation.isAnyFieldExceptUuidPresent()) {
			conversation.setConversationOwner(request.getUserUuid());
			getConversationDao().saveConversation(conversation);
		}
		final List<Map<String, String>> xmlContents = JaxbParserUtil.parseXMLToDBCacheItems(webServiceResponse,
				apiParams.getClassName(), request.getInstanceId());
		CacheTableDAO.insertItemsIntoDB(xmlContents, apiParams.getTableName());
		return createLambdaInput(request, "Building cache for " + request.getService() + " service is completed.",
				true);
	}
	
	private void doValidations(APIParameters apiParams, FrontMRequest request) throws FrontMException {
		if (!apiParams.isXMLFormat() && !apiParams.isJsonFormat()) {
			throw new FrontMException(INVALID_FORMAT_IN_DB);
		}
		
		if (!apiParams.isGetMethod() && !apiParams.isPostMethod()) {
			throw new FrontMException(INVALID_METHOD_IN_DB);
		}
		
		switch (request.getCommandName()) {
		case BUILD_CACHE_COMMAND:
			if (apiParams.isJsonFormat()) {
				throw new FrontMException(BUILD_CACHE_JSON);
			}
			if(isEmpty(apiParams.getTableName()) || isEmpty(apiParams.getClassName())) {
				throw new FrontMException(BUILD_CACHE_REQUIRED_FILEDS);
			}

			break;
		case GET_DATA_FROM_SERVICE_COMMAND:
			if (apiParams.isXMLFormat() && apiParams.getMapping() == null) {
				throw new FrontMException(MISSING_MAPPING_FOR_XML_FORMAT);
			}
			break;
		default:
			throw new FrontMException(UNKNOWN_COMMAND);
		}
	}

	public APIParamsDAO getApiParamsDao() {
		if (this.apiParamsDao == null) {
			this.apiParamsDao = new APIParamsDAO();
		}
		return apiParamsDao;
	}

	public ConversationDAO getConversationDao() {
		if (this.conversationDao == null) {
			this.conversationDao = new ConversationDAO();
		}
		return conversationDao;
	}

	// for testing
	void setApiParamsDao(APIParamsDAO apiParamsDao) {
		this.apiParamsDao = apiParamsDao;
	}

	void setConversationDao(ConversationDAO conversationDao) {
		this.conversationDao = conversationDao;
	}
}