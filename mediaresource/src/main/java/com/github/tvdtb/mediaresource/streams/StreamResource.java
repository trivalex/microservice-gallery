package com.github.tvdtb.mediaresource.streams;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.imaging.util.IoUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

import com.github.tvdtb.mediaresource.browser.boundary.BrowserBoundary;
import com.github.tvdtb.mediaresource.browser.control.MediaTypeControl;
import com.github.tvdtb.mediaresource.browser.control.io.StreamDto;

@Component
@Path("streams")
public class StreamResource {

	// TODO - should be its own microservice

	@Inject
	MediaTypeControl mediaTypeControl;

	@Inject
	BrowserBoundary browser;

	static volatile int counter = 0;

	@OPTIONS
	@Path("/")
	public Response optionsMedia() {
		return Response.ok()//
				.header("Access-Control-Allow-Origin", "*")//
				.build();
	}

	@GET
	@Path("/")
	public Response getStreams() {
		return Response.ok()//
				.header("Access-Control-Allow-Origin", "*")//
				.build();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("{albumName}/{dummy}")
	public Response postMedia(//
			@PathParam("albumName") String albumName, //
			@PathParam("dummy") String dummy, //
			@FormDataParam("file") InputStream file, @FormDataParam("file") FormDataContentDisposition fileDisposition
	// @Context HttpServletRequest request
	) throws IOException {
		System.out.println("filedispo=" + fileDisposition + " size=" + fileDisposition.getSize() + "  file=" + file);

		BufferedInputStream in = new BufferedInputStream(file, 100);
		in.mark(100);
		int byte1 = in.read();
		int byte2 = in.read();
		in.reset();

		if (byte1 == 'P' && byte2 == 'K') {
			ZipInputStream zis = new ZipInputStream(in);
			ZipEntry zipEntry = null;
			while ((zipEntry = zis.getNextEntry()) != null) {
				BufferedInputStream zisBis = new BufferedInputStream(zis, 100);
				saveFile(albumName, zisBis, zipEntry.getName(), zipEntry.getTime());
			}
		} else {
			Date modificationDate = fileDisposition.getModificationDate();
			saveFile(albumName, in, fileDisposition.getFileName(),
					modificationDate == null ? -1L : modificationDate.getTime());
		}

		// multipart.getFields().entrySet().forEach(entry -> {
		// System.out.println(entry.getKey()+"=");
		// entry.getValue().stream().forEach(value -> {
		// byte[] bytes = value.getEntityAs(byte[].class);
		// System.out.println(" bytes="+bytes);
		// });
		//
		// });

		return Response //
				.ok("ok", MediaType.TEXT_PLAIN)//
				.header("Access-Control-Allow-Origin", "*")//
				.build();
	}

	private String saveFile(String albumName, InputStream in, String name, long time) throws IOException {
		in.mark(100);
		String type = mediaTypeControl.readMediaTypeFrom(in);
		in.reset();
		byte[] imageBytes = IoUtils.getInputStreamBytes(in);
		StreamDto streamDto = StreamDto.fromBytes(name, ""/* path */, imageBytes, type, time);

		String path = browser.writeImage(albumName, streamDto);
		System.out.println("PATH=" + path);
		return path;

	}

	private void readFile(InputStream in) throws IOException, FileNotFoundException {
		try (FileOutputStream fos = new FileOutputStream("upload_data." + (++counter) + ".jpg")) {
			byte[] buffer = new byte[8192 * 8192];
			int totalBytes = 0;
			int bytes = 0;
			while (((bytes = in.read(buffer))) >= 0) {
				totalBytes += bytes;
				fos.write(buffer, 0, bytes);
			}
			System.out.println("BYTES = " + totalBytes);
		}
	}
}
