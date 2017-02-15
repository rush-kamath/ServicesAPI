package com.frontm.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@DynamoDBTable(tableName="frontm-mobilehub-1030065648-APIParameters")
public class APIParameters {
	private String domain;
	private String service;
	private String apiKey;
	private boolean flattenResponseRequired;
	private String format;
	private String method;
	private String url;
	private String uuid;
	
//	@DynamoDBTypeConverted(converter = MapConverter.class)
	private String mapping;

	public static final String JSON_FORMAT = "JSON";
	public static final String XML_FORMAT = "XML";
	
	public static final String GET_METHOD = "GET";
	public static final String POST_METHOD = "POST";

	public APIParameters() {
	
	}
	
	public APIParameters(String domain, String service) {
		this.domain = domain;
		this.service = service;
	}

	@DynamoDBHashKey
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	@DynamoDBRangeKey
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public boolean isFlattenResponseRequired() {
		return flattenResponseRequired;
	}

	public void setFlattenResponseRequired(boolean flattenResponseRequired) {
		this.flattenResponseRequired = flattenResponseRequired;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return "APIParameters [domain=" + domain + ", service=" + service + ", apiKey=" + apiKey
				+ ", flattenResponseRequired=" + flattenResponseRequired + ", format=" + format + ", method=" + method
				+ ", url=" + url + ", uuid=" + uuid + ", mapping=" + Jackson.toJsonPrettyString(mapping) + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
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
		APIParameters other = (APIParameters) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		return true;
	}

	// helper methods
	public APIParameters withDomain(String domain) {
		this.domain = domain;
		return this;
	}
	
	public boolean isJsonFormat() {
		return JSON_FORMAT.equals(this.getFormat());
	}
	
	public boolean isXMLFormat() {
		return XML_FORMAT.equals(this.getFormat());
	}
	
	public boolean isGetMethod() {
		return GET_METHOD.equals(this.getMethod());
	}
	
	public boolean isPostMethod() {
		return POST_METHOD.equals(this.getMethod());
	}

	public String getMapping() {
		return mapping;
	}
	
	public void setMapping(String mapping) {
		this.mapping = mapping;
	}
	
	@JsonIgnore
	public JsonNode getMappingJson() throws Exception {
		 // mapping is not a required field for JSON formats. If it is null, do not attempt creating JSON node.
		if(mapping == null) {
			return null;
		}
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readTree(mapping);
	}
}
