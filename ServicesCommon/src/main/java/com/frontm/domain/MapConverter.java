package com.frontm.domain;

import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MapConverter implements DynamoDBTypeConverter<Map<String, AttributeValue>, String> {
	private static final Logger logger = Logger.getLogger(MapConverter.class);

	@Override
	public Map<String, AttributeValue> convert(String object) {
		Item item = new Item().withJSON("document", object);
		Map<String,AttributeValue> attributes = InternalUtils.toAttributeValues(item);
		return attributes.get("document").getM();
	}

	@Override
	public String unconvert(Map<String, AttributeValue> object) {
		try {
			final Map<String, Object> simpleMapValue = InternalUtils.toSimpleMapValue(object);
			return new ObjectMapper().writeValueAsString(simpleMapValue);
		} catch (JsonProcessingException e) {
			logger.error(e);
			return null;
		}
	}
}
