package com.github.tvdtb.mediaresource;

import java.util.TimeZone;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

//@Component
//@Provider
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {

	private final ObjectMapper objectMapper;

	public ObjectMapperResolver() {
		super();
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		ISO8601DateFormat format = new ISO8601DateFormat();
		format.setTimeZone(TimeZone.getDefault());
		objectMapper.setDateFormat(format);
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return objectMapper;
	}
}
