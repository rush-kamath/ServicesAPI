package com.frontm.domain;

import java.util.List;

public class AgentGuardLambdaInput {
	private String instanceId;
	private String command;
	private String creatorInstanceId;
	private List<String> contexts;
	private FrontMRequest parameters;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public FrontMRequest getParameters() {
		return parameters;
	}

	public void setParameters(FrontMRequest parameters) {
		this.parameters = parameters;
	}

	public String getCreatorInstanceId() {
		return creatorInstanceId;
	}

	public void setCreatorInstanceId(String creatorInstanceId) {
		this.creatorInstanceId = creatorInstanceId;
	}

	public List<String> getContexts() {
		return contexts;
	}

	public void setContexts(List<String> contexts) {
		this.contexts = contexts;
	}

	@Override
	public String toString() {
		return "AgentGuardLambdaInput [instanceId=" + instanceId + ", command=" + command + ", creatorInstanceId="
				+ creatorInstanceId + ", contexts=" + contexts + ", parameters=" + parameters + "]";
	}

}
