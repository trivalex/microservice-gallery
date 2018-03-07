package com.github.tvdtb.mediaresource.browser.control.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.imaging.util.IoUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.github.tvdtb.mediaresource.MediaResource;
import com.github.tvdtb.mediaresource.browser.control.ImagePersistenceControl;
import com.github.tvdtb.mediaresource.browser.control.MediaTypeControl;
import com.github.tvdtb.mediaresource.browser.control.io.StreamDto;
import com.github.tvdtb.mediaresource.browser.entity.Album;
import com.github.tvdtb.mediaresource.browser.entity.FileItem;
import com.github.tvdtb.mediaresource.browser.entity.Folder;
import com.github.tvdtb.mediaresource.browser.entity.FolderInformation;
import com.github.tvdtb.mediaresource.browser.entity.ImageInformation;
import com.github.tvdtb.mediaresource.browser.entity.MediaFile;
import com.github.tvdtb.mediaresource.browser.entity.PictureFile;
import com.github.tvdtb.mediaresource.config.control.ConfigControl;

/**
 * Implementation based on Filesystem
 * 
 */
@Component
public class ImagePersistenceControlFilesystemImpl implements ImagePersistenceControl {

	private static final String LOG_EXCEPTION = "Exception: {}";

	@Inject
	Logger logger;

	@Inject
	MediaTypeControl mediaType;

	@Inject
	ConfigControl config;

	private String getCacheDirPattern() {
		return config.getConfig().getAlbum().getCacheDirPattern();
	}

	private String getCacheDirFilter() {
		return config.getConfig().getAlbum().getDirFilter();
	}

	public ImageInformation readImageInfo(Album album, String path) {
		String[] split = splitPath(path);
		return readImageInfo(album, split[0], split[1]);
	}

	String[] splitPath(String path) {
		int index = path.lastIndexOf('/');
		if (index < 0)
			return new String[] { "/", path };
		else
			return new String[] { path.substring(0, index), path.substring(index + 1) };
	}

