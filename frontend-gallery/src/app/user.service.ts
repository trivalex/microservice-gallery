import 'core-js/es6/map';
import 'core-js/es6/set';
import { Storage } from '@ionic/storage';

import { Injectable } from '@angular/core';
import { HateoasService } from './hateoas';

export class User {
    username: string;
    password: string;
    token: string;
}

export class Token {
    value: string;
}

@Injectable()
export class UserService {

    public loggedIn: boolean = false;
    public user: User = new User();

    constructor(private storage: Storage, private $hateoas: HateoasService) {
        console.log("USER SERVICE")
        storage.ready().then(() => {
            storage.get('user').then((val) => {
                console.log('user', val);
                this.user = val;
                this.loggedIn = (val != null)
                if (this.loggedIn) {
                    this.$hateoas.setHeaders({"Authentication" : "Bearer "+this.user.token})
                }
            })
        });
    }

    login(user: User) {
        this.user = user;
        this.loggedIn = true;
        this.storage.set('user', this.user).then(() => {
            console.log('SAVED USER ', user);
        })
    }

    logoff() {
        this.user = new User();
        this.loggedIn = false;
        this.storage.set('user', null).then(() => {
            console.log('SAVED LOGOFF ', null);
        })
    }






}
