package com.github.tvdtb.mediaresource.browser.control.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tvdtb.mediaresource.MediaResource;

public class StreamDtoFileImpl extends StreamDtoRecoverableImpl {
	static Logger logger = LoggerFactory.getLogger(StreamDtoFileImpl.class);
	private File file;

	public StreamDtoFileImpl(File f, String type) {
		super(type, f.length(), f.lastModified());
		this.file = f;
	}

	@Override
	protected MultiReadInputStream createContent() {
		try {
			return new MultiReadInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			if (logger.isErrorEnabled())
				logger.error("Exception: {}", e);

			throw MediaResource.handleException(e);
		}
	}
}
