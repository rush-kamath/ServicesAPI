package com.frontm.util;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import com.frontm.domain.APIParameters;
import com.frontm.domain.FrontMRequest;
import com.frontm.domain.FrontMRequest.Parameters;

public class WebServiceUtil {

	private static final Logger logger = Logger.getLogger(WebServiceUtil.class);
	private static final String URL_QUERY_PARAM_DELIM = "=";

	public static String parseWebServiceResponse(APIParameters apiParams, Response response) throws Exception {
		final String responseContent = response.readEntity(String.class);
		logger.info("web service status: " + response.getStatusInfo());
		logger.info("web service headers: " + response.getStringHeaders());
		logger.info("web service response: " + responseContent);

		if (apiParams.isJsonFormat()) {
			return responseContent;
		} else {
			logger.info("parsing the XML to JSON");
			return ConvertXMLToJson.convert(responseContent, apiParams.getMappingJson(), apiParams.getService()).toString();
		}
	}

	public static Response getWebserviceResponse(FrontMRequest input, APIParameters apiParams,
			Invocation.Builder invocationBuilder) {
		Response response = null;
		if (apiParams.isGetMethod()) {
			logger.debug("Invoking the webservce with GET method");
			response = invocationBuilder.get();
		} else {
			logger.debug("Invoking the webservice with POST method");
			String jsonBody = null;

			final Parameters parameters = input.getParameters();
			if (parameters != null) {
				jsonBody = parameters.getBodyAsString();
				if (jsonBody != null) {
					if (apiParams.isXMLFormat()) {
						// TODO JSON to XML conversion
					}
				}
			}

			response = invocationBuilder.post(Entity.entity(jsonBody, MediaType.APPLICATION_JSON));
		}
		return response;
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
		target = addParamsToWebTarget(input.getParameters(), target);
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
	static WebTarget addParamsToWebTarget(Parameters parameters, WebTarget originalTarget) {
		if (parameters == null) {
			return originalTarget;
		}

		WebTarget finalTarget = originalTarget;
		final String object = parameters.getObject();
		if (isNotEmpty(object)) {
			logger.debug("Added " + object + " to path");
			finalTarget = finalTarget.path(object);
		}

		final List<String> queryStrings = parameters.getQueryString();
		if (queryStrings == null || queryStrings.isEmpty()) {
			return finalTarget;
		}

		logger.debug("Added query string to path: " + queryStrings);
		for (String queryString : queryStrings) {
			if (queryString == null) {
				continue;
			}

			final String[] split = queryString.split(URL_QUERY_PARAM_DELIM);
			if (split.length < 2) {
				continue;
			}
			finalTarget = finalTarget.queryParam(split[0], split[1]);
		}

		return finalTarget;
	}

	public static boolean isNotEmpty(String string) {
		return !isEmpty(string);
	}

	public static boolean isEmpty(String string) {
		return string == null || string.trim().isEmpty();
	}

}
