
import { Hateoas, Link } from '../../app/hateoas/hateoas.model';


export class Alba extends Hateoas {
  defaultAlbum: string = null;

  //@JsonProperty('alba', 'Album')
  //Reflect.metadata('jsonProperty', { name: "alba"})
  alba: Array<Album> = [];

  copyFrom(json: any) {
    super.copyFrom(json);
    this.defaultAlbum = json["defaultAlbum"];
    this.alba = new Array<Album>();
    if (json["alba"] != null) {
      json["alba"].forEach(element => {
        let xe = new Album();
        xe.copyFrom(element);
        this.alba.push(xe);
      });
    }
  }
}


export class Album extends Hateoas {
  name: string = null;
  path: string = null;


  copyFrom(json: any) {
    super.copyFrom(json);
    this.name = json["name"];
    this.path = json["path"];
  }

  test(x) {
    console.log("in album " + x);
  }
}

export class ImageLinks {
  icon:string = null;
  preview: string = null;
}

export class Image extends Hateoas {
  name: string = null;
  path: string = null;
  type: string = null;
  width: number = null;
  height: number = null;
  orientation: number = null;
  dominantColor: string = null;
  imageLinks:ImageLinks = new ImageLinks();
  view: any;

  getRotatedWidth() {
    let result = this.width
    if (this.orientation!=1 && this.orientation!=-1)
      result = this.height
    return result;
  }
  getRotatedAspectRatio() {
    let result = this.width/this.height
    if (this.orientation!=1 && this.orientation!=-1)
      result = 1/result
    return result;
  }

  copyFrom(json: any) {
    super.copyFrom(json);
    this.name = json["name"]
    this.path = json["path"]
    this.dominantColor = json["dominantColor"]
    this.type = json["type"]
    this.width = json["width"]
    this.height = json["height"]
    this.orientation = json["orientation"]
  }
}

export class Folder extends Hateoas {
  name: string = null;
  path: string = null;
  //  @JsonProperty('content', 'FileItem')
  folders: Array<Folder> = null;
  images: Array<Image> = null;
  icons: Array<Link> = null;

  copyFrom(json: any) {
    super.copyFrom(json);
    this.name = json["name"]
    this.path = json["path"]
    this.folders = [];
    this.images = [];
    this.icons = [];
    if (json["folders"] != null) {
      json["folders"].forEach(element => {
        let fi = new Folder();
        fi.copyFrom(element);
        this.folders.push(fi);
      })
    }
    if (json["images"] != null) {
      json["images"].forEach(element => {
        let fi = new Image();
        fi.copyFrom(element);
        this.images.push(fi);
      })
    }
    if (json["icons"] != null) {
      json["icons"].forEach(element => {
        let h = new Link();
        h.copyFrom(element);
        this.icons.push(h);
      })
    }
  }
}
