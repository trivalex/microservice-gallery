import { RequestMethod } from '@angular/http'
//import { JsonProperty } from './jsonmapper.model';

export class HateoasFactory {
    static produce(name: string, value?: any): any {
        switch (name) {
            case 'Link': return new Link();
            case 'LinkContainer': return new LinkContainer();
            case 'Date': return value==null ? null : new Date(value);
            }
        return null;
    }
}

export interface HateoasListener {
    linksChanged(lc:LinkContainer);
}

export class ErrorConsumer {
    fatal(httpCode:number, message: string, url: string) {
       console.log("HATEOAS FATAL: status=",httpCode, "message=",message, "url=",url);
    }
    error(httpCode:number, message: string, url: string) {
       console.log("HATEOAS ERROR: status=",httpCode, "message=",message, "url=",url);
    }
}

export class Link {
    rel: string = null;
    href: string = null;
    method: string = null;

    getMethod(): RequestMethod {
        return RequestMethod.Get;
    }

    public copyFrom(json:any) {
        this.rel = json["rel"];
        this.href = json["href"];
        this.method = json["method"];
    }
}

export class LinkContainer {
    [name:string] : Link;
}

export class Hateoas {

    public constructor() {
        this._links = new LinkContainer();
    }

    public copyFrom(json:any) {
        if (json["_links"]!=null) {
            Object.keys(json["_links"]).forEach(key => {
                let link = new Link();
                link.copyFrom(json["_links"][key]);
                this._links[key] = link;
           });
        }
    }

    public add(other:Hateoas){
        if (other==null || other._links==null)
            return;
        if (this._links==null)
            this._links = new LinkContainer;
        Object.keys(other._links).forEach(key => {
            this._links[key] = other._links[key];
        });
    }

    //@JsonProperty('_links', 'LinkContainer', 'inverse')
    _links: LinkContainer = null;
}

export class Options {
    name?: string = null;
    link?: Link = null;
    factory?: (x: string, value?:any) => any;
    parameters?: { [name: string]: string }
    body?: any;
    ignoreErrors?: Array<number>;

    constructor(where: string | Link, parameters?: { [name: string]: string }) {
        if (typeof where == "string")
            this.name = <string>where;
        else
            this.link = <Link>where;
        this.parameters = parameters;
    }

    static create(whereToGo: string | Options | Link): Options {
        //console.log("OPTIONS.create ", whereToGo, typeof whereToGo)
        if (whereToGo instanceof Options)
            return <Options>whereToGo;
        else if (whereToGo instanceof Link)
            return new Options(whereToGo);

        if (typeof whereToGo ==='object' && whereToGo["href"]!=null ) {
            // es ist ein link!
            let result= new Options(whereToGo);
            return result;
        }

        let options = null;
        if (typeof whereToGo == 'string')
            options = new Options(<string>whereToGo);
        else if (typeof whereToGo == 'object') {
            options = new Options(Reflect.get(whereToGo, "name"))

            let _link = Reflect.get(whereToGo, "link"); 
            if (_link != null) {
                let link = new Link();
                link.href = Reflect.get(_link, "href");
                link.rel = Reflect.get(_link, "rel");
                link.method = Reflect.get(_link, "method");
                options.link  =link;
            }
            options.parameters =Reflect.get( whereToGo, "parameters");
            options.factory =Reflect.get( whereToGo, "factory");
            options.body = Reflect.get( whereToGo, "body");
            options.ignoreErrors =Reflect.get( whereToGo, "ignoreErrors");
        } else {
            options = new Options(<Link>whereToGo);
        }

        return options;
    }
}
