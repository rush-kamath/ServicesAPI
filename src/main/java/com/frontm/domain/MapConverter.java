package com.frontm.domain;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MapConverter implements DynamoDBTypeConverter<String, Map<String, Object>> {
	private static final Logger logger = Logger.getLogger(MapConverter.class);
	
	@Override
	public String convert(Map<String,Object> object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JsonProcessingException e) {
			logger.error("Unable to convert to String. Map is: " + object, e);
			return null;
		}
	}

	@Override
	public Map<String,Object> unconvert(String object) {
		ObjectMapper m = new ObjectMapper();
		try {
			return m.readValue(object, new TypeReference<Map<String, Object>>() {});
		} catch (IOException e) {
			logger.error("Unable to convert to map. String is: " + object, e);
			return null;
		}
	}
}
