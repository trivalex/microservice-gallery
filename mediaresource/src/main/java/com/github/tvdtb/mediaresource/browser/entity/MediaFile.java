package com.github.tvdtb.mediaresource.browser.entity;

public class MediaFile extends FileItem {
	
	long size;
	
	public MediaFile() {
		super("file");
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
