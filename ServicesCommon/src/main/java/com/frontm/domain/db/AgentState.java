package com.frontm.domain.db;

import java.util.List;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "AgentState")
public class AgentState {
	private String instanceId;
	private String creatorInstanceId;
	private List<String> contexts;

	@DynamoDBHashKey
	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
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
		return "AgentState [instanceId=" + instanceId + ", creatorInstanceId=" + creatorInstanceId + ", contexts="
				+ contexts + "]";
	}

	public static AgentState createNewAgent(String creatorInstanceId, List<String> contexts) {
		AgentState agentState = new AgentState();
		agentState.setInstanceId(UUID.randomUUID().toString());
		agentState.setCreatorInstanceId(creatorInstanceId);
		agentState.setContexts(contexts);
		return agentState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentState other = (AgentState) obj;
		if (instanceId == null) {
			if (other.instanceId != null)
				return false;
		} else if (!instanceId.equals(other.instanceId))
			return false;
		return true;
	}
	
	
}
