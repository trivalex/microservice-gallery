package com.github.tvdtb.mediaresource.browser.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class FolderInformation {
	static final int VERSION = 3;

	private String name;
	private String path;
	@JsonIgnore // ignored by Jackson for REST, but not for local Cache/Persistence
	private int version;

	private List<FolderInformation> folders;

	int iconCount;

	public FolderInformation() {
		version = 0;
	}

	public FolderInformation(String name, String path) {
		version = VERSION;
		this.name = name;
		this.path = path;
	}

	@JsonIgnore
	public boolean isCurrent() {
		return version == VERSION;
	}

	private List<ImageInformation> images;

	public List<ImageInformation> getImages() {
		return images;
	}

	public void setImages(List<ImageInformation> images) {
		this.images = images;
	}

	public List<FolderInformation> getFolders() {
		return folders;
	}

	public void setFolders(List<FolderInformation> folders) {
		this.folders = folders;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIconCount() {
		return iconCount;
	}

	public void setIconCount(int iconCount) {
		this.iconCount = iconCount;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
