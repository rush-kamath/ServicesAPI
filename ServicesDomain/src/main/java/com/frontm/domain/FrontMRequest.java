package com.frontm.domain;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FrontMRequest {
	private String domain;
	private String service;
	private String username;
	private String password;
	private Parameters parameters;

	public static class Parameters {
		private String object;
		private String conversationId;
		private String userUuid;
		private Boolean push;
		private List<String> queryString;
		private Map<String, Object> body;

		public String getObject() {
			return object;
		}

		public void setObject(String object) {
			this.object = object;
		}

		public List<String> getQueryString() {
			return queryString;
		}

		public void setQueryString(List<String> queryString) {
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

		public String getConversationId() {
			return conversationId;
		}
		
		public void setConversationId(String conversationId) {
			this.conversationId = conversationId;
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

		@Override
		public String toString() {
			return "Parameters [object=" + object + ", conversationId=" + conversationId + ", userUuid=" + userUuid
					+ ", push=" + push + ", queryString=" + queryString + ", body=" + getBodyAsString() + "]";
		}
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

	public Parameters getParameters() {
		return parameters;
	}

	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return "FrontMRequest [domain=" + domain + ", service=" + service + ", username=" + username + ", password="
				+ password + ", parameters=" + parameters + "]";
	}

}
