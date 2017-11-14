package com.github.tvdtb.mediaresource.rest;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.DELETE;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class Hateoas {

	public static HateoasLink fromCaller(UriInfo uriInfo, Object... values) {
		return Hateoas.fromCallerByIndex(uriInfo, 1, values); // this method = 1
	}

	public static HateoasLink fromCallerByIndex(UriInfo uriInfo, int index, Object... values) {
		index++; // getStackTrace method
		index++; // fromCaller
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements[index].getMethodName().startsWith("lambda$")) {
			index++;
			while (stackTraceElements[index].getClassName().startsWith("java.util"))
				index++;
		}
		try {
			return fromMethod(uriInfo, "self", Class.forName(stackTraceElements[index].getClassName()),
					stackTraceElements[index].getMethodName(), values);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static HateoasLink fromMethod(UriInfo uriInfo, String rel, Class<?> resource, String method,
			Object... values) {
		return _fromMethod(uriInfo, rel, resource, method, null, values);
	}

	public static HateoasLink fromMethodQueryParameters(UriInfo uriInfo, String rel, Class<?> resource, String method,
			Consumer<UriBuilder> consumer, Object... values) {
		return _fromMethod(uriInfo, rel, resource, method, consumer, values);
	}

	private static HateoasLink _fromMethod(UriInfo uriInfo, String rel, Class<?> resource, String method,
			Consumer<UriBuilder> consumer, Object[] values) {

		UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(resource);
		try {
			uriBuilder = uriBuilder.path(resource, method);
		} catch (IllegalArgumentException iae) {
			// ignorieren - Methode hat kein @Path
		}

		if (consumer != null)
			consumer.accept(uriBuilder);

		uriBuilder.scheme(null).host(null).port(-1);
		String tpl = uriBuilder.toTemplate();

		if (values != null && values.length > 0) {
			Pattern p = Pattern.compile("([^\\{]*)\\{([^\\{]+)\\}(.*)");
			for (Object v : values) {
				Matcher m = p.matcher(tpl);
				if (m.matches()) {
					tpl = m.group(1) + v.toString() + m.group(3);
				} else
					break;
			}

			try {
				Link build = Link.fromUriBuilder(uriBuilder) //
						.rel(rel) //
						.build();
				tpl = build.getUri().toString();
			} catch (IllegalArgumentException iae) {
				// ignore - wir nutzen das Template wenn nicht alle Werte
				// vorhanden sind
				// TODO - Links die nur teilweise gefüllt werden müssen
				// unterstützt werden
			}
		}

		HateoasLink result = new HateoasLink(rel, "GET", tpl);

		Arrays.stream(resource.getMethods()).filter(m -> method.equals(m.getName())).findFirst().ifPresent(m -> {
			if (m.getAnnotation(POST.class) != null)
				result.setMethod("POST");
			if (m.getAnnotation(PUT.class) != null)
				result.setMethod("PUT");
			if (m.getAnnotation(DELETE.class) != null)
				result.setMethod("DELETE");
			if (m.getAnnotation(HEAD.class) != null)
				result.setMethod("HEAD");
		});

		return result;
	}

}
