package com.github.tvdtb.mediaresource.rest;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class HateoasDto<T> extends HateoasEntity {
	@JsonUnwrapped
	private T content;

	public HateoasDto() {
		this(null);
	}

	public HateoasDto(T entity) {
		this.content = entity;
	}

	public <X> HateoasDto<X> copyAs(Class<X> clazz) {
		HateoasDto<X> result = new HateoasDto<>((X) getContent());
		result.setLinks(new HashMap<>(getLinks()));
		return result;
	}

	public HateoasDto(T entity, HateoasLink link) {
		this.content = entity;
		getLinks().put(link.getRel(), link);
	}

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
	}

}
