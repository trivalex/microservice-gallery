package com.github.tvdtb.mediaresource.browser.control.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.tvdtb.mediaresource.browser.control.MediaTypeControl;
import com.github.tvdtb.mediaresource.browser.control.impl.ImagePersistenceControlFilesystemImpl;

public class ImagePersistenceControlFilesystemImplTest {

	private static ImagePersistenceControlFilesystemImpl control;

	@BeforeClass
	public static void init() {
		control = new ImagePersistenceControlFilesystemImpl();
		control.mediaType = new MediaTypeControl();
	}

	@Test
	public void testUnknownIcon() {
		assertThat(control.getUnknown(), notNullValue());
	}
}
