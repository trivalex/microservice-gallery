package com.github.tvdtb.mediaresource.streams;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Component
@Path("streams")
public class StreamResource {

	// TODO - should be its own microservice

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
	// @Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("{user}/{stream}")
	public Response postMedia(//
			@PathParam("user") String user, //
			@PathParam("stream") String stream, //
			// InputStream in //
			@FormDataParam("file") InputStream file, @FormDataParam("file") FormDataContentDisposition fileDisposition
	// @Context HttpServletRequest request
	) throws IOException {
		System.out.println("MULTIPART: " + user + "  stream=" + stream);

		System.out.println("filedispo=" + fileDisposition + " size=" + fileDisposition.getSize() + "  file=" + file);

		try (FileOutputStream fos = new FileOutputStream("upload_data." + (++counter) + ".jpg")) {
			byte[] buffer = new byte[100 * 8192];
			int bytes = 0;
			while (((bytes = file.read(buffer))) >= 0) {
				System.out.println("BYTES = " + bytes);
				fos.write(buffer, 0, bytes);
			}
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
}
