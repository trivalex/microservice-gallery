package com.github.tvdtb.mediaresource.config.control;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.imaging.util.IoUtils;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.github.tvdtb.mediaresource.config.model.Configuration;

@Component
@ConfigurationProperties("application")
public class ConfigControl {

	@Inject
	Logger logger;

	String config;

	private Configuration configuration;

	public Configuration getConfig() {
		return configuration;
	}

	@PostConstruct
	public void readConfig() {

		String json = config;
		if (config.startsWith("file:")) {
			json = readFile(config.substring(5));
		}

		configuration = JSON.parseObject(json, Configuration.class);
	}

	private String readFile(String fileName) {
		try {
			File f = new File(fileName);
			byte[] jsonData = IoUtils.getFileBytes(f);
			String json = new String(jsonData, "UTF-8");
			return json;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setConfig(String config) {
		this.config = config;
	}

}