	public ImageInformation readImageInfo(Album album, String path, String imageName) {
		try {
			ImageInformation result = null;
			String qualifier = "info";
			String newExt = "json";
			File cacheFile = _findCacheFile(album, path, imageName, qualifier, newExt);
			if (cacheFile.exists()) {
				try (FileInputStream fis = new FileInputStream(cacheFile)) {
					StringBuffer sb = new StringBuffer();
					BufferedReader r = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
					char[] chars = new char[1024];
					int count = 0;
					while ((count = r.read(chars)) >= 0) {
						sb.append(chars, 0, count);
					}

					result = JSON.parseObject(sb.toString(), ImageInformation.class);
					return result;
				}
			}
			return result;
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);

			throw MediaResource.handleException(e);
		}
	}

	@Override
	public void writeImageInfo(Album album, String path, ImageInformation newImageInfo) {
		String[] split = splitPath(path);
		writeImageInfo(album, split[0], split[1], newImageInfo);
	}

	@Override
	public void writeImageInfo(Album album, String path, String imageName, ImageInformation newImageInfo) {
		try {
			String qualifier = "info";
			String newExt = "json";

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (OutputStreamWriter out = new OutputStreamWriter(baos, "UTF-8")) {
				JSON.writeJSONString(out, newImageInfo);
			}
			writeCacheFile(album, path, imageName, qualifier, newExt, new ByteArrayInputStream(baos.toByteArray()));
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);

			throw MediaResource.handleException(e);
		}
	}

	@Override
	public void writeFolderInfo(Album album, String path, FolderInformation result) {
		try {
			String qualifier = "info";
			String newExt = "json";

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (OutputStreamWriter out = new OutputStreamWriter(baos, "UTF-8")) {
				JSON.writeJSONString(out, result);
			}
			writeCacheFile(album, path, ".folder", qualifier, newExt, new ByteArrayInputStream(baos.toByteArray()));
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);

			throw MediaResource.handleException(e);
		}
	}

	@Override
	public FolderInformation readFolderInfo(Album album, String path) {
		try {
			FolderInformation result = null;
			String qualifier = "info";
			String newExt = "json";
			File cacheFile = _findCacheFile(album, path, ".folder", qualifier, newExt);
			if (cacheFile.exists()) {
				long lastmodifiedCacheFile = cacheFile.lastModified();
				File dir = new File(album.getPath(), path);
				boolean exists = dir.exists();
				boolean cacheOutdated = dir.lastModified() > lastmodifiedCacheFile;
				if (!exists || (exists && cacheOutdated))
					result = null;
				else {
					try (FileInputStream fis = new FileInputStream(cacheFile)) {
						StringBuffer sb = new StringBuffer();
						BufferedReader r = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
						char[] chars = new char[1024];
						int count = 0;
						while ((count = r.read(chars)) >= 0) {
							sb.append(chars, 0, count);
						}

						result = JSON.parseObject(sb.toString(), FolderInformation.class);
						return result;
					}
				}

			}
			return result;
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);

			throw MediaResource.handleException(e);
		}
	}

	@Override
	public List<FileItem> readPath(Album album, String path) {
		File dir2Read = new File(album.getPath(), path);
		if (!dir2Read.isDirectory())
			throw new IllegalArgumentException("not a folder " + dir2Read.getPath());

		return Arrays.stream(dir2Read.listFiles()) //
				.sorted((a, b) -> compare(a, b)) //
				.filter(f -> !f.isHidden() && f.canRead()) //
				.filter(f -> (f.isDirectory() && !f.getName().matches(getCacheDirFilter()))
						|| (f.isFile() && isMediaFile(f))) //
				.map(file -> {
					FileItem result = null;
					if (file.isDirectory()) {
						Folder folder = new Folder();
						folder.setName(file.getName());
						folder.setPath(concatPath(path, file.getName()));
						result = folder;
					} else {
						MediaFile media = null;
						if (isPicture(file)) {
							media = new PictureFile();
						} else
							media = new MediaFile();

						media.setName(file.getName());
						media.setPath(concatPath(path, file.getName()));
						media.setSize(file.length());

						result = media;
					}
					return result;
				})//
				.collect(Collectors.toList());
	}

	private String concatPath(String path, String name) {
		if (path == null || path.length() == 0)
			return name;
		else if (path.endsWith("/") || name.startsWith("/"))
			return path + name;
		else
			return path + "/" + name;
	}

	private boolean isMediaFile(File f) {
		return f.getName().toUpperCase().endsWith(".JPG");
	}

	private boolean isPicture(File f) {
		return f.getName().toUpperCase().endsWith(".JPG");
	}

	public static int compare(File a, File b) {
		if (a.isDirectory() != b.isDirectory()) {
			if (a.isDirectory())
				return -1;
			else
				return 1;
		}
		return a.getName().toUpperCase().compareTo(b.getName().toUpperCase());
	}

	public boolean exists(String path) {
		return new File(path).exists();
	}

	public boolean exists(String path, String path2) {
		return new File(path, path2).exists();
	}

	@Override
	public StreamDto find(Album album, String path, String imageName) {
		File file = new File(album.getPath(), path + "/" + imageName);
		return StreamDto.fromFile(file, path, mediaType.readMediaTypeFrom(file));
	}

	@Override
	public StreamDto findCached(Album album, String path, String imageName, String qualifier) {
		File f = _findCacheFile(album, path, imageName, qualifier, null);
		if (f.exists())
			return StreamDto.fromFile(f, path, mediaType.readMediaTypeFrom(f));
		else
			return null;
	}

	public File _findCacheFile(Album album, String path, String fileName, String qualifier, String newExtension) {
		String extension = "";
		int index = fileName.lastIndexOf('.');
		if (index > 0) {
			extension = fileName.substring(index + 1);
			fileName = fileName.substring(0, index);
		}

		String filePath = MessageFormat.format(getCacheDirPattern() //
				, album.getName(), album.getPath(), path, fileName, qualifier,
				(newExtension == null ? extension : newExtension));
		return new File(filePath);
	}

	public String getType(File mediaFile) {
		String fileName = mediaFile.getName();
		int index = fileName.lastIndexOf('.');
		if (index > 0) {
			String extension = fileName.substring(index + 1).toUpperCase();
			if ("JPG".equals(extension)) {
				return "image/jpg";
			} else if ("PNG".equals(extension)) {
				return "image/png";
			}
		}
		return "application/octet-stream";
	}

	public void writeCacheFile(Album album, String path, String fileName, String qualifier, InputStream stream) {
		writeCacheFile(album, path, fileName, qualifier, null, stream);
	}

	public void writeCacheFile(Album album, String path, String fileName, String qualifier, String newExtension,
			InputStream stream) {

		try {
			File cacheFile = _findCacheFile(album, path, fileName, qualifier, newExtension);
			File cacheDir = cacheFile.getParentFile();
			synchronized (ImagePersistenceControlFilesystemImpl.class) {
				if (!cacheDir.exists()) {
					if (!cacheDir.mkdirs()) {
						throw new IllegalArgumentException("unable to create " + cacheDir.getPath());
					}
					// TODO make hidden on Windows
				}
			}

			try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
				byte[] buffer = new byte[8 * 1024];
				for (int bytes = stream.read(buffer); bytes >= 0; bytes = stream.read(buffer)) {
					fos.write(buffer, 0, bytes);
				}
			} finally {
				stream.close();
			}
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);

			throw MediaResource.handleException(e);
		}
	}

	@Override
	public void writeCache(Album album, String path, String imageName, String qualifier, StreamDto stream) {
		writeCacheFile(album, path, imageName, qualifier, stream.getContent());

	}

	@Override
	public StreamDto getUnknown() {
		try {
			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/unknown.png");
			byte[] buffer = new byte[500];
			int bytes = in.read(buffer, 0, buffer.length);

			String type = mediaType.readMediaTypeFrom(new ByteArrayInputStream(buffer, 0, bytes));
			StreamDto result = StreamDto.fromBytes("unknown.png", "unknown", buffer, type, -1L);
			return result;
		} catch (IOException e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);

			throw MediaResource.handleException(e);
		}
	}

	@Override
	public StreamDto find(Album album, String path) {
		String[] split = splitPath(path);
		return find(album, split[0], split[1]);
	}

	@Override
	public void writeImage(Album album, String targetPath, StreamDto streamDto) {
		try {
			File file = new File(album.getPath(), targetPath + "/" + streamDto.getName());
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				try (FileOutputStream fos = new FileOutputStream(file)) {
					IoUtils.copyStreamToStream(streamDto.getContent(), fos);
				}
			}
		} catch (Exception e) {
			MediaResource.handleException(e);
		}
	}

}
