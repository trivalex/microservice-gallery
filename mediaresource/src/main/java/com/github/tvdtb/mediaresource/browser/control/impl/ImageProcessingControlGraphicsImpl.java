package com.github.tvdtb.mediaresource.browser.control.impl;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.AllTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryType;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoLong;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoShort;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.github.tvdtb.mediaresource.MediaResource;
import com.github.tvdtb.mediaresource.browser.boundary.ImageSize;
import com.github.tvdtb.mediaresource.browser.control.ImageProcessingControl;
import com.github.tvdtb.mediaresource.browser.control.MediaTypeControl;
import com.github.tvdtb.mediaresource.browser.control.io.StreamDto;
import com.github.tvdtb.mediaresource.browser.entity.ImageInformation;

@Component
public class ImageProcessingControlGraphicsImpl implements ImageProcessingControl {

	private static final String LOG_EXCEPTION = "Exception: {}";
	private static final int METADATA_BUFFER = 0;
	@Inject
	MediaTypeControl mediaType;

	@Inject
	Logger logger;

	@Override
	public void scale(StreamDto source, ImageInformation imageInfo, ImageSize requiredSize,
			ScalingResultConsumer scalingResultConsumer) {

		try {
			AtomicReference<StreamDto> ref = new AtomicReference<StreamDto>(null);
			// check if Image info is available - if not, read it
			if (imageInfo == null) {
				source.mark(METADATA_BUFFER);
				try {
					imageInfo = readImageInformation(source, (size, info, icon) -> {
						ref.set(icon);
					});
				} finally {
					source.reset();
				}
			}

			if (ImageSize.ICON.equals(requiredSize)) {
				if (ref.get() == null && imageInfo.getThumbnailSize() > 0) {
					StreamDto thumb = extractExifThumbnail(source);
					// some cameras create thumbnails with borders - or just illegal ones or without
					// correct rotation
					thumb = correctThumbnail(source, thumb, imageInfo);
					ref.set(thumb);
				}

				if (ref.get() != null) {
					scalingResultConsumer.accept(requiredSize, imageInfo, ref.get());
					return;
				}
			}

			StreamDto result = scaleAndRotate(source, imageInfo.getOrientation(), requiredSize);
			scalingResultConsumer.accept(requiredSize, imageInfo, result);
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);

			MediaResource.handleException(e);
		}
	}

	private StreamDto correctThumbnail(StreamDto source, StreamDto thumb, ImageInformation imageInfo) {
		try {
			ImageInfo thumbInfo = null;
			thumb.mark(METADATA_BUFFER);
			try {
				thumbInfo = Imaging.getImageInfo(thumb.getContent(), "image/jpeg");
			} finally {
				thumb.reset();
			}

			ImageSize expectedSize = null;
			double aspectRatio = ((double) imageInfo.getWidth()) / ((double) imageInfo.getHeight());
			int expectedThumbWidth = (int) (thumbInfo.getHeight() * aspectRatio);
			// if the expected sizes don't match we mark the real size which will cause real
			// scaling
			if (Math.abs(expectedThumbWidth - thumbInfo.getWidth()) >= 2) {
				System.out.println("need to scale " + imageInfo.getName() + " because of illegal height: "
						+ thumbInfo.getWidth() + " instead of " + expectedThumbWidth);
				expectedSize = ImageSize.ICON;
			}

			// if scaling is required or orientation isn't default we need to handle the
			// image by scaling
			if (expectedSize != null || imageInfo.getOrientation() != 0) {
				source.mark(-1);
				try {
					thumb = scaleAndRotate(source, imageInfo.getOrientation(), ImageSize.ICON);
				} finally {
					source.reset();
				}
			}

			return thumb;
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);
			throw MediaResource.handleException(e);
		}

	}

	private StreamDto extractExifThumbnail(StreamDto source) {
		try {
			IImageMetadata metadata = null;
			source.mark(METADATA_BUFFER);
			try {
				metadata = Imaging.getMetadata(source.getContent(), "image/jpeg");
			} finally {
				source.reset();
			}
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			if (null != jpegMetadata) {
				byte[] thumbnail = jpegMetadata.getEXIFThumbnailData();
				if (thumbnail != null) {
					String type = mediaType.readMediaTypeFrom(new ByteArrayInputStream(thumbnail));
					return StreamDto.fromBytes(thumbnail, type, -1L);
				}
			}
			return null;
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);

			throw MediaResource.handleException(e);
		}
	}

	private StreamDto scaleAndRotate(StreamDto source, int orientation, ImageSize size) {
		byte[] imageBytes = scaleAndRotate(source.getContent(), orientation, size);
		String type = mediaType.readMediaTypeFrom(imageBytes[0], imageBytes[1]);
		StreamDto stream = StreamDto.fromBytes(imageBytes, type, -1L);
		return stream;
	}

	private byte[] scaleAndRotate(InputStream in, int orientation, ImageSize size) {

		try {
			BufferedImage image = ImageIO.read(in);
			int rotate = 0;
			if (orientation == 6)
				rotate = 90;
			if (orientation == 8)
				rotate = -90;

			int width = image.getWidth();
			int height = image.getHeight();

			int maxWidth = size == null ? width : size.getWidth();
			int maxHeight = size == null ? height : size.getHeight();

			double widthRotated = rotate == 0 ? width : height;
			double heightRotated = rotate == 0 ? height : width;

			double scale = Math.min(maxWidth / ((double) width), maxHeight / ((double) height));
			if (scale > 1)
				scale = 1;
			BufferedImage scaled = new BufferedImage((int) (widthRotated * scale), (int) (heightRotated * scale),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics2d = (Graphics2D) scaled.createGraphics();
			AffineTransform afScale = AffineTransform.getScaleInstance(scale, scale);
			if (rotate != 0) {
				AffineTransform afTranslate = AffineTransform.getTranslateInstance(-width / 2.0d, -height / 2.0d);
				AffineTransform afRotate = AffineTransform.getRotateInstance(rotate * Math.PI / 180.0d);
				AffineTransform afTranslate2 = AffineTransform.getTranslateInstance(scale * widthRotated / 2.0d,
						scale * heightRotated / 2.0d);

				afScale.concatenate(afTranslate);
				afRotate.concatenate(afScale);
				afTranslate2.concatenate(afRotate);
				graphics2d.setTransform(afTranslate2);
			} else {
				graphics2d.setTransform(afScale);
			}
			graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			graphics2d.drawImage(image, 0, 0, width, height, (ImageObserver) null);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(scaled, "jpg", baos);
			image.flush();
			scaled.flush();

			byte[] imageBytes = baos.toByteArray();
			return imageBytes;
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);

			throw MediaResource.handleException(e);
		}

	}

	public ImageInformation readImageInformation(StreamDto source) {
		return readImageInformation(source, null);
	}

	public ImageInformation readImageInformation(StreamDto source, ScalingResultConsumer consumer) {
		ImageInformation result = new ImageInformation();
		result.setLastmodified(source.getDate());

		StreamDto thumbStream = null;

		BufferedImage image = null;
		try {
			IImageMetadata metadata = null;
			source.mark(METADATA_BUFFER);
			try {
				metadata = Imaging.getMetadata(source.getContent(), "image/jpeg");
			} finally {
				source.reset();
			}
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			if (null != jpegMetadata) {
				result.setExif(true);
				byte[] thumbnail = jpegMetadata.getEXIFThumbnailData();
				if (thumbnail != null) {
					String type = mediaType.readMediaTypeFrom(new ByteArrayInputStream(thumbnail));
					thumbStream = StreamDto.fromBytes(thumbnail, type, -1L);
					result.setThumbnailSize(thumbnail.length);
					image = ImageIO.read(new ByteArrayInputStream(thumbnail));
				}
				image = jpegMetadata.getEXIFThumbnail();

				// Exif is the only source for orientation
				result.setOrientation(getOrientation(jpegMetadata));
				TiffImageMetadata exif = jpegMetadata.getExif();

				Number width = (Number) exif.getFieldValue(AllTagConstants.TIFF_TAG_IMAGE_WIDTH);
				Number height = (Number) exif.getFieldValue(AllTagConstants.TIFF_TAG_IMAGE_LENGTH);
				if (width != null && height != null) {
					result.setWidth(width.intValue());
					result.setHeight(height.intValue());
				} else {
					int[] wShort = exif.getFieldValue(
							new TagInfoLong("ExifImageWidth", 0xa002, 1, TiffDirectoryType.TIFF_DIRECTORY_IFD0));
					int[] hShort = exif.getFieldValue(
							new TagInfoLong("ExifImageHeight", 0xa003, 1, TiffDirectoryType.TIFF_DIRECTORY_IFD0));
					if (wShort != null && hShort != null) {
						result.setWidth(wShort[0]);
						result.setHeight(hShort[0]);
					}
				}
			}

		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);

			throw MediaResource.handleException(e);
		}

		if (result.getHeight() == 0) {
			source.mark(METADATA_BUFFER);
			try {
				ImageInfo imageInfo = Imaging.getImageInfo(source.getContent(), "image/jpeg");
				result.setWidth(imageInfo.getWidth());
				result.setHeight(imageInfo.getHeight());
			} catch (Exception e) {
				if (logger.isErrorEnabled())
					logger.error(LOG_EXCEPTION, e);

				throw MediaResource.handleException(e);
			} finally {
				source.reset();
			}
		}

		// the image found here is sufficiently small - just calculate the dominant
		// color
		if (image != null) {
			String hex = calculateDominantColor(image);
			result.setDominantColor(hex);
		}
		if (consumer != null && thumbStream != null) {
			thumbStream = correctThumbnail(source, thumbStream, result);
			consumer.accept(ImageSize.ICON, result, thumbStream);
		}
		return result;
	}

	private String calculateDominantColor(BufferedImage image) {
		int count = 10;
		int dx = image.getWidth() / (count + 1);
		int dy = image.getHeight() / (count + 1);

		int[] rgbArray = new int[1];
		double r = 0;
		double g = 0;
		double b = 0;
		for (int x = 0; x < count; x++) {
			for (int y = 0; y < count; y++) {
				image.getRGB(dx / 2 + x * dx, dy / 2 + dy * y, 1, 1, rgbArray, 0, 1);
				r += (0x00FF & (rgbArray[0] >> 16));
				g += (0x00FF & (rgbArray[0] >> 8));
				b += (0x00FF & (rgbArray[0]));
			}
		}
		int countSquared = count * count;
		int averageR = (int) (r / countSquared);
		int averageG = (int) (g / countSquared);
		int averageB = (int) (b / countSquared);
		String hex = "#" + toHexString(averageR) + toHexString(averageG) + toHexString(averageB);
		return hex;
	}

	private int getOrientation(IImageMetadata metadata) {
		try {
			TiffImageMetadata tiffImageMetadata;

			if (metadata instanceof JpegImageMetadata) {
				tiffImageMetadata = ((JpegImageMetadata) metadata).getExif();
			} else if (metadata instanceof TiffImageMetadata) {
				tiffImageMetadata = (TiffImageMetadata) metadata;
			} else {
				return -1;
			}

			TiffField field = tiffImageMetadata.findField(TiffTagConstants.TIFF_TAG_ORIENTATION);
			if (field != null) {
				return field.getIntValue();
			} else {
				TagInfo tagInfo = new TagInfoShort("Orientation", 274, 1, TiffDirectoryType.TIFF_DIRECTORY_IFD0); // MAGIC_NUMBER
				field = tiffImageMetadata.findField(tagInfo);
				if (field != null) {
					return field.getIntValue();
				} else {
					return -1;
				}
			}
		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(LOG_EXCEPTION, e);
			return -1;
		}
	}

	private String toHexString(int value) {
		String result = Integer.toHexString(value);
		if (result.length() == 1)
			result = "0" + result;
		return result;
	}

}
