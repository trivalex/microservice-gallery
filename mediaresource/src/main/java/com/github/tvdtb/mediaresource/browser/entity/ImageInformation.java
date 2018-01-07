package com.github.tvdtb.mediaresource.browser.entity;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.github.tvdtb.mediaresource.rest.HateoasEntity;

@XmlRootElement
public class ImageInformation extends HateoasEntity {
	private String name;
	private int width;
	private int height;
	private long lastmodified;
	private int orientation;
	private String dominantColor;
	private boolean exif;
	private int thumbnailSize;
	private Date mediaDate;

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

}
