package com.github.tvdtb.mediaresource.browser.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.tvdtb.mediaresource.rest.HateoasEntity;
import com.github.tvdtb.mediaresource.rest.HateoasLink;

@JsonInclude(Include.NON_EMPTY)
public class FolderInformation extends HateoasEntity {

	private String name;

	private List<FolderInformation> folders;

	private List<HateoasLink> _icons;

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

	public List<HateoasLink> getIcons() {
		return _icons;
	}

	public void setIcons(List<HateoasLink> _icons) {
		this._icons = _icons;
	}

}
