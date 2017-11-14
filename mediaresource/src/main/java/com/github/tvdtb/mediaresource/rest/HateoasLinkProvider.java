package com.github.tvdtb.mediaresource.rest;

import java.security.Principal;

import javax.ws.rs.core.UriInfo;

@FunctionalInterface
public interface HateoasLinkProvider {
	void accept(UriInfo uriInfo, Principal principal, HateoasEntity result);
}
