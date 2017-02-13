package com.frontm.serialize;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.frontm.domain.MessageQueue.Item.Content;

@SuppressWarnings("serial")
public class ContentSerailizer extends StdSerializer<Content> {
	public ContentSerailizer() {
		this(null);
	}

	public ContentSerailizer(Class<Content> t) {
		super(t);
	}

	@Override
	public void serialize(Content value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("contentType", value.getContentType());
		if (value.getError() == null) {
			jgen.writeStringField("details", value.getDetails());
		} else {
			jgen.writeStringField("error", value.getError());
		}
		jgen.writeEndObject();
	}
}
