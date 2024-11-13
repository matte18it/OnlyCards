import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../utility/environment";

@Injectable({
  providedIn: 'root'
})

export class CaptchaService {

  constructor(private http: HttpClient) {}

  verifyCaptcha(token: string): Observable<any> {
    //verifica del captcha
    return this.http.get<any>(`${environment.backendUrl}/v1/auth/captcha/${token}`);
  }
}
