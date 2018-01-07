package com.github.tvdtb.mediaresource.browser.control.io;

import java.io.ByteArrayInputStream;

public class StreamDtoBytesImpl extends StreamDtoRecoverableImpl {

	private byte[] data;

	public StreamDtoBytesImpl(String name, byte[] data, String type, long lastModified) {
		super(name, type, data.length, lastModified);
		this.data = data;
	}

	public MultiReadInputStream createContent() {
		return new MultiReadInputStream(new ByteArrayInputStream(data));
	}

}
