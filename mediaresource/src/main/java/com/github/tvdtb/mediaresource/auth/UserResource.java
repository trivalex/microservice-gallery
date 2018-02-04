package com.github.tvdtb.mediaresource.auth;

import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Component;

import com.github.tvdtb.mediaresource.auth.model.LoginDto;
import com.github.tvdtb.mediaresource.config.control.ConfigControl;
import com.github.tvdtb.mediaresource.config.model.SeurityConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api("User")
@Component
@Path("user")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

	private SecretKey key;

	@Inject
	ConfigControl config;

	@Context
	UriInfo uriInfo;

	@PostConstruct
	public void init() {

		SeurityConfig security = config.getConfig().getSecurity();
		if (security != null) {
			String jwtKey = security.getJwtKey();
			System.out.println("KEY=" + jwtKey);
			byte[] keyBytes = Base64.getDecoder().decode(jwtKey);
			key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
		} else {
			key = MacProvider.generateKey();
		}
	}

	@ApiOperation(value = "getUser")
	@GET
	public Response getUser(@Context HttpHeaders headers) {
		String authentication = headers.getHeaderString("Authentication");
		Matcher matcher = Pattern.compile("Bearer (.*)").matcher(authentication);
		if (matcher.matches()) {
			String jwtString = matcher.group(1);

			Jwt jwt = Jwts.parser().setSigningKey(key).parse(jwtString);
			Claims body = (Claims) jwt.getBody();

			String username = body.getSubject();

			return Response.ok(username, MediaType.TEXT_PLAIN).build();
		} else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

	}

	@ApiOperation(value = "login")
	@PUT
	@Path("/login")
	public Response login(LoginDto json) {
		System.out.println("JSON=" + json);

		if (json.getUsername() == null || json.getUsername().trim().length() < 3
				|| !checkPassword(json.getUsername(), json.getPassword()))
			return Response.status(Response.Status.FORBIDDEN).build();

		String jwtString = Jwts.builder()//
				.setSubject(json.getUsername())//
				.setIssuedAt(new Date())//
				.signWith(SignatureAlgorithm.HS512, key).compact();

		return Response.ok(jwtString, MediaType.TEXT_PLAIN).build();
	}

	private boolean checkPassword(String username, String password) {
		// TODO implement me
		return password != null && password.equals(username.toLowerCase());
	}

}
