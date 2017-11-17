# microservice-gallery

## What it is
A simple photo gallery microservice/HTML5 application 

## What it not is
Completed

## Architecture
Simple Spring Boot REST service.

Using HAL/Hateoas for linking.

HTML5 frontend

Pure Filesystem persistence & caching

Json Web Tokens for Authentication 

Java Imaging APIs, FASTJson 

## HAL
The frontend relies on links defined by the backend.

/api/

   ```json
   {
	"_links": {
		"alba": {
			"method" : "GET",
			"href" : "/media/api/browser/alba/"
		}
	}
   }
   ```

/media/api/browser/alba/ returns an array of album objects. each of those has a link:


   ```json
   ..
	"_links": {
		"root": {
			"method" : "GET",
			"href" : "/media/api/browser/alba/default/"
		}
	...
	}
   ```

This leads to a recursive folder structure containing "folders" and "images". 

A folder is navigationable by its self link:

   ```json
   ..
	"_links": {
		"self": {
			"method" : "GET",
			"href" : "/media/api/browser/alba/default/pictures"
		}
	...
	}
   ```

An image provides its download links, currently only "icon" and "preview" are used by the frontend

   ```json
   ..
	"_links": {
		"icon": {
			"method" : "GET",
			"href" : "/media/api/browser/alba/default/pictures/some.jpg/ICON"
		},
		"preview": {
			"method" : "GET",
			"href" : "/media/api/browser/alba/default/pictures/some.jpg/PREVIEW"
		},
		"download": {
			"method" : "GET",
			"href" : "/media/api/browser/alba/default/pictures/some.jpg/download"
		}
	...
	}
   ```



## Howto
Create directory .mediaresource in your user home, put a file config.json pointing to your pictures directory (path property):

   ```json
   {
	"album": {
		"dirFilter": "\\.cache",
		/* 0=album.name 1=album.path, 2=path, 3=fileName, 4=qualifier, 5=extension */
		"cacheDirPattern": "{1}/.cache/{2}/{3}.{4}.{5}",
		"alba": [
			{
				"name": "default",
				"path": "album",
			}
		]
	}
   }
   ```


Build project

   ```
   mvn package
   ```

run 

   ```
   java -jar target/mediaresource-0.0.1-SNAPSHOT.jar
   ```

open 

   ```
   http://localhost:8080/media/
   ```
   
UI:

![screenshot0](https://user-images.githubusercontent.com/12240765/32807989-fc9019ec-c991-11e7-9655-db65beb4da15.jpg)

![screenshot1](https://user-images.githubusercontent.com/12240765/32807990-fcb35b96-c991-11e7-8411-df86483c0844.jpg)