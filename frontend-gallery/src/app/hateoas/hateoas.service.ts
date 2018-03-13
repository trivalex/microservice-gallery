import 'core-js/es6/map';
import 'core-js/es6/set';

import './hateoas.model';

import { Injectable, Inject } from '@angular/core';
import { Http, Response, Request, RequestMethod, RequestOptionsArgs, Headers } from '@angular/http'

import { Logger } from "../logger/logger.service";

// https://www.npmjs.com/package/rxjs
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/share';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/forkJoin';


import { Hateoas, Options, Link, LinkContainer, ErrorConsumer, HateoasListener } from './hateoas.model';
//import { JsonMapper } from './jsonmapper.service'


export class HateoasConfig {
    rootPaths: Array<string>;
    headers?: HateoasHeaders;
}
export class HateoasHeaders {
    [name:string] : any;
}



/**
 * Service für die schnelle Nutzung von HATEOAS-Links.
 * 
 * 
 */

@Injectable()
export class HateoasService {
    observable: Observable<Hateoas>;
    urls: Array<string>;
    rootLinks: Hateoas;
    active: boolean=false;
    public counters : any = { all : 0 }
    listeners: Array<HateoasListener> = [];
    headers: HateoasHeaders;

    constructor(private _http: Http
        , config: HateoasConfig
        , private _logger: Logger
        , @Inject('ErrorConsumer') private errorConsumer?: ErrorConsumer
    ) {
        //console.log("HATEOAS config=", config)
        this.headers = config.headers
        this.initFromUrls(config.rootPaths);
    }

    public rootLink(linkName:string, parameters?: { [name: string]: string }) : Link {
        let link = Reflect.get(this.rootLinks._links, linkName);
        if (parameters) {
            let result = new Link()
            result.copyFrom(link);
            let options = new Options("dummyLink", parameters);
            result.href = this.createLinkUrl(link.href, options)
            link = result
        } 
        return link;
    }

    private directOrDeferred<T>(fn: (rootLinks: Hateoas) => T): Observable<T> {
        if (this.rootLinks != null)
            return Observable.of(fn(this.rootLinks));
        else {
            return this.observable.map(fn);
        }
    }

    public setHeaders(headers: HateoasHeaders) {
        this.headers = headers;
    }

    addListener(listener:HateoasListener) {
        this.listeners.push(listener);
    }

    notifyListeners() {
        if (this.rootLinks!=null) {
            this.listeners.forEach(l => l.linksChanged(this.rootLinks._links));
        }
    }

    reload() {
        this.initFromUrls(this.urls);
    }

    getRootLink(linkName: string): Observable<Link> {
        let fn: (x: Hateoas) => Link = (rootLinks: Hateoas) => Reflect.get(rootLinks._links, linkName);
        return this.directOrDeferred(fn);
    }
    getRootLinks(): Observable<LinkContainer> {
        let fn: (x: Hateoas) => LinkContainer = (rootLinks: Hateoas) => rootLinks._links;
        return this.directOrDeferred(fn);
    }

    checkRootLink(linkName: string): Observable<boolean> {
        let fn: (x: Hateoas) => boolean = (rootLinks: Hateoas) => Reflect.has(rootLinks._links, linkName);
        return this.directOrDeferred(fn);
    }

    private createRequestOptions() {
        let result = null;
        let headers = this.createRequestHeaders()
        if (headers!=null) {
            result = <RequestOptionsArgs>{headers: headers}           
        }
        return result;
    }
    private createRequestHeaders() {
        let myHeaders = null;
        if (this.headers) {
            myHeaders = new Headers();
            Object.keys(this.headers).forEach( name => {
                let value = this.headers[name]
                if (typeof value === "function") {
                    value = value()
                }
                myHeaders.append(name, value);
            })
        }
        return myHeaders;
    }

    private initFromUrls(urls: Array<string>) {
        // initialisieren
        this._logger.debug(">INIT ", urls);
        this.urls = urls;
        this.rootLinks = null;
        

        // Ein Array aller Observables für alle URLs
        let observables:Observable<Response>[] = 
            urls.map(url=>{ 
               return this._http.get(url, this.createRequestOptions())//
               .catch(x=>{
                   this._logger.debug("ERROR: ",url, "not available!",x);
                   return Observable.of(<Response>null);
               })
               .share()
            });

        // per ForkJoin zu einem Observable zusammengefasst
        let responsesObservable:Observable<Response[]> = Observable.forkJoin(observables) //
            .share();

        // zu einem Observable<Hateoas> umwandeln
        this.observable = responsesObservable
        .map( //
            (responses: Response[]) => {//
                let result:Hateoas = new Hateoas();
                responses.map(response => 
                    this.deserialize(Hateoas, response==null ? null : response.json()) //
                ).forEach(element => {
                    result.add(element);  
                })
                return result;
            }
        ).share();

        // Am Ende alle Links abholen und speichern
        this.observable.subscribe(
            rootLinks => {
                this.rootLinks = rootLinks;
                console.log("<INIT ", this.rootLinks)
                this.notifyListeners();
            }
        );

    }

