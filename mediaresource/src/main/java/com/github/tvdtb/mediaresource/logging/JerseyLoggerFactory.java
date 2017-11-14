package com.github.tvdtb.mediaresource.logging;

import javax.ws.rs.ext.Provider;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InstantiationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Provider
public class JerseyLoggerFactory implements Factory<Logger> {

	@Autowired
	InstantiationService service;

	@Override
	public Logger provide() {
		String className = service.getInstantiationData().getParentInjectee().getInjecteeClass().getName();
		return LoggerFactory.getLogger(className);
	}

	@Override
	public void dispose(Logger instance) {
		// do nothing
	}

}
