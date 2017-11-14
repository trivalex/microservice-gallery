package com.github.tvdtb.mediaresource.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api("hateoas")
@Component
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
public class HateoasResource {
	@Context
	UriInfo uriInfo;
	
	@Context
	SecurityContext security;

	@Autowired
	HateoasRegistry registry;

	@ApiOperation("Provide HATEOAS root links for frontend")
	@GET
	public Response getLinks() {

		HateoasDto<HateoasEntity> result = new HateoasDto<>(null);

		registry.getConsumers().forEach(consumer -> consumer.accept(uriInfo, security.getUserPrincipal(), result));
		result.addLink(Hateoas.fromCaller(uriInfo));

		return Response.ok(result).build();
	}

	public static class HateoasEntity {

	}

}
