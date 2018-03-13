import { Component, ViewChild } from '@angular/core';
import { Sanitizer } from '@angular/core';
import { NavController, NavParams, ViewController, LoadingController, Loading } from 'ionic-angular';
import { Album, Folder, Image } from '../list/album.model';

@Component({
  selector: 'page-gallery',
  templateUrl: 'gallery.html',
})
export class GalleryPage {

  @ViewChild('#bottom')
  bottomElement;

  album: Album;
  currentIndex: number = -1;
  folder: Folder;
  item: Image;
  hasNext: boolean;
  hasPrevious: boolean;
  loading: boolean = false;
  loadingSpinner: Loading;

  constructor(
    public navCtrl: NavController
    , public loadingCtrl: LoadingController
    , public navParams: NavParams
    , public viewCtrl: ViewController
    , public sanitizer: Sanitizer) {

    this.album = navParams.get("album")
    this.folder = navParams.get("folder")
    this.item = navParams.get("item")
    this.select(this.folder.images.indexOf(this.item));
    this.updateNextPrev();
  }

  ngAfterViewInit() {
    this.bottomElement = document.getElementById("bottom")
    this.checkBottomVisible();
  }

  ionViewDidLoad() {
  }

  swipeEvent(event) {
    if (event && event.direction == 2)
      this.next();
    else if (event && event.direction == 4)
      this.previous();
  }

  previous() {
    this.offset(-1);
  }

  next() {
    this.offset(1);
  }

  offset(offset) {
    let index = this.currentIndex + offset;
    if (index >= this.folder.images.length)
      index = this.folder.images.length - 1;
    if (index < 0)
      index = 0;
    this.select(index);
  }

  select(index) {
    if (index != this.currentIndex) {
      let newItem = this.folder.images[index];
      try {
        var i = document.createElement('img'); // or new Image()
        this.loading = true;
        let _this = this;
        let temp = {
          loadingSpinner : this.loadingCtrl.create({ content: '', showBackdrop: false })
          , timeout: null
        };
        i.onload = function () {
          _this.item = _this.folder.images[index];
          _this.loading = false;
          _this.currentIndex = index;
          _this.checkBottomVisible();
          _this.updateNextPrev();
          clearTimeout(temp.timeout);
          temp.loadingSpinner.dismiss();
        }
        temp.timeout = setTimeout(() => temp.loadingSpinner.present(), 700);
        i.src = newItem.imageLinks.preview;
      } catch (e) {
        this.item = this.folder.images[index];
      }
    }
  }

  iconWidth = 80;
  iconMargin = 4;
  checkBottomVisible() {
    if (this.bottomElement) {

      let iconMiddle=0;
      //let scrollLeft = this.bottomElement["scrollLeft"];
      for (let x = 0; x<=this.currentIndex; x++) {
        let fileX = this.folder.images[x];
        let factor = x<this.currentIndex ? 1 : 0.5
        let ar=fileX.getRotatedAspectRatio()
        if (ar>=1) {
          let value = Math.round(factor * (this.iconWidth+this.iconMargin))
          //console.log(x+"="+value+" f="+factor)
          iconMiddle+=value;
        } else {
          let value=Math.round(factor*(this.iconWidth*ar+this.iconMargin))
          //console.log(x+"="+value+" f="+factor)
          iconMiddle+=value
        }
      }

      let width = this.bottomElement["clientWidth"]
      //console.log("width="+width+" middle="+iconMiddle+"-----------------------------------------------")
      if (width && width > 0) {
        let middle = width / 2;
        let iconScroll = iconMiddle - middle;
        //console.log("CHeck Bottom: ", scrollLeft, width, iconScroll)
        if (iconScroll < 0)
          iconScroll = 0;
        if (iconScroll > 0) {
            this.bottomElement.scrollTo({
              top: 0,
              left: iconScroll,
              behavior: 'smooth'
            })
        }
      }

    }
  }

  updateNextPrev() {
    let currentIndex = this.folder.images.indexOf(this.item);
    this.hasPrevious = currentIndex > 0;
    this.hasNext = currentIndex + 1 < this.folder.images.length;
  }

  selectBottom(item, index) {
    this.select(index);
  }

  dismiss() {
    this.viewCtrl.dismiss();
  }

  itemCssVariable(item: Image) {
    let degrees = 0;
    if (item.orientation == 6)
      degrees = 90;
    return '--image: url(' + encodeURI(item.imageLinks.preview) + ') ; --rotate: rotate(' + degrees + 'deg)'
  }

  bottomCssVariable() {
    let width=0;
    for (let x = 0; x<this.folder.images.length; x++) {
      let fileX = this.folder.images[x];
      let ar=fileX.getRotatedAspectRatio()
      if (ar>=1) {
        width+=this.iconWidth;
      } else {
        let value=this.iconWidth*ar
        width+=value
      }
    }
    return '--width: ' + width +"px"
  }

}
