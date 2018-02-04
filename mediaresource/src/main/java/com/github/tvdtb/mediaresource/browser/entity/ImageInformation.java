package com.github.tvdtb.mediaresource.browser.entity;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;

@XmlRootElement
public class ImageInformation {
	static final int VERSION = 1;
	@JsonIgnore // ignored by Jackson for REST, but not for local Cache/Persistence
	int version = 0;
	private String name;
	private String path;
	private int width;
	private int height;
	private long lastmodified;
	private int orientation;
	private String dominantColor;
	private boolean exif;
	private int thumbnailSize;
	private Date mediaDate;

	public ImageInformation() {
	}

	public ImageInformation(String name, String path) {
		version = VERSION;
		this.name = name;
		this.path = path;
	}

	@JsonIgnore
	public boolean isCurrent() {
		return version == VERSION;
	}

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

	public long getLastmodified() {
		return lastmodified;
	}

	public void setLastmodified(long lastmodified) {
		this.lastmodified = lastmodified;
	}

	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public String getDominantColor() {
		return dominantColor;
	}

	public void setDominantColor(String dominantColor) {
		this.dominantColor = dominantColor;
	}

	public int getThumbnailSize() {
		return thumbnailSize;
	}

	public void setThumbnailSize(int thumbnailSize) {
		this.thumbnailSize = thumbnailSize;
	}

	public boolean isExif() {
		return exif;
	}

	public void setExif(boolean exif) {
		this.exif = exif;
	}

	@Override
	public String toString() {
		return "ImageInformation [width=" + width + ", height=" + height + ", lastmodified=" + lastmodified
				+ ", orientation=" + orientation + ", dominantColor=" + dominantColor + ", exif=" + exif
				+ ", thumbnailSize=" + thumbnailSize + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getMediaDate() {
		return mediaDate;
	}

	public void setMediaDate(Date mediaDate) {
		this.mediaDate = mediaDate;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
