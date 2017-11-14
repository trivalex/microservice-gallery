package com.github.tvdtb.mediaresource.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HateoasLink {
	@JsonIgnore
	private String rel;

	private String method;

	private String href;

	public HateoasLink() {

	}

	public HateoasLink(String rel, String method, String url) {
		super();
		this.rel = rel;
		this.method = method;
		this.href = url;
	}

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String url) {
		this.href = url;
	}

	@Override
	public String toString() {
		return "HateoasLink [rel=" + rel + ", method=" + method + ", href=" + href + "]";
	}
}
