package com.github.tvdtb.mediaresource.browser.boundary;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import org.springframework.stereotype.Component;

import com.github.tvdtb.mediaresource.browser.control.AlbumPersistenceControl;
import com.github.tvdtb.mediaresource.browser.control.ImagePersistenceControl;
import com.github.tvdtb.mediaresource.browser.control.ImageProcessingControl;
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

	public FolderInformation readFolder(Album album, final String path, boolean parent) {

		FolderInformation result = imagePersistence.readFolderInfo(album, path);

		if (result == null) {

			List<FileItem> fileItems = imagePersistence.readPath(album, path);
			List<FolderInformation> folders = fileItems.stream()//
					.filter(fi -> fi instanceof Folder)//
					.map(fi -> {
						Folder folder = (Folder) fi;
						FolderInformation folderInfo = new FolderInformation();
						folderInfo.setName(folder.getName());
						return folderInfo;
					})//
					.collect(Collectors.toList());

			List<ImageInformation> images = fileItems.stream().filter(fi -> fi instanceof PictureFile)//
					.map(fi -> {
						PictureFile pf = (PictureFile) fi;

						ImageInformation imageInformation = readImageInformation(album, pf.getPath());
						imageInformation.setName(pf.getName());
						return imageInformation;
					}).collect(Collectors.toList());

			result = new FolderInformation();
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
		StreamDto result = imagePersistence.findCached(album, path, imageName, desiredSize.name());
		if (result != null) {
			return result;
		}

		// find resource itself
		result = imagePersistence.find(album, path, imageName);
		if (result == null)
			throw new NotFoundException(path + " " + imageName);

		AtomicReference<ImageInformation> imageInfo = new AtomicReference<>(
				imagePersistence.readImageInfo(album, path, imageName));
		AtomicReference<StreamDto> reference = new AtomicReference<StreamDto>(null);

		// if found, scale it and save data to cache
		imageProcessing.scale(result, imageInfo.get(), desiredSize, (size, newImageInfo, streamResult) -> {
			// this is our result
			if (size.equals(desiredSize))
				reference.set(streamResult);
			// write Image Info if it was changed/created
			if (!newImageInfo.equals(imageInfo.get())) {
				imagePersistence.writeImageInfo(album, path, imageName, newImageInfo);
				imageInfo.set(newImageInfo);
			}
			// write scaled image to cache
			streamResult.mark();
			try {
				imagePersistence.writeCache(album, path, imageName, desiredSize.name(), streamResult);
			} finally {
				streamResult.reset();
			}
		});

		// at least some image - this will not be cached
		if ((result = reference.get()) == null)
			result = imagePersistence.getUnknown();

		return result;
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

}
