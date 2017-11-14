package com.github.tvdtb.mediaresource.browser.control;

import java.util.List;

import com.github.tvdtb.mediaresource.browser.control.io.StreamDto;
import com.github.tvdtb.mediaresource.browser.entity.Album;
import com.github.tvdtb.mediaresource.browser.entity.FileItem;
import com.github.tvdtb.mediaresource.browser.entity.FolderInformation;
import com.github.tvdtb.mediaresource.browser.entity.ImageInformation;

/**
 * interface for reading images and Folders (if such exists)
 *
 */
public interface ImagePersistenceControl {

	List<FileItem> readPath(Album album, String path);

	StreamDto findCached(Album album, String path, String imageName, String qualifier);

	StreamDto find(Album album, String path);

	StreamDto find(Album album, String path, String imageName);

	void writeCache(Album album, String path, String imageName, String qualifier, StreamDto streamResult);

	StreamDto getUnknown();

	ImageInformation readImageInfo(Album album, String path);

	ImageInformation readImageInfo(Album album, String path, String imageName);

	void writeImageInfo(Album album, String path, String imageName, ImageInformation newImageInfo);

	void writeImageInfo(Album album, String path, ImageInformation result);

	void writeFolderInfo(Album album, String path, FolderInformation result);

	FolderInformation readFolderInfo(Album album, String path);

}
