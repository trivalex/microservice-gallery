package com.github.tvdtb.mediaresource.browser.entity;

import java.util.List;

import com.github.tvdtb.mediaresource.rest.HateoasDto;
import com.github.tvdtb.mediaresource.rest.HateoasEntity;

public class Alba extends HateoasEntity {
	String defaultAlbumName;
	List<HateoasDto<Album>> alba;

	public String getDefaultAlbumName() {
		return defaultAlbumName;
	}

	public void setDefaultAlbumName(String defaultAlbumName) {
		this.defaultAlbumName = defaultAlbumName;
	}

	public List<HateoasDto<Album>> getAlba() {
		return alba;
	}

	public void setAlba(List<HateoasDto<Album>> alba) {
		this.alba = alba;
	}

}
