import { Component, ViewChild, ElementRef, HostListener } from '@angular/core';
import { Sanitizer } from '@angular/core';
import { NavController, NavParams, ModalController, LoadingController } from 'ionic-angular';

import { UserService } from '../../app/user.service';
import { Logger } from '../../app/logger';
import { HateoasService } from '../../app/hateoas/hateoas.service';
import { Alba, Album, Folder, Image } from './album.model';
import { GalleryPage } from "../gallery/gallery";
import { UserPage } from "../user/user";

@Component({
  selector: 'page-list',
  templateUrl: 'list.html'
  //  styleUrls:["./list.scss"]
})
export class ListPage {
  selectedItem: any;
  icons: string[];
  items: Array<{ title: string, note: string, icon: string }>;
  alba: Array<Album> = [];
  album: Album = null;
  folder: Folder;
  breadcrumb: Array<Folder>;
  counters: any;
  
  @ViewChild('iconrow')
  iconrowElement: ElementRef;
  
  iconTargetHeight=120
  lastWidth: number;

  constructor(public navCtrl: NavController
    , public loadingCtrl: LoadingController
    , public navParams: NavParams
    , private $hateoas: HateoasService
    , public userService: UserService
    , private logger: Logger
    , private modalCtrl: ModalController
    , public sanitizer: Sanitizer
  ) {
    this.album = navParams.get("album");
    this.setFolder(navParams.get("folder"));
    this.breadcrumb = navParams.get("breadcrumb");
    this.logger.info("ListPage album=", this.album);
    this.counters = $hateoas.counters;

    if (this.album == null) {
      this.$hateoas.getLinkedEntity(Alba, "alba").subscribe(alba => {
        this.alba = alba.alba;
      });
      this.breadcrumb = new Array();
    } else {
      console.log("ALBUM=", this.album, "FOLDER=", this.folder)

      let link = null;
      if (this.folder == null)  {
        link = this.$hateoas.rootLink("albumRoot", {album: this.album.name});
      } else {
        link = this.$hateoas.rootLink("folder", {album: this.album.name, folderPath: this.folder.path});
      }

      /*let link = this.folder == null ? 
        this.album._links["root"] :
        this.folder._links["self"];*/

      this.$hateoas.getLinkedEntity(Folder, link).subscribe(folder => {
        //console.log("FOLDER=", folder)
        this.setFolder(folder);
        window.setTimeout(()=>this.calculateIconSizes(), 100)
        window.setTimeout(()=>this.calculateIconSizes(), 2000) // required for some mobile devices
      });
    }

  }

  setFolder(folder:Folder) {
    if (folder!=null) {
      folder.images.forEach(item => {
        let ar = item.getRotatedAspectRatio()
        let height = this.iconTargetHeight
        let width = Math.round(Math.min(2*this.iconTargetHeight, ar*height))
        item.view = { width: width, height: height, targetWidth: width, targetHeight: height}
        item.imageLinks.icon = this.$hateoas.rootLink("imageIcon", { album: this.album.name, imagePath: item.path, image : item.name }).href
        item.imageLinks.preview = this.$hateoas.rootLink("imagePreview", { album: this.album.name, imagePath: item.path, image : item.name }).href
      })
    }
    this.folder = folder;
    window.setTimeout(()=>this.calculateIconSizes(), 100)
  }

  ngAfterViewInit() {
    this.calculateIconSizes()
  }

  @HostListener('window:resize', ['$event']) windowResize(event : any) {
    this.calculateIconSizes()
  }

