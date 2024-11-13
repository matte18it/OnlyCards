import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class UrlInterceptor implements HttpInterceptor {

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Modifica la richiesta aggiungendo il carattere %20 al posto dello spazio
    const modifiedUrl = request.url.replace(/ /g, '%20');
    const modifiedRequest = request.clone({ url: modifiedUrl });

    return next.handle(modifiedRequest);
  }
}
