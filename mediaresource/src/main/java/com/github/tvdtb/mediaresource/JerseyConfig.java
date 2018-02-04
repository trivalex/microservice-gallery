package com.github.tvdtb.mediaresource;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.github.tvdtb.mediaresource.auth.UserResource;
import com.github.tvdtb.mediaresource.browser.BrowserResource;
import com.github.tvdtb.mediaresource.streams.StreamResource;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

@Component
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {
		register(MultiPartFeature.class);

		// http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-developing-web-applications.html
		register(StreamResource.class);
		register(BrowserResource.class);
		register(UserResource.class);

		// http://tech.asimio.net/2016/04/05/Microservices-using-Spring-Boot-Jersey-Swagger-and-Docker.html
		this.register(ApiListingResource.class);
		this.register(SwaggerSerializers.class);

		BeanConfig config = new BeanConfig();
		config.setConfigId("com.github.tvdtb.mediaresource");
		config.setTitle("Image Manager");
		config.setVersion("v1");
		config.setContact("tvdtb");
		config.setSchemes(new String[] { "http", "https" });
		config.setBasePath("/api");
		config.setResourcePackage(getClass().getPackage().getName());
		config.setPrettyPrint(true);
		config.setScan(true);
	}

}