    getLinkedEntity<T extends { copyFrom(jsonObject:any)}>(clazz: { new (): T }, whereToGo: Options | string | Link) {
        let options = Options.create(whereToGo);
        return this.followLink(options) //
            .map((response: any) => {
                let result = this.deserialize(clazz, response.json());
                return result;
            });
    }

    followLink(whereToGo: Options | string | Link): Observable<Response> {
        let options = Options.create(whereToGo);
        if (this.rootLinks)
            return this._followLink(options);
        else {
            return this.observable.flatMap((rootLinks: Hateoas) => {
                this.rootLinks = rootLinks;
                return this._followLink(options);
            });
        }
    }

    createBodyObject(options:Options) {
        let result = {};
        this.copy(options.body, result, key => !key.startsWith("_"));
        return result;
    }

    _followLink(options: Options): Observable<Response> {
        const hateoasService = this;
        let link = this.getLink(this.rootLinks, options);
        this._logger.debug("Link for  ", options, " resolved to ", link);
        let href = this.createLinkUrl(link.href, options);
        let method = this.getMethod(link);
        let bodyObject = this.createBodyObject(options);
        this._logger.debug("Link for options ", options, " resolved to ", href, method, bodyObject);
        let request = new Request({ url: href, method: method, body: bodyObject, headers: this.createRequestHeaders() });

        this.counters.all++;
        this.active = this.counters.all > 0;
        let result = this._http //
            .request(request)
            .share();

        result.subscribe(response => {
        }, errorResponse => {
            hateoasService.decrementCounters();
            let status = errorResponse.status;
            if (options.ignoreErrors && options.ignoreErrors.find(x=>x==status)) {
                this._logger.debug("ignoring error handled by component: ", status)
            } else {
                hateoasService.errorConsumer.error(errorResponse.status, errorResponse.statusText, errorResponse.url);
            }
            return new Array(); // this observable will never produce a value
        }, () => {
            hateoasService.decrementCounters();
        })
        return result;
    }

    decrementCounters() {
        this.counters.all--;
        this.active = this.counters.all > 0;
    }

    getMethod(l: Link): RequestMethod {
        switch (l.method) {
            case 'POST': return RequestMethod.Post;
            case 'PUT': return RequestMethod.Put;
            case 'DELETE': return RequestMethod.Delete;
        }
        return RequestMethod.Get;
    }

    createLinkUrl(template: string, options: Options) {
        let re = /(.*)\{([^\}]+)\}(.*)/;
        let link = template;

        let rer = re.exec(link);
        //this._logger.debug(">processing link ...")
        while (rer != null) {
            let value: string = options.parameters[rer[2]];
            if (value == null)
                throw new Error("Link " + template + " requires property " + rer[2]);
            link = rer[1] + value + rer[3];
            rer = re.exec(link);
        }
        //this._logger.debug("< link", link)
        return link;
    }

    getLink(lc: Hateoas, options: Options): Link {
        if (options.name != null) {
            let result = lc._links[options.name];
            if (result == null)
                throw new Error("unknown link named " + options.name);
            return result;
        } else if (options.link != null) {
            return options.link;
        }
        this._logger.error("unknown link options", options);
        throw new Error("unknown link options" + options);
    }


    public deserialize<T extends { copyFrom(jsonObject:any)}>(clazz: { new (): T}, jsonObject:any):T {
        let result = new clazz();

        result.copyFrom(jsonObject);

        return result;
    }


    public copy(source: any, target: any, filter:(x:string)=> boolean) {
        if (source==null)
            return null;
        let keys = Object.keys(source);
        keys.forEach((key) => {
            console.log("KEY ", key, !key.startsWith("_"), filter(key))
            if (filter(key)) {
                let value = Reflect.get(source, key);
                let newTarget = this._copy(value, filter);
                Reflect.set(target, key, newTarget);
            }
        });
    }

    public _copy(value:any, filter:(x:string)=> boolean) : any {
        let newTarget = null;
        if (value==null) {
            newTarget = null;
        } else if (this.isPrimitive(value)) {
            newTarget = value;
        } else if (value instanceof Array) {
            newTarget = [];
            value.forEach(e => {
                newTarget.push(e);
            })
        } else {
            newTarget = {};
            this.copy(value, newTarget, filter);
        }
        return newTarget;
    }

    public isPrimitive(obj:any) {
        let result = false;
        switch (typeof obj) {
            case "string":
            case "number":
            case "boolean":
                result= true;
        }
        if (!result)
        result = !!(obj instanceof String || obj === String ||
            obj instanceof Number || obj === Number ||
            obj instanceof Date || obj === Date ||            
            obj instanceof Boolean || obj === Boolean);
        return result;
    }



}
