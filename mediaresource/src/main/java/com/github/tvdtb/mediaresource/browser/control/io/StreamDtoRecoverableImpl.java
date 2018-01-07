package com.github.tvdtb.mediaresource.browser.control.io;

import java.io.InputStream;

public abstract class StreamDtoRecoverableImpl extends StreamDto {

	MultiReadInputStream content;

	public StreamDtoRecoverableImpl(String name, String type, long length, long lastModified) {
		super(name, type, length, lastModified);
	}

	public void mark(int length) {
		_getContent().mark(length);
	}

	public void reset() {
		_getContent().reset();
		content = null;
	}

	public InputStream getContent() {
		return _getContent();
	}

	private MultiReadInputStream _getContent() {
		if (content == null) {
			this.content = createContent();
		}
		return content;
	}

	protected abstract MultiReadInputStream createContent();

}
