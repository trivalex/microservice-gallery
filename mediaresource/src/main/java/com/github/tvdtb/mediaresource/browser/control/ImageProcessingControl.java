package com.github.tvdtb.mediaresource.browser.control;

import com.github.tvdtb.mediaresource.browser.boundary.ImageSize;
import com.github.tvdtb.mediaresource.browser.control.io.StreamDto;
import com.github.tvdtb.mediaresource.browser.entity.ImageInformation;

/**
 * handle image-specific funtions like scaling and reading information from
 * images
 * 
 */
public interface ImageProcessingControl {
	public ImageInformation readImageInformation(StreamDto source);

	public ScalingDTO scale(StreamDto source, ImageInformation imageInfo, ImageSize requiredSize);

	public static class ScalingDTO {
		public ImageSize size;
		public StreamDto result;
	}

}
