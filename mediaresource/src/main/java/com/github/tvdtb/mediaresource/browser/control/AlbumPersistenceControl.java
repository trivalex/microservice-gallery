package com.github.tvdtb.mediaresource.browser.control;

import java.util.List;

import com.github.tvdtb.mediaresource.browser.entity.Album;

/**
 * Persistence Interface for Album entities (pl.: Alba)
 * 
 */
public interface AlbumPersistenceControl {

	List<Album> getAlba();

	Album getAlbum(String name);

}
