package com.github.tvdtb.mediaresource.browser.boundary;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import org.springframework.stereotype.Component;

import com.github.tvdtb.mediaresource.browser.control.AlbumPersistenceControl;
import com.github.tvdtb.mediaresource.browser.control.ImagePersistenceControl;
import com.github.tvdtb.mediaresource.browser.control.ImageProcessingControl;
import com.github.tvdtb.mediaresource.browser.control.ImageProcessingControl.ScalingDTO;
import com.github.tvdtb.mediaresource.browser.control.io.StreamDto;
import com.github.tvdtb.mediaresource.browser.entity.Album;
import com.github.tvdtb.mediaresource.browser.entity.FileItem;
import com.github.tvdtb.mediaresource.browser.entity.Folder;
import com.github.tvdtb.mediaresource.browser.entity.FolderInformation;
import com.github.tvdtb.mediaresource.browser.entity.ImageInformation;
import com.github.tvdtb.mediaresource.browser.entity.PictureFile;

@Component
public class BrowserBoundary {

	@Inject
	ImagePersistenceControl imagePersistence;

	@Inject
	AlbumPersistenceControl albumPersistence;

	@Inject
	ImageProcessingControl imageProcessing;

	public List<Album> readAlba() {
		// simple delegation due to multiple implementations
		return albumPersistence.getAlba();
	}

	public Album readAlbum(String name) {
		// simple delegation due to multiple implementations
		return albumPersistence.getAlbum(name);
	}

	public FolderInformation readFolder(Album album, final String path) {

		FolderInformation result = imagePersistence.readFolderInfo(album, path);

		if (result == null || !result.isCurrent()) {

			List<FileItem> fileItems = imagePersistence.readPath(album, path);
			List<FolderInformation> folders = fileItems.stream()//
					.filter(fi -> fi instanceof Folder)//
					.map(fi -> {
						Folder folder = (Folder) fi;
						FolderInformation folderInfo = new FolderInformation(folder.getName(), folder.getPath());
						return folderInfo;
					})//
					.collect(Collectors.toList());

			List<ImageInformation> images = fileItems.stream().filter(fi -> fi instanceof PictureFile)//
					.map(fi -> {
						PictureFile pf = (PictureFile) fi;

						ImageInformation imageInformation = readImageInformation(album, pf.getPath());
						imageInformation.setName(pf.getName());
						return imageInformation;
					})//
					.collect(Collectors.toList());

			String name = path;
			int idx = name.lastIndexOf("/");
			if (idx >= 0) {
				name = path.substring(idx + 1);
			}

			result = new FolderInformation(name, path);
			result.setFolders(folders);
			result.setImages(images);

			imagePersistence.writeFolderInfo(album, path, result);
		}

		return result;
	}

	public boolean isAbsolute(String path) {
		return path != null && path.startsWith("/");
	}

	public StreamDto readImage(Album album, String path, String imageName//
			, ImageSize desiredSize) throws IOException {

		// try to find the required data in cache
		StreamDto cachedResult = imagePersistence.findCached(album, path, imageName, desiredSize.name());
		if (cachedResult != null) {
			return cachedResult;
		}

		// find resource itself
		StreamDto original = imagePersistence.find(album, path, imageName);
		if (original == null)
			throw new NotFoundException(path + " " + imageName);

		ImageInformation imageInfo = imagePersistence.readImageInfo(album, path, imageName);

		// if found, scale it and save data to cache
		ScalingDTO scaled = imageProcessing.scale(original, imageInfo, desiredSize);
		StreamDto streamResult = scaled.result;
		streamResult.mark();
		try {
			imagePersistence.writeCache(album, path, imageName, desiredSize.name(), streamResult);
		} finally {
			streamResult.reset();
		}

		return streamResult;
	}

	public ImageInformation readImageInformation(Album album, String path) {
		ImageInformation result = imagePersistence.readImageInfo(album, path);
		if (result == null) {
			StreamDto source = imagePersistence.find(album, path);
			result = imageProcessing.readImageInformation(source);
			imagePersistence.writeImageInfo(album, path, result);
		}
		return result;
	}

	public String writeImage(String albumName, StreamDto streamDto) {
		ImageInformation imageInformation = imageProcessing.readImageInformation(streamDto);

		Date mediaDate = imageInformation.getMediaDate();
		if (mediaDate != null && mediaDate.getTime() > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'yyyyMMdd");
			String targetPath = sdf.format(mediaDate);

			Album album = albumPersistence.getAlbum(albumName);
			imagePersistence.writeImage(album, targetPath, streamDto);
			return targetPath;
		} else
			return null;
	}

}
