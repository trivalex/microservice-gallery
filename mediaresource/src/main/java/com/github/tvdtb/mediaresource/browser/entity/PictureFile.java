package com.github.tvdtb.mediaresource.browser.entity;

public class PictureFile extends MediaFile {
	
	int width;
	int height;
	int orientation;
	
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getOrientation() {
		return orientation;
	}
	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

}
