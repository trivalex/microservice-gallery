package com.github.tvdtb.mediaresource.config.model;

public class Configuration {
	SeurityConfig security;
	AlbumConfig album;

	public SeurityConfig getSecurity() {
		return security;
	}

	public void setSecurity(SeurityConfig security) {
		this.security = security;
	}

	public AlbumConfig getAlbum() {
		return album;
	}

	public void setAlbum(AlbumConfig album) {
		this.album = album;
	}

}
