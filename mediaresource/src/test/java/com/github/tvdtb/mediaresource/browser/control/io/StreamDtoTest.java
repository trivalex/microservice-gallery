package com.github.tvdtb.mediaresource.browser.control.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class StreamDtoTest {

	private static byte[] bytes;
	private static File tempFile;
	private static File tempFile2;

	@BeforeClass
	public static void setup() throws Exception {
		bytes = new byte[] { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, (byte) 0xAF, (byte) 0xFE, 0x00, 0x01,
				0x02, 0x03, 0x04 };
		tempFile = File.createTempFile("mediaresource", ".data");
		tempFile2 = File.createTempFile("mediaresource", ".data");
		try (FileOutputStream fos = new FileOutputStream(tempFile)) {
			fos.write(bytes);
		}
		try (FileOutputStream fos = new FileOutputStream(tempFile2)) {
			for (int i = 0; i < 1000; i++)
				fos.write(bytes);
		}
		for (int i = 0; i < bytes.length; i++)
			System.out.print(" " + i + "=" + bytes[i]);
		System.out.println();
		tempFile.deleteOnExit();
		tempFile2.deleteOnExit();
	}

	@Test
	public void testBytes() throws IOException {
		StreamDto sr = StreamDto.fromBytes("bytes", bytes, "application/octet-stream", -1L);

		_testStreamResult(sr);

	}

	@Test
	public void testTempFile() throws IOException {
		StreamDto sr = StreamDto.fromFile(tempFile, "application/octet-stream");

		_testStreamResult(sr);

	}

	private void _testStreamResult(StreamDto sr) throws IOException {
		for (int i = 0; i < 10; i++) {
			sr.mark();
			InputStream content = sr.getContent();
			try {
				byte[] read = new byte[3];
				content.read(read);
				byte[] expected = new byte[] { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA };
				Assert.assertArrayEquals(expected, read);
			} finally {
				sr.reset();
			}
			if (i % 2 == 0) {
				content.close();
			}
		}

		assertThat(sr.getContent().read(new byte[1000]), equalTo(bytes.length));
	}

}
