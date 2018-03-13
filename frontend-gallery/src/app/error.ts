import { ErrorConsumer } from './hateoas/hateoas.module'

export interface ErrorHandler {
  error(e:Error);
}

export class Error {
  constructor(public fatal:boolean, public statusCode: number, public statusText: string, public url: string) {
  }
}


export class AppErrorConsumer extends ErrorConsumer {
  private handlers: Array<ErrorHandler>;
  private errors: Array<Error>

  constructor() {
    super();
    this.handlers = new Array();
    this.errors = new Array();
  }

  notifyHandlers() {
    this.errors.forEach(error => {
      this.handlers.forEach(handler => {
        handler.error(error)
      });
    });
    this.errors.length = 0;

  }

  addHandler(handler: ErrorHandler) {
    this.handlers.push(handler);
    this.notifyHandlers();
  }

  fatal(statusCode: number, statusText: string, url: string)  {
    this.errors.push(new Error(true, statusCode, statusText, url));
    if (this.handlers.length > 0)
      this.notifyHandlers();
  }

  error(statusCode: number, statusText: string, url: string) {
    this.errors.push(new Error(false, statusCode, statusText, url));
    if (this.handlers.length > 0)
      this.notifyHandlers();
  }
}

export const AppErrorConsumerFactory = {
  provide: 'ErrorConsumer',
  useClass: AppErrorConsumer
}
  
