import { NgModule, ModuleWithProviders } from '@angular/core';
import { HttpModule } from '@angular/http';
import { HateoasService, HateoasConfig } from './hateoas.service';
export { ErrorConsumer } from './hateoas.model';


@NgModule({
    imports: [HttpModule],
    declarations: [],
    exports: [HttpModule],
    providers: [HateoasService]
})
export class HateoasModule {
    static forRoot(config: HateoasConfig): ModuleWithProviders {
        return {
            ngModule: HateoasModule,
            providers: [
                { provide: HateoasConfig, useValue: config }
            ]
        };
    }
}