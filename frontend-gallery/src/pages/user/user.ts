import { Component } from '@angular/core';
import { NavController, NavParams, AlertController } from 'ionic-angular';

import { User, UserService } from '../../app/user.service';
import { HateoasService } from '../../app/hateoas';



@Component({
  selector: 'page-user',
  templateUrl: 'user.html',
})
export class UserPage {

  username:string;
  password:string;

  constructor(public navCtrl: NavController
    , public navParams: NavParams
    , public userService: UserService
    , public alertCtrl: AlertController
    , public hateoas: HateoasService) {
  }

  ionViewDidLoad() {
    console.log('ionViewDidLoad UserPage');
  }

  login() {
    console.log("user=",this.username,"pass=",this.password)

    let user = <User>{ username: this.username, password: this.password};
    this.hateoas.followLink({name: "login", body: user}) //
    .subscribe(response=>{
        let token = response.text();
        user.token = token;
        user.password = "***";
        this.userService.login(user)
                    
    }, error => {
        this.userService.logoff();
        let alert = this.alertCtrl.create({
          title: 'Fehler!',
          subTitle: 'Anmeldung nicht erfolgreich!',
          buttons: ['OK']
        });
        alert.present();
    })
  }

  logoff() {
    this.userService.logoff();
  }

}
