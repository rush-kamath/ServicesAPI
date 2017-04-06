package com.frontm.util;

import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;

public class LambdaUtil {

	public static InvokeRequest createInvokeRequest(String lambdaName, String inputJson) {
		InvokeRequest invokeRequest = new InvokeRequest();
		invokeRequest.setFunctionName(lambdaName);
		invokeRequest.setPayload(inputJson);
		invokeRequest.setInvocationType(InvocationType.Event);
		return invokeRequest;
	}
}
