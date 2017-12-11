package com.github.tvdtb.mediaresource.browser;

import java.security.Principal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Component;

import com.github.tvdtb.mediaresource.browser.boundary.BrowserBoundary;
import com.github.tvdtb.mediaresource.browser.boundary.ImageSize;
import com.github.tvdtb.mediaresource.browser.control.io.StreamDto;
import com.github.tvdtb.mediaresource.browser.entity.Alba;
import com.github.tvdtb.mediaresource.browser.entity.Album;
import com.github.tvdtb.mediaresource.browser.entity.FolderInformation;
import com.github.tvdtb.mediaresource.rest.Hateoas;
import com.github.tvdtb.mediaresource.rest.HateoasDto;
import com.github.tvdtb.mediaresource.rest.HateoasEntity;
import com.github.tvdtb.mediaresource.rest.HateoasRegistry;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Component
@Api("Browser")
@Path("browser")
@Produces(MediaType.APPLICATION_JSON)
public class BrowserResource {

	@Inject
	BrowserBoundary boundary;

	@Inject
	HateoasRegistry hateoas;

	@Context
	UriInfo uriInfo;

	@PostConstruct
	public void init() {
		hateoas.register(BrowserResource.class, (UriInfo uriInfo, Principal principal, HateoasEntity result) -> {
			result.addLink(Hateoas.fromMethod(uriInfo, "alba", BrowserResource.class, "getAlba"));
		});
		;
	}

	@ApiOperation(value = "getAlba")
	@GET
	@Path("/alba/")
	public Response getAlba() {
		List<HateoasDto<Album>> alba = boundary.readAlba().stream() //
				.map(a -> hateoas(a)) //
				.collect(Collectors.toList());
		Alba result = new Alba();
		result.addLink(Hateoas.fromCaller(uriInfo));
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

		return Response.ok(hateoas(a)).build();
	}

	private HateoasDto<Album> hateoas(Album a) {
		HateoasDto<Album> result = new HateoasDto<Album>(a);
		result.addLink(Hateoas.fromMethod(uriInfo, "self", BrowserResource.class, "getAlbum", a.getName()));
		result.addLink(Hateoas.fromMethod(uriInfo, "root", BrowserResource.class, "browseFolder" //
				, a.getName(), ""));
		result.addLink(Hateoas.fromMethod(uriInfo, "listAll", BrowserResource.class, "listFolderAll" //
				, a.getName(), ""));
		return result;
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
		result.addLink(Hateoas.fromCaller(uriInfo, album, path));

		return Response.ok(result).build();

	}

	private FolderInformation readItems(Album a, String path, boolean parent) {

		FolderInformation folder = boundary.readFolder(a, path);

		folder.getFolders().stream().forEach(f -> {
			String folderPath = concatPath(path, f.getName());
			f.addLink(Hateoas//
					.fromMethod(uriInfo, "self", getClass(), "browseFolder", //
							a.getName(), folderPath));

			FolderInformation childFolder = boundary.readFolder(a, folderPath);
			f.setIcons(new LinkedList<>());

			int skip = calculateDelta(childFolder.getImages().size(), 10);
			AtomicInteger ai = new AtomicInteger();

			childFolder.getImages().stream().filter(ii -> {
				if (ai.incrementAndGet() < skip) {
					return false;
				} else {
					ai.set(0);
					return true;
				}
			}).map(i -> {

				return Hateoas//
						.fromMethod(uriInfo, "icon", getClass(), "getMedia", //
								a.getName(), folderPath, i.getName(), ImageSize.ICON.name());
			}).forEach(h -> f.getIcons().add(h));

		});

		folder.getImages().forEach(i -> {
			i.addLink(Hateoas//
					.fromMethod(uriInfo, "icon", getClass(), "getMedia", //
							a.getName(), path, i.getName(), ImageSize.ICON.name()));

			i.addLink(Hateoas//
					.fromMethod(uriInfo, "preview", getClass(), "getMedia", //
							a.getName(), path, i.getName(), ImageSize.PREVIEW.name()));

			i.addLink(Hateoas//
					.fromMethod(uriInfo, "download", getClass(), "getMedia", //
							a.getName(), path, i.getName(), "download"));

		});

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
