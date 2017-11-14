package com.github.tvdtb.mediaresource.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class RESTExceptionHandler implements ExceptionMapper<Exception> {

	// @Autowired
	Logger logger = LoggerFactory.getLogger(RESTExceptionHandler.class.getName());

	public RESTExceptionHandler() {
	}

	public static class JsonError {
		int status;
		String message;

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}

	@Override
	public Response toResponse(Exception exception) {
		int status = 500;
		if (exception instanceof WebApplicationException) {
			status = ((WebApplicationException) exception).getResponse().getStatus();
		}
		JsonError entity = new JsonError();
		entity.setStatus(status);
		entity.setMessage(exception.getMessage());

		if (logger.isErrorEnabled())
			logger.error("REST Exception ", exception);

		return Response.status(status).entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build();
	}

}
