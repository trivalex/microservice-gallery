package com.github.tvdtb.mediaresource.browser.control.io;

import java.io.File;
import java.io.InputStream;

public abstract class StreamDto {

	String type;
	private long length;
	private long date;
	private String name;
	private String path;

	public StreamDto(String name, String path, String type, long length, long date) {
		this.name = name;
		this.path = path;
		this.type = type;
		this.length = length;
		this.date = date;
	}

	public static StreamDto fromFile(File f, String path, String type) {
		return new StreamDtoFileImpl(f, path, type);
	}

	public static StreamDto fromBytes(String name, String path, byte[] data, String type, long lastModified) {
		return new StreamDtoBytesImpl(name, path, data, type, lastModified);
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

	public String getPath() {
		return path;
	}

}
