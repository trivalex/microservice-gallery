import { Component } from '@angular/core';
import { NavController, NavParams } from 'ionic-angular';
import { HateoasService } from '../../app/hateoas/hateoas.service';
import { Alba, Album } from '../list/album.model';

/**
 * Generated class for the UploadPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */


@Component({
  selector: 'page-upload',
  templateUrl: 'upload.html',
})
export class UploadPage {
  alba:Album[]=null;
  album: Album = new Album();

  constructor(public navCtrl: NavController
    , public navParams: NavParams
    , private $hateoas: HateoasService) {
    this.$hateoas.getLinkedEntity(Alba, "alba").subscribe(alba => {
      this.alba = alba.alba;
      if (this.alba.length>0)
        this.album = this.alba[0];
  
    });

  }



  ionViewDidLoad() {
    console.log('ionViewDidLoad UploadPage');
  }

}
