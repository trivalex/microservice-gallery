package com.github.tvdtb.mediaresource.rest;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HateoasEntity {

	@JsonProperty("_links")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Map<String, HateoasLink> links;

	public HateoasEntity() {
		this.links = new TreeMap<>();
	}

	public Map<String, HateoasLink> getLinks() {
		return links;
	}

	public void setLinks(Map<String, HateoasLink> links) {
		this.links = links;
	}

	public void addLink(HateoasLink link) {
		if (this.links == null) {
			this.links = new TreeMap<>();
		}
		this.links.put(link.getRel(), link);
	}

	public String getLinkHref(String linkName) {
		HateoasLink link = this.links.get(linkName);
		if (link != null)
			return link.getHref();
		return null;
	}

}
