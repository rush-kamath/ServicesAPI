package com.frontm.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontm.domain.db.Conversation;

public class FrontMRequest {
	private String domain;
	private String service;
	private String username;
	private String password;
	private String filter;
	private String object;
	private Conversation conversation;
	private String userUuid;
	private Boolean push;
	private String queryString;
	private Map<String, Object> body;
	private String instanceId;
	private String commandName;

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public Map<String, Object> getBody() {
		return body;
	}

	@JsonIgnore
	public String getBodyAsString() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(body);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	public void setBody(Map<String, Object> body) {
		this.body = body;
	}

	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}

	public Boolean getPush() {
		return push;
	}

	public void setPush(Boolean push) {
		this.push = push;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	@Override
	public String toString() {
		return "FrontMRequest [domain=" + domain + ", service=" + service + ", username=" + username + ", password="
				+ password + ", filter=" + filter + ", object=" + object + ", conversation=" + conversation
				+ ", userUuid=" + userUuid + ", push=" + push + ", queryString=" + queryString + ", body="
				+ getBodyAsString() + ", instanceId=" + instanceId + ", commandName=" + commandName + "]";
	}
}
