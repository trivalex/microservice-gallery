package com.github.tvdtb.mediaresource.browser.control.io;

import java.io.ByteArrayInputStream;

public class StreamDtoBytesImpl extends StreamDtoRecoverableImpl {

	private byte[] data;

	public StreamDtoBytesImpl(byte[] data, String type, long lastModified) {
		super(type, data.length, lastModified);
		this.data = data;
	}

	public MultiReadInputStream createContent() {
		return new MultiReadInputStream(new ByteArrayInputStream(data));
	}

}
