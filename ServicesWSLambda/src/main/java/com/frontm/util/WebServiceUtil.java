package com.frontm.util;

import static com.frontm.util.StringUtil.isNotEmpty;
import static com.frontm.util.StringUtil.processQueryString;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.apache.log4j.Logger;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.frontm.domain.FrontMRequest;
import com.frontm.domain.db.APIParameters;
import com.frontm.exception.FrontMException;

public class WebServiceUtil {
	private static final Logger logger = Logger.getLogger(WebServiceUtil.class);

	public static String getWebServiceResponse(APIParameters apiParams, Response response) throws Exception {
		final String responseContent = response.readEntity(String.class);
		final int status = response.getStatus();
		final StatusType statusInfo = response.getStatusInfo();
		logger.info("web service status: " + status + " " + statusInfo);
		logger.debug("web service response: " + responseContent);

		if (Response.Status.OK.getStatusCode() == status) {
			return responseContent;
		} else {
			logger.info("throwing FrontMException exception since status is: " + statusInfo);
			throw new FrontMException(status + " " + statusInfo + " " + responseContent);
		}
	}


	public static String parseWebServiceResponse(APIParameters apiParams, final String responseContent) throws Exception {
		if (apiParams.isJsonFormat()) {
			return responseContent;
		} else {
			logger.info("parsing the XML to JSON");
			return ConvertXMLToJson.convert(responseContent, apiParams.getMappingJson()).toString();
		}
	}
	

	public static Response callWebservice(FrontMRequest input, APIParameters apiParams,
			Invocation.Builder invocationBuilder) throws FrontMException {
		Response response = null;
		try {
			if (apiParams.isGetMethod()) {
				logger.debug("Invoking the webservce with GET method");
				response = invocationBuilder.get();
			} else {
				logger.debug("Invoking the webservice with POST method");
				String jsonBody = input.getBodyAsString();
				if (jsonBody != null) {
					if (apiParams.isXMLFormat()) {
						// TODO JSON to XML conversion
					}
				}
				response = invocationBuilder.post(Entity.entity(jsonBody, MediaType.APPLICATION_JSON));
			}
			return response;

		} catch (Exception e) {
			logger.error("Error occured while invoking webservice: ", e);
			throw new FrontMException(e.getMessage());
		}
	}

	public static Invocation.Builder createWebserviceCall(FrontMRequest input, APIParameters apiParams) {
		final Client client = ClientBuilder.newClient();

		final String userName = input.getUsername();
		final String password = input.getPassword();
		if (isNotEmpty(userName) && isNotEmpty(password)) {
			logger.info("setting userName and password for auth: " + userName + "/" + password);
			HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(userName, password);
			client.register(feature);
		}

		WebTarget target = client.target(apiParams.getUrl());
		target = addParamsToWebTarget(input, target);
		logger.info("Final Uri" + target.getUri().toString());

		Invocation.Builder invocationBuilder = target
				.request(apiParams.isJsonFormat() ? MediaType.APPLICATION_JSON : MediaType.APPLICATION_XML);

		return invocationBuilder;
	}

	/*
	 * Assumption: the query string in the input json is a string array. Each
	 * element is of the format "key=value", where key is a string. value can be
	 * multiple strings delimited by commas. The delimiter between the string is
	 * "=". If an element of the array does not follow this format, it will be
	 * ignored and will not be available in the final webservice uri
	 */
	static WebTarget addParamsToWebTarget(FrontMRequest request, WebTarget originalTarget) {
		WebTarget finalTarget = originalTarget;
		final String object = request.getObject();
		if (isNotEmpty(object)) {
			logger.debug("Added " + object + " to path");
			finalTarget = finalTarget.path(object);
		}

		final Map<String, String> queryMap = processQueryString(request.getQueryString());
		for(String key : queryMap.keySet()) {
			String value = queryMap.get(key);
			finalTarget = finalTarget.queryParam(key, value);
			logger.info("Added query string to path: " + key + "=" + value);
		}
		return finalTarget;
	}
}
