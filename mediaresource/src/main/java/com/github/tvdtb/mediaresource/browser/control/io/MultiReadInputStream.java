package com.github.tvdtb.mediaresource.browser.control.io;

import java.io.IOException;
import java.io.InputStream;

public class MultiReadInputStream extends InputStream {

	private InputStream in;
	boolean marked = false;

	MultiReadInputStream(InputStream in) {
		this.in = in;
	}

	@Override
	public int read() throws IOException {
		int result = in.read();
		return result;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int result = in.read(b, off, len);
		return result;
	}

	@Override
	public synchronized void mark(int readlimit) {
		marked = true;
	}

	@Override
	public synchronized void reset() {
		marked = false;
	}

	@Override
	public void close() throws IOException {
		if (!marked) {
			in.close();
		}
	}
}