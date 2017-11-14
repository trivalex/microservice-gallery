package com.github.tvdtb.mediaresource.browser.boundary;

public enum ImageSize {

	/**
	 * absolute mini icon size
	 */
	MINI(10, 10),
	/**
	 * Icon size is displayed everywhere but its size is not guaranteed. It might be
	 * the original exif thumbnail and therefore larger or even smaller
	 */
	ICON(180, 180),
	/**
	 * the preview size is displayed in the gallery
	 */
	PREVIEW(1440, 1080),
	/**
	 * For zooming, the original image
	 */
	ORIGINAL(-1, -1);

	private int width;
	private int height;

	private ImageSize(int w, int h) {
		this.width = w;
		this.height = h;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
