package com.github.tvdtb.mediaresource.browser;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.github.tvdtb.mediaresource.browser.boundary.BrowserBoundary;
import com.github.tvdtb.mediaresource.browser.boundary.ImageSize;
import com.github.tvdtb.mediaresource.browser.control.io.StreamDto;
import com.github.tvdtb.mediaresource.browser.entity.Alba;
import com.github.tvdtb.mediaresource.browser.entity.Album;
import com.github.tvdtb.mediaresource.browser.entity.FolderInformation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Component
@Api("Browser")
@Path("browser")
@Produces(MediaType.APPLICATION_JSON)
public class BrowserResource {

	@Inject
	BrowserBoundary boundary;

	@ApiOperation(value = "getAlba")
	@GET
	@Path("/alba/")
	public Response getAlba() {
		List<Album> alba = boundary.readAlba().stream() //
				.collect(Collectors.toList());
		Alba result = new Alba();
		result.setAlba(alba);
		return Response.ok(result).build();
	}

	@ApiOperation(value = "getAlbum")
	@GET
	@Path("/alba/{album}")
	public Response getAlbum(@PathParam("album") String album //
	) throws Exception {
		Album a = boundary.readAlbum(album);
		if (a == null) {
			throw new NotFoundException("Album: " + album);
		}

		return Response.ok(a).build();
	}

	@ApiOperation(value = "browseFolder")
	@GET
	@Path("/alba/{album}/{path:.*}")
	public Response browseFolder(@PathParam("album") String album //
			, @PathParam("path") String path) throws Exception {
		if (path.endsWith("/"))
			path = path.substring(0, path.length() - 1);
		if (path.startsWith("/"))
			path = path.substring(1);

		Album a = boundary.readAlbum(album);
		if (a == null) {
			throw new NotFoundException("Album: " + album);
		}

		FolderInformation result = readItems(a, path, true);

		return Response.ok(result).build();

	}

	private FolderInformation readItems(Album a, String path, boolean parent) {

		FolderInformation folder = boundary.readFolder(a, path);

		folder.getFolders()//
				.sort((first, second) -> //
		-1 * first.getName()//
				.compareTo(//
						second.getName()));

		return folder;
	}

	int calculateDelta(int imageCount, int maxIconCount) {
		int skip = 0;
		if (imageCount > 1.5 * maxIconCount) {
			skip = ((int) ((double) imageCount) / maxIconCount - 1);
		}
		return skip;
	}

	private String concatPath(String path, String name) {
		if (path.length() == 0 || path.endsWith("/") || name.startsWith("/"))
			return path + name;
		else
			return path + "/" + name;
	}

	@GET
	@Path("/alba/{album}/{path:.*}/{imageName}/{type}")
	public Response getMedia(//
			@PathParam("album") String albumName //
			, @PathParam("path") final String path//
			, @PathParam("imageName") final String imageName//
			, @PathParam("type") final String type//
	) throws Exception {

		ImageSize desiredSize = ImageSize.valueOf(type);

		Album album = boundary.readAlbum(albumName);
		if (desiredSize == null || album == null)
			return Response.status(Response.Status.NOT_FOUND).build();

		StreamDto sr = boundary.readImage(album, path, imageName, desiredSize);

		Date fileDate = new Date(sr.getDate());
		Date expires = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);

		return Response.ok(sr.getContent())//
				.header("Content-Type", sr.getType())//
				.header("date", fileDate) //
				.header("expires", expires) //
				.header("cache-control", "public, max-age=86400, no-transform") //
				.build();
	}

}