  calculateIconSizes() {
    let margin=2
    //console.log("calculateIconSizes", this.folder, this.iconrowElement)
    if (this.folder!=null && this.iconrowElement!=null) {
      let width = this.iconrowElement.nativeElement.clientWidth - 10
      //if (this.lastWidth==width)
      //  return;
      //this.lastWidth = width;

      let media = this.folder.images
      //console.log("--------------------------------calculate width=",width, "length=", media.length)
      let rowWidth = 0;
      let startIndex = -1; // last included index
      let index = 0; // next (exclusive) index
      let distance = width
      let maxScale = 1;
      while (index < media.length) {
        let nextCount = index-startIndex
        let nextWidth = media[index].view.targetWidth
        let newDistance = Math.abs(width-rowWidth-nextWidth-nextCount*margin)
        //console.log("index=",index,"w=",width, "row", rowWidth," next",nextWidth, "dist", distance, "newDist", newDistance)
        let addedImage=false
        if (Math.abs(newDistance)<Math.abs(distance)) {
          index++
          distance = newDistance
          rowWidth+=nextWidth
          addedImage=true
        }

        let endReached = index>=media.length;
        if (!addedImage || endReached) {
          if (index==startIndex+1)
            index++;
          let count = index-startIndex-1; // index is not included
          let scale = (width-count*margin) / (rowWidth)
          //console.log("SCALE: end="+endReached+" max="+maxScale+" scale="+scale)
          if (endReached) {
            scale = Math.min(scale,maxScale)
          } else {
            maxScale = Math.max(maxScale, scale)
          }
          //console.log("--ROW count=", count, "scale=",scale, "start", startIndex, "index",index," w=",rowWidth+"/"+width)
          for (let i=startIndex+1; i<index; i++) {
            media[i].view.width = scale * media[i].view.targetWidth
            media[i].view.height = scale * media[i].view.targetHeight
          }
          startIndex=index-1
          rowWidth=0
          distance = width;
        }
      }
    }
  }    

  iconTargetWidth = 120;
  iconWidth = this.iconTargetWidth;

  albumTapped(event, item:Album) {
    this.logger.info("albumTapped=", event, item)

      // That's right, we're pushing to ourselves!
      this.navCtrl.setRoot(ListPage, {
        album: item
        , breadcrumb: []
      });
  }

  fileItemTapped(event, item: Image) {
    let modal = this.modalCtrl.create(GalleryPage, { album: this.album, folder: this.folder, item: item });
    modal.present();
  }

  folderTapped(event, item: Folder) {
      let breadcrumb = new Array();
      this.breadcrumb.forEach(f => breadcrumb.push(f))
      breadcrumb.push(item)
      this.navCtrl.push(ListPage, {
        album: this.album
        , folder: item
        , breadcrumb: breadcrumb
      });
  }

  breadcrumbTapped(event, folder: Folder) {
    let index = this.breadcrumb.indexOf(folder);
    let breadcrumb = this.breadcrumb.slice(0, index + 1);
    this.navCtrl.setRoot(ListPage, {
      album: this.album
      , folder: folder
      , breadcrumb: breadcrumb
    });
  }
  breadcrumbTappedAlbum(event, album: Album) {
    this.navCtrl.setRoot(ListPage, {
      album: this.album
      , breadcrumb : []
    });

  }

  isEmpty() {
    if (this.folder == null)
      return false;
    return (!this.folder.folders || this.folder.folders.length==0) 
        && (!this.folder.images || this.folder.images.length==0) 
  }

  calculateTransform(item: Image) {
    let degrees = 0;
    if (item.orientation == 6)
      degrees = 90;
    return 'rotate(' + degrees + 'deg)'
  }
  itemCssVariable(item: Image) {
    let color = item.dominantColor==null ? "#000000" : item.dominantColor
    return '--image: url(' + encodeURI(item.imageLinks.icon) + ') ; --dominant-color: '+color+'; width: '+item.view.width+"px; height: "+item.view.height+"px;";
  }

  orientation(item: Image) {
    let result = "horizontal";
    if (item.orientation == 6)
      result = "vertical";
    return result
  }

  folderIcons(folder:Folder) {
    let result=[];
    folder.icons.forEach( x => result.push(x.href));
    return result;
  } 

  iconRowCssVariable() {
    return '--width: ' + this.iconWidth +"px"
  }

  login() {
    this.navCtrl.push(UserPage);
  }
}

