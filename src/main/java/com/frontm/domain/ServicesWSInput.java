package com.frontm.domain;

public class ServicesWSInput {
	private APIParameters apiParameters;
	private FrontMRequest request;

	public ServicesWSInput() {
		
	}
	
	public ServicesWSInput(FrontMRequest request, APIParameters apiParameters) {
		this.apiParameters = apiParameters;
		this.request = request;
	}
	
	public APIParameters getApiParameters() {
		return apiParameters;
	}

	public void setApiParameters(APIParameters apiParameters) {
		this.apiParameters = apiParameters;
	}

	public FrontMRequest getRequest() {
		return request;
	}

	public void setRequest(FrontMRequest request) {
		this.request = request;
	}

	@Override
	public String toString() {
		return "ServicesWSInput [apiParameters=" + apiParameters + ", request=" + request + "]";
	}

}
