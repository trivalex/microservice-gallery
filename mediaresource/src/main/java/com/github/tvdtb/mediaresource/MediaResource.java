package com.github.tvdtb.mediaresource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * http://localhost:8080/api/ http://localhost:8080/swagger/index.html
 * http://localhost:8080/ui/index.html
 * 
 * @author brt
 *
 */
@SpringBootApplication
@Configuration
public class MediaResource {
	public static void main(String[] args) {
		SpringApplication.run(MediaResource.class, args);
	}

	public static RuntimeException handleException(Exception e) {
		if (e instanceof RuntimeException)
			throw (RuntimeException) e;
		else
			throw new RuntimeException(e);
	}

}
