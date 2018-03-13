import { Logger as NGxLogger } from "angular2-logger/core";

const ngxLogger = new NGxLogger();
window["logger"] = ngxLogger;

export class Logger {

    public error(message?: any, ...optionalParams: any[]): void {
        ngxLogger.error(message, ...optionalParams);
    }
    public info(message?: any, ...optionalParams: any[]): void {
        ngxLogger.info(message, ...optionalParams);
    }
    public warn(message?: any, ...optionalParams: any[]): void {
        ngxLogger.warn(message, ...optionalParams);
    }
    public log(message?: any, ...optionalParams: any[]): void {
        ngxLogger.log(message, ...optionalParams);
    }
    public debug(message?: any, ...optionalParams: any[]): void {
        ngxLogger.debug(message, ...optionalParams);
    }



}

