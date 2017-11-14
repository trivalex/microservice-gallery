package com.github.tvdtb.mediaresource.rest;

import java.util.TreeMap;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

@Component
@ApplicationScope
public class HateoasRegistry {

	@Autowired
	Logger logger;

	private TreeMap<Class<?>, HateoasLinkProvider> map;

	@PostConstruct
	public void init() {
		this.map = new TreeMap<>((a, b) -> a.getName().compareTo(b.getName()));
	}

	public void register(Class<?> clazz, HateoasLinkProvider linkProvider) {
		if (logger.isInfoEnabled())
			logger.info("Registriere HATEOAS {}", clazz);
		map.put(clazz, linkProvider);
	}

	public Stream<HateoasLinkProvider> getConsumers() {
		return map.values().stream();
	}

}
