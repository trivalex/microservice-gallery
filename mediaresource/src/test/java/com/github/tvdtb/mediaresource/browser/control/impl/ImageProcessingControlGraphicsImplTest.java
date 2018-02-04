package com.github.tvdtb.mediaresource.browser.control.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.github.tvdtb.mediaresource.browser.boundary.ImageSize;
import com.github.tvdtb.mediaresource.browser.control.ImageProcessingControl.ScalingDTO;
import com.github.tvdtb.mediaresource.browser.control.MediaTypeControl;
import com.github.tvdtb.mediaresource.browser.control.io.StreamDto;
import com.github.tvdtb.mediaresource.browser.entity.ImageInformation;

public class ImageProcessingControlGraphicsImplTest {

	private static ImageProcessingControlGraphicsImpl control;

	@BeforeClass
	public static void init() {
		control = new ImageProcessingControlGraphicsImpl();
		control.logger = LoggerFactory.getLogger(ImageProcessingControlGraphicsImpl.class.getName());
		control.mediaType = new MediaTypeControl();
	}

	@Test
	public void testScalingIconSize() throws Exception {
		StreamDto result = _scale("OnePlus3T.jpg", ImageSize.ICON);
		BufferedImage scaledImage = ImageIO.read(result.getContent());

		// for icon size we allow exif icon
		assertThat(scaledImage.getWidth(), equalTo(180));
		assertThat(scaledImage.getHeight(), equalTo(101)); // aspect ratio is maintained
	}

	@Test
	public void testScalingCustomSize() throws Exception {
		String sizeName = "mySize";
		Map<String, int[]> allSizes = new HashMap<>();
		allSizes.put(sizeName, new int[] { 10, 10 });

		StreamDto result = _scale("OnePlus3T.jpg", ImageSize.MINI);
		BufferedImage scaledImage = ImageIO.read(result.getContent());

		// for custom sizes we expect exact size with aspect ratio
		assertThat(scaledImage.getWidth(), equalTo(10));
		assertThat(scaledImage.getHeight(), equalTo(5)); // aspect ratio is maintained
	}

	private StreamDto _scale(String imageName, ImageSize desiredSize) throws Exception {
		ImageInformation imageInfo = readImageInfo(imageName);
		StreamDto source = createStreamResult(imageName);

		ScalingDTO sdto = control.scale(source, imageInfo, desiredSize);
		return sdto.result;
	}

	@Test
	public void testImageInformationOneplus3T_1() throws Exception {
		ImageInformation imageInfo = readImageInfo("OnePlus3T.jpg");
		assertThat(imageInfo.getWidth(), equalTo(1024));
		assertThat(imageInfo.getHeight(), equalTo(576));
		assertThat(imageInfo.isExif(), equalTo(true));
		assertThat(imageInfo.getOrientation(), equalTo(-1));
		assertThat(imageInfo.getDominantColor(), equalTo("#657373"));
	}

	@Test
	public void testImageInformationSonyRX100() throws Exception {
		ImageInformation imageInfo = readImageInfo("Sony-RX100.JPG");
		assertThat(imageInfo.getWidth(), equalTo(1024));
		assertThat(imageInfo.getHeight(), equalTo(683));
		assertThat(imageInfo.isExif(), equalTo(true));
		assertThat(imageInfo.getOrientation(), equalTo(1));
		assertThat(imageInfo.getDominantColor(), equalTo("#657064"));
	}

	public void testImageInformationSimple() throws Exception {
		ImageInformation imageInfo = readImageInfo("red.jpg");
		assertThat(imageInfo.getWidth(), equalTo(100));
		assertThat(imageInfo.getHeight(), equalTo(100));
		assertThat(imageInfo.isExif(), equalTo(false));
		assertThat(imageInfo.getDominantColor(), nullValue());
	}

	private ImageInformation readImageInfo(String imageName) throws Exception {
		StreamDto source = createStreamResult(imageName);
		ImageInformation info = control.readImageInformation(source);
		System.out.println(imageName + " -> " + info);
		return info;
	}

	private StreamDto createStreamResult(String imageName) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(imageName);
		File f = new File(resource.getPath());
		StreamDto source = StreamDto.fromFile(f, "test", "image/jpg");
		return source;
	}
}
