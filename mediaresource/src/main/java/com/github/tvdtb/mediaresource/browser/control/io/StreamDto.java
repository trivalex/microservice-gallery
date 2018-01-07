package com.github.tvdtb.mediaresource.browser.control.io;

import java.io.File;
import java.io.InputStream;

public abstract class StreamDto {

	String type;
	private long length;
	private long date;
	private String name;

	public StreamDto(String name, String type, long length, long date) {
		this.name = name;
		this.type = type;
		this.length = length;
		this.date = date;
	}

	public static StreamDto fromFile(File f, String type) {
		return new StreamDtoFileImpl(f, type);
	}

	public static StreamDto fromBytes(String name, byte[] data, String type, long lastModified) {
		return new StreamDtoBytesImpl(name, data, type, lastModified);
	}

	public void mark() {
		this.mark(-1);
	}

	public abstract void mark(int length);

	public abstract void reset();

	public abstract InputStream getContent();

	public String getType() {
		return type;
	}

	public long getDate() {
		return date;
	}

	public long getLength() {
		return length;
	}

	public String getName() {
		return name;
	}

}
