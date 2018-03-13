import { BrowserModule } from '@angular/platform-browser';
import { ErrorHandler, NgModule } from '@angular/core';
import { IonicApp, IonicErrorHandler, IonicModule } from 'ionic-angular';
import { IonicStorageModule } from '@ionic/storage';

import { MyApp } from './app.component';
import { ListPage } from '../pages/list/list';
import { GalleryPage } from '../pages/gallery/gallery';
import { UserPage } from '../pages/user/user';

import { StatusBar } from '@ionic-native/status-bar';
import { SplashScreen } from '@ionic-native/splash-screen';

import { LoggerModule } from './logger/logger.module';
import { HateoasModule } from './hateoas/';
import { UserService } from './user.service';
import { AppHeaderComponent } from '../components/app-header/app-header';

import { AppErrorConsumerFactory } from "./error";
import { UploadPage } from '../pages/upload/upload';

@NgModule({
  declarations: [
    MyApp,
    UserPage,
    ListPage, 
    UploadPage,
    GalleryPage,
    AppHeaderComponent
  ],
  imports: [
    BrowserModule
    ,IonicModule.forRoot(MyApp)
    ,LoggerModule
    ,HateoasModule.forRoot({ rootPaths: ["assets/links.json"] })
    ,IonicStorageModule.forRoot()
    
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp,
    ListPage, GalleryPage, UserPage, UploadPage
  ],
  providers: [
    StatusBar,
    SplashScreen,
    AppErrorConsumerFactory, UserService,
    {provide: ErrorHandler, useClass: IonicErrorHandler}
  ]
})
export class AppModule {}

/*

Logger          https://github.com/code-chunks/angular2-logger

                logger.level = logger.Level.OFF; logger.store();
                logger.level = logger.Level.ERROR; logger.store();
                logger.level = logger.Level.WARN; logger.store();
                logger.level = logger.Level.INFO; logger.store();
                logger.level = logger.Level.DEBUG; logger.store();
                logger.level = logger.Level.LOG; logger.store();


*/