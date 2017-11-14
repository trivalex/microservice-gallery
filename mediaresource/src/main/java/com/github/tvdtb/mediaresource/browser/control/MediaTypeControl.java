package com.github.tvdtb.mediaresource.browser.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.github.tvdtb.mediaresource.browser.control.io.StreamDto;

@Component
public class MediaTypeControl {

	public String readMediaTypeFrom(StreamDto sr) {
		sr.mark();
		try {
			return readMediaTypeFrom(sr.getContent());
		} finally {
			sr.reset();
		}
	}

	public String readMediaTypeFrom(File f) {
		try {
			try (FileInputStream fis = new FileInputStream(f)) {
				return readMediaTypeFrom(fis);
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String readMediaTypeFrom(InputStream fis) {
		try {
			byte[] bytes = new byte[2];
			int count = fis.read(bytes);
			if (count != 2)
				throw new IllegalArgumentException("stream does not contain magic bytes: " + count);
			return readMediaTypeFrom(bytes[0], bytes[1]);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public String readMediaTypeFrom(byte b1, byte b2) {
		if (b1 == ((byte) 0xFF) && b2 == ((byte) 0xD8))
			return "image/jpeg";
		else if (b1 == ((byte) 0x89) && b2 == ((byte) 0x50))
			return "image/png";
		else
			throw new IllegalArgumentException(Integer.toHexString(b1) + " " + Integer.toHexString(b2));
	}

}
